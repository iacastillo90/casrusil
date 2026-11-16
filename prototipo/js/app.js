/* Núcleo del prototipo: estado, router por hash, shell, barra de control, diálogos y eventos. */
(function () {
  'use strict';
  var U = window.U, C = window.C, D = window.D, REG = window.REG, html = U.html, icon = U.icon, raw = U.raw;
  var V = window.V = window.V || {};      // vistas por id de pantalla
  var A = window.A = window.A || {};      // acciones de clic (data-action)
  var CH = window.CH = window.CH || {};   // acciones de cambio (data-action-change)

  // ---------- Estado ----------
  var KEY = 'proto-siierp-v2';
  var saved = {};
  try { saved = JSON.parse(localStorage.getItem(KEY) || '{}') || {}; } catch (e) { saved = {}; }
  var mods = {}; REG.MODULES.forEach(function (m) { mods[m[0]] = true; });
  var S = window.S = {
    role: saved.role || 'ADMIN',
    modules: saved.modules || mods,
    theme: saved.theme || 'light',
    screenState: saved.screenState || 'datos',
    collapsed: !!saved.collapsed,
    env: saved.env || 'cert',
    openGroups: saved.openGroups || { 'g-sales': true, 'g-tax': true },
    tables: {}, tabs: {}, wiz: {}, ui: {},
    pop: null, flyout: null, dialog: null, drawer: null, mobileOpen: false
  };
  function persist() {
    try { localStorage.setItem(KEY, JSON.stringify({ role: S.role, modules: S.modules, theme: S.theme, screenState: S.screenState, collapsed: S.collapsed, env: S.env, openGroups: S.openGroups })); } catch (e) { /* almacenamiento no disponible */ }
  }

  // ---------- Router ----------
  var MEM = document.documentElement.getAttribute('data-router') === 'memory';
  var memRoute = null;
  function currentRoute() { return MEM ? (memRoute || '/') : (location.hash.replace(/^#/, '') || '/'); }
  function parseHash() {
    var h = currentRoute();
    var qi = h.indexOf('?'), path = qi >= 0 ? h.slice(0, qi) : h, query = {};
    if (qi >= 0) h.slice(qi + 1).split('&').forEach(function (kv) { var p = kv.split('='); if (p[0]) query[decodeURIComponent(p[0])] = decodeURIComponent(p[1] || ''); });
    if (path.length > 1) path = path.replace(/\/$/, '');
    return { path: path, query: query };
  }
  function match(path) {
    var exact = REG.SCREENS.filter(function (s) { return s.route === path; })[0];
    if (exact) return { sc: exact, params: {} };
    for (var i = 0; i < REG.SCREENS.length; i++) {
      var s = REG.SCREENS[i];
      if (s.route.indexOf(':') < 0) continue;
      var a = s.route.split('/'), b = path.split('/');
      if (a.length !== b.length) continue;
      var params = {}, ok = true;
      for (var j = 0; j < a.length; j++) {
        if (a[j].charAt(0) === ':') params[a[j].slice(1)] = decodeURIComponent(b[j]);
        else if (a[j] !== b[j]) { ok = false; break; }
      }
      if (ok) return { sc: s, params: params };
    }
    return null;
  }
  function go(path) {
    if (MEM) { memRoute = path; render(); return; }
    if (location.hash === '#' + path) render(); else location.hash = '#' + path;
  }
  // Reemplaza la ruta actual sin volver a renderizar (equivale a history.replaceState)
  function replaceRoute(path) { if (MEM) { memRoute = path; lastPath = path; } else { history.replaceState(null, '', '#' + path); } }

  // ---------- Utilidades de render ----------
  function roleLabel() { return REG.ROLE_LABEL[S.role]; }
  function userInfo() { return REG.USERS_BY_ROLE[S.role] || ['Usuario', 'U']; }
  function visibleNav() {
    return REG.NAV.map(function (g) {
      var items = g.items.map(function (id) { return REG.BY_ID[id]; }).filter(function (sc) {
        return REG.canAccess(sc, S.role) && (!sc.module || S.modules[sc.module]);
      });
      return { g: g, items: items };
    }).filter(function (x) { return x.items.length; });
  }
  function activeNavId(sc) { return sc.navParent || sc.id; }

  function sidebar(sc) {
    var active = activeNavId(sc);
    var groups = visibleNav();
    var navHtml = groups.map(function (x) {
      var g = x.g, hasActive = x.items.some(function (i) { return i.id === active; });
      if (g.flat && x.items.length === 1) {
        var it = x.items[0];
        return html`<div class="nav-section"><a class="nav-item" href="#${it.route}" ${it.id === active ? raw('aria-current="page"') : ''} title="${S.collapsed ? g.label : ''}">${icon(g.icon, 18)}<span class="nav-label">${REG.NAV_LABEL[it.id] || it.title}</span></a></div>`;
      }
      var open = S.openGroups[g.id] !== undefined ? S.openGroups[g.id] : hasActive;
      return html`<div class="nav-section">
        <button type="button" class="nav-group-btn ${hasActive ? 'has-active' : ''}" aria-expanded="${S.collapsed ? 'false' : String(!!open)}" ${S.collapsed ? raw('aria-haspopup="true"') : raw('aria-controls="sub-' + g.id + '"')} data-action="${S.collapsed ? 'flyout' : 'nav-toggle'}" data-group="${g.id}" title="${S.collapsed ? g.label : ''}">
          ${icon(g.icon, 18)}<span class="nav-label">${g.label}</span>${icon('chevRight', 14, 'chev')}
        </button>
        ${open && !S.collapsed ? html`<div class="nav-sub" id="sub-${g.id}">${x.items.map(function (it) {
          return html`<a class="nav-item" href="#${it.route}" ${it.id === active ? raw('aria-current="page"') : ''}><span class="nav-label">${REG.NAV_LABEL[it.id] || it.title}</span>${it.dev === 'planificado' ? html`<span class="nav-badge" title="Planificado">Nuevo</span>` : ''}</a>`;
        })}</div>` : ''}
      </div>`;
    });
    var flyout = '';
    if (S.collapsed && S.flyout) {
      var fg = groups.filter(function (x) { return x.g.id === S.flyout.id; })[0];
      if (fg) flyout = html`<div class="nav-flyout" role="menu" aria-label="${fg.g.label}" style="left:68px;top:${S.flyout.top}px"><h4>${fg.g.label}</h4>${fg.items.map(function (it) {
        return html`<a class="nav-item" role="menuitem" href="#${it.route}" ${it.id === active ? raw('aria-current="page"') : ''} data-action="flyout-close"><span class="nav-label" style="display:inline">${REG.NAV_LABEL[it.id] || it.title}</span></a>`;
      })}</div>`;
    }
    return html`<aside class="sidebar" aria-label="Navegación principal">
      <div class="brand"><span class="brand-mark">${icon('landmark', 16)}</span><span class="brand-name">SII-ERP <span>AI</span></span></div>
      <nav class="nav">${groups.length ? navHtml : html`<p class="muted small" style="padding:8px">Tu rol no tiene áreas asignadas.</p>`}</nav>
      <div class="sidebar-foot">
        <button type="button" class="nav-item" data-action="collapse" aria-label="${S.collapsed ? 'Expandir menú' : 'Contraer menú'}">${icon(S.collapsed ? 'chevsRight' : 'chevsLeft', 18)}<span class="nav-label">Contraer menú</span></button>
      </div>
    </aside>${flyout}`;
  }

  function header() {
    var u = userInfo();
    var unread = D.notifications.length;
    return html`<header class="header">
      <button type="button" class="icon-btn only-mobile" aria-label="Abrir menú" data-action="mobile-menu">${icon('menu', 20)}</button>
      <div class="company"><strong>${D.company.name}</strong><span>RUT ${D.company.rut}</span></div>
      <span class="env-pill ${S.env === 'cert' ? 'env-cert' : 'env-prod'}" title="${S.env === 'cert' ? 'Los documentos se envían al ambiente de pruebas del SII y no tienen validez tributaria.' : 'Los documentos enviados tienen validez tributaria y consumen folios reales.'}">${icon(S.env === 'cert' ? 'shield' : 'alert', 13)}${S.env === 'cert' ? 'SII: certificación' : 'SII: producción'}</span>
      <span class="header-spacer"></span>
      <button type="button" class="search-btn" data-action="cmd-open" aria-keyshortcuts="Control+K">${icon('search', 15)}<span class="lbl">Buscar pantallas y acciones</span><kbd>Ctrl K</kbd></button>
      <button type="button" class="btn btn-outline btn-sm hide-mobile" data-action="ai-open">${icon('sparkles', 15)}Asistente</button>
      <div class="rel">
        <button type="button" class="icon-btn" aria-label="Notificaciones (${unread} sin leer)" aria-expanded="${S.pop === 'notif'}" data-action="pop" data-pop="notif">${icon('bell', 18)}${unread ? html`<span class="dot"></span>` : ''}</button>
        ${S.pop === 'notif' ? html`<div class="menu-pop" role="dialog" aria-label="Notificaciones">${D.notifications.map(function (n) {
          return html`<a class="mi" href="${n.link}" data-action="pop-close"><span class="badge t-${n.tone}" style="height:28px;width:28px;padding:0;justify-content:center">${icon(n.icon, 14)}</span><span><strong style="display:block">${n.title}</strong><span class="small muted">${n.body}</span></span></a>`;
        })}<div style="border-top:1px solid var(--border);margin-top:4px;padding-top:4px"><a class="mi" href="#/notifications" data-action="pop-close">Ver todas las notificaciones</a></div></div>` : ''}
      </div>
      <div class="rel">
        <button type="button" class="icon-btn" style="width:auto;padding:0 4px;gap:8px;display:flex" aria-label="${u[1]}, menú de usuario de ${u[0]}, ${roleLabel()}" aria-expanded="${S.pop === 'user'}" data-action="pop" data-pop="user"><span class="avatar">${u[1]}</span></button>
        ${S.pop === 'user' ? html`<div class="menu-pop" style="min-width:240px" role="menu">
          <div style="padding:8px 10px"><strong style="display:block">${u[0]}</strong><span class="small muted">${roleLabel()}</span></div>
          <a class="mi" role="menuitem" href="#/employee" data-action="pop-close">${icon('user', 16)}Ver mi portal de empleado</a>
          <a class="mi" role="menuitem" href="#/mapa" data-action="pop-close">${icon('map', 16)}Mapa del prototipo</a>
          <a class="mi" role="menuitem" href="#/login" data-action="pop-close">${icon('logout', 16)}Cerrar sesión</a>
        </div>` : ''}
      </div>
    </header>`;
  }

  function ctrlbar() {
    return html`<div class="ctrlbar" role="region" aria-label="Controles del prototipo">
      <span class="cb-title">Prototipo</span>
      <label>Rol <select data-action-change="set-role" aria-label="Rol">
        <optgroup label="Roles del backend">${REG.ROLES.map(function (r) { return html`<option value="${r[0]}" ${S.role === r[0] ? raw('selected') : ''}>${r[1]}</option>`; })}</optgroup>
        <optgroup label="Portales">${REG.PERSONAS.map(function (r) { return html`<option value="${r[0]}" ${S.role === r[0] ? raw('selected') : ''}>${r[1]}</option>`; })}</optgroup>
      </select></label>
      <div class="rel">
        <button type="button" data-action="pop" data-pop="mods" aria-expanded="${S.pop === 'mods'}">${icon('layers', 13)} Módulos (${REG.MODULES.filter(function (m) { return S.modules[m[0]]; }).length}/${REG.MODULES.length})</button>
        ${S.pop === 'mods' ? html`<div class="cb-pop" role="dialog" aria-label="Módulos por rubro"><strong>Módulos activados para esta empresa</strong><p class="small muted" style="margin:4px 0 8px">Simula la pestaña Módulos de Configuración (propuesta nueva, PLAN §3.3).</p>${REG.MODULES.map(function (m) {
          return html`<label class="check"><input type="checkbox" data-action-change="toggle-module" value="${m[0]}" ${S.modules[m[0]] ? raw('checked') : ''}>${m[1]}</label>`;
        })}</div>` : ''}
      </div>
      <label class="cb-hide">Estado de pantalla <select data-action-change="set-state" aria-label="Estado de pantalla">
        ${[['datos', 'Con datos'], ['vacio', 'Vacío'], ['cargando', 'Cargando'], ['error', 'Error']].map(function (o) { return html`<option value="${o[0]}" ${S.screenState === o[0] ? raw('selected') : ''}>${o[1]}</option>`; })}
      </select></label>
      <button type="button" data-action="theme" aria-pressed="${S.theme === 'dark'}">${icon(S.theme === 'dark' ? 'sun' : 'moon', 13)} ${S.theme === 'dark' ? 'Modo claro' : 'Modo oscuro'}</button>
      <span class="spacer"></span>
      <button type="button" data-action="demo-open">${icon('list', 13)} Guion de la demo</button>
      <a href="#/mapa">${icon('map', 13)} Mapa del prototipo</a>
    </div>`;
  }

  function pageHead(sc, view) {
    var crumbs = view.crumbs || (sc.navParent ? [[REG.BY_ID[sc.navParent].title, '#' + REG.BY_ID[sc.navParent].route]] : []);
    var areaCrumb = sc.area && sc.area !== 'Inicio' && sc.area !== 'Prototipo' && sc.area !== (view.title || sc.title) ? [[sc.area, null]] : [];
    var all = [['Inicio', '#/']].concat(areaCrumb, crumbs);
    return html`<div>
      <nav class="crumbs" aria-label="Ruta de navegación">${all.map(function (c, i) {
        return html`${c[1] ? html`<a href="${c[1]}">${c[0]}</a>` : html`<span>${c[0]}</span>`}${icon('chevRight', 12)}`;
      })}<span aria-current="page">${view.title || sc.title}</span></nav>
      <div class="page-head">
        <div class="titles"><div class="title-row"><h1 tabindex="-1" id="page-title">${view.title || sc.title}</h1>${view.titleExtra || ''}${C.devTag(sc)}</div>${view.subtitle ? html`<p>${view.subtitle}</p>` : ''}</div>
        ${view.actions ? html`<div class="actions">${view.actions}</div>` : ''}
      </div>
    </div>`;
  }

  function stateBody(sc, view) {
    if (sc.stateful === false || S.screenState === 'datos') return null;
    if (S.screenState === 'cargando') return C.loading(view.loadingKind);
    if (S.screenState === 'error') return html`<div class="card">${C.errorState({ endpoint: view.endpoint })}</div>`;
    return html`<div class="card">${C.empty(view.empty || {})}</div>`;
  }

  function guard(sc) {
    if (!REG.canAccess(sc, S.role)) return C.noAccess(sc, roleLabel());
    if (sc.module && !S.modules[sc.module]) return C.moduleOff(sc, REG.effectiveRole(S.role) === 'ADMIN');
    return null;
  }

  function callView(sc, ctx) {
    var fn = V[sc.id];
    if (!fn) return { body: C.empty({ title: 'Vista no implementada', text: sc.id }) };
    var v = fn(ctx) || {};
    if (v.__html !== undefined) v = { body: v };
    return v;
  }

  // ---------- Render principal ----------
  var lastPath = null;
  function render() {
    var r = parseHash();
    var m = match(r.path);
    document.documentElement.setAttribute('data-theme', S.theme);
    var root = document.getElementById('root');
    var focusInfo = captureFocus();
    var routeChanged = lastPath !== currentRoute();
    if (routeChanged) { S.pop = null; S.flyout = null; S.mobileOpen = false; if (m && r.query.tab) S.tabs[m.sc.navParent || m.sc.id] = r.query.tab; }

    if (m && m.sc.layout === 'redirect') { go(REG.homeFor(S.role)); return; }

    var out;
    if (!m) {
      out = shellApp({ id: 'notfound', title: 'Página no encontrada', area: 'Prototipo', stateful: false }, { body: html`<div class="card">${C.empty({ icon: 'search', title: 'Esta dirección no existe', text: 'Revisa el enlace o vuelve al mapa del prototipo.', action: C.btn('Ir al mapa', { variant: 'primary', href: '#/mapa' }) })}</div>` });
    } else {
      var sc = m.sc;
      var ctx = { params: m.params, query: r.query, sc: sc };
      var blocked = guard(sc);
      var view = blocked ? { body: html`<div class="card">${blocked}</div>` } : callView(sc, ctx);
      if (!blocked) { var sb = stateBody(sc, view); if (sb) view.body = sb; }
      if (sc.layout === 'auth') out = shellAuth(sc, view);
      else if (sc.layout === 'employee' || sc.layout === 'b2b') out = shellPortal(sc, view, blocked);
      else out = shellApp(sc, view);
    }
    root.innerHTML = html`<a class="skip-link" href="#main" data-action="skip">Saltar al contenido</a>
      <div class="proto-banner" role="note">${icon('info', 13)}<span>Prototipo — datos ficticios.</span><span class="long">Las etiquetas de estado indican qué funciona hoy en el producto real.</span></div>
      <div class="layout">${out}</div>${ctrlbar()}`.__html;
    renderOverlays();
    if (routeChanged) {
      lastPath = currentRoute();
      var h = document.getElementById('page-title'); if (h) h.focus({ preventScroll: true });
      window.scrollTo(0, 0);
    } else restoreFocus(focusInfo);
  }
  window.App = window.App || {};
  App.render = render; App.go = go; App.replaceRoute = replaceRoute; App.isMemory = MEM;

  function shellApp(sc, view) {
    return html`<div class="app ${S.collapsed ? 'collapsed' : ''} ${S.mobileOpen ? 'mobile-open' : ''}">
      ${sidebar(sc)}
      <div class="main-col">${header()}
        ${S.env === 'prod' && (sc.id === 'invoice-new' || sc.id === 'invoice-detail') ? html`<div style="padding:12px 24px 0">${C.alert('warning', 'Estás emitiendo en producción', 'Los documentos que envíes al SII tienen validez tributaria y consumen folios reales.')}</div>` : ''}
        <main class="content" id="main">${pageHead(sc, view)}${view.body}</main>
      </div>
    </div>`;
  }
  function shellAuth(sc, view) {
    return html`<main class="auth-wrap" id="main"><div class="auth-card ${view.wide ? 'wide' : ''}">
      <div class="row" style="justify-content:space-between;margin-bottom:16px"><span class="row" style="gap:10px"><span class="brand-mark">${icon('landmark', 16)}</span><strong style="font-size:16px">SII-ERP AI</strong></span>${C.devTag(sc)}</div>
      ${view.body}</div></main>`;
  }
  function shellPortal(sc, view, blocked) {
    var isEmp = sc.layout === 'employee';
    var u = userInfo();
    var tab = S.tabs['portal-' + sc.id] || (isEmp ? 'payslips' : 'orders');
    var links = isEmp ? [['payslips', 'Liquidaciones'], ['profile', 'Mis datos']] : [['orders', 'Mis pedidos'], ['new', 'Nuevo pedido']];
    return html`<header class="portal-head">
      <span class="row" style="gap:10px"><span class="brand-mark">${icon(isEmp ? 'user' : 'bag', 16)}</span><strong>${isEmp ? 'Portal del empleado' : 'Portal de clientes'}</strong></span>
      <span class="small muted hide-mobile">${D.company.name}</span>
      <nav aria-label="Secciones del portal">${blocked ? '' : links.map(function (l) { return html`<a href="#${sc.route}" data-action="portal-tab" data-group="portal-${sc.id}" data-tab="${l[0]}" ${tab === l[0] ? raw('aria-current="page"') : ''}>${l[1]}</a>`; })}</nav>
      <span class="header-spacer"></span>${C.devTag(sc)}
      <span class="avatar" aria-hidden="true">${u[1]}</span><span class="hide-mobile">${u[0]}</span>
      ${REG.effectiveRole(S.role) === 'ADMIN' || REG.effectiveRole(S.role) === 'HR' ? C.btn('Volver al ERP', { size: 'sm', href: '#/dashboard', icon: 'arrowLeft' }) : C.btn('Salir', { size: 'sm', href: '#/login', icon: 'logout' })}
    </header>
    <main class="portal-main" id="main"><h1 tabindex="-1" id="page-title" class="${view.hideTitle ? 'sr-only' : ''}" style="margin-bottom:16px">${view.title || sc.title}</h1>${view.body}</main>`;
  }

  // ---------- Overlays: diálogo, cajón, toasts ----------
  var lastFocusBeforeOverlay = null;
  function renderOverlays() {
    var o = document.getElementById('overlay-root');
    var out = '';
    if (S.dialog) {
      var d = S.dialog, body = typeof d.body === 'function' ? d.body() : d.body;
      out += html`<div class="dialog-back" data-action="dialog-backdrop"><div class="dialog ${d.wide ? 'wide' : ''}" role="${d.alert ? 'alertdialog' : 'dialog'}" aria-modal="true" aria-labelledby="dlg-title" ${d.desc !== false ? raw('aria-describedby="dlg-body"') : ''}>
        <div class="dialog-head"><h2 id="dlg-title">${d.title}</h2>${C.iconBtn('x', 'Cerrar', 'dialog-close')}</div>
        <div class="dialog-body" id="dlg-body">${body}</div>
        ${d.actions ? html`<div class="dialog-foot">${typeof d.actions === 'function' ? d.actions() : d.actions}</div>` : ''}
      </div></div>`.__html;
    }
    if (S.drawer) {
      var dr = S.drawer, db = typeof dr.body === 'function' ? dr.body() : dr.body;
      out += html`<div class="drawer-back" data-action="drawer-close"></div><aside class="drawer" role="dialog" aria-modal="true" aria-labelledby="drw-title">
        <div class="drawer-head">${dr.icon ? icon(dr.icon, 18) : ''}<h2 id="drw-title">${dr.title}</h2>${C.iconBtn('x', 'Cerrar', 'drawer-close')}</div>
        <div class="drawer-body">${db}</div>${dr.foot ? html`<div class="drawer-foot">${typeof dr.foot === 'function' ? dr.foot() : dr.foot}</div>` : ''}</aside>`.__html;
    }
    o.innerHTML = out;
  }
  App.dialog = function (cfg) {
    if (!S.dialog && !S.drawer) lastFocusBeforeOverlay = document.activeElement;
    S.dialog = cfg; renderOverlays(); focusFirst('.dialog');
  };
  App.closeDialog = function () { S.dialog = null; renderOverlays(); restoreOverlayFocus(); };
  App.drawer = function (cfg) {
    if (!S.dialog && !S.drawer) lastFocusBeforeOverlay = document.activeElement;
    S.drawer = cfg; renderOverlays(); focusFirst('.drawer');
  };
  App.closeDrawer = function () { S.drawer = null; renderOverlays(); restoreOverlayFocus(); };
  App.refreshOverlay = function () { var f = captureFocus(); renderOverlays(); restoreFocus(f); };
  function focusFirst(sel) {
    var c = document.querySelector(sel); if (!c) return;
    var el = c.querySelector('[autofocus]') || c.querySelector('.dialog-body input, .dialog-body select, .dialog-body textarea, .drawer-body input, .drawer-body textarea') || c.querySelector('.dialog-foot .btn-primary, .dialog-foot .btn-destructive') || c.querySelector('button, a[href], input, select, textarea');
    if (el) el.focus();
  }
  function restoreOverlayFocus() {
    if (lastFocusBeforeOverlay && document.body.contains(lastFocusBeforeOverlay)) lastFocusBeforeOverlay.focus();
    else { var h = document.getElementById('page-title'); if (h) h.focus(); }
    lastFocusBeforeOverlay = null;
  }
  App.toast = function (text, tone) {
    var t = document.getElementById('toasts');
    var el = document.createElement('div');
    el.className = 'toast ' + (tone === 'bad' ? 't-bad' : tone === 'info' ? '' : 't-ok');
    el.setAttribute('role', 'status');
    el.innerHTML = html`${icon(tone === 'bad' ? 'xCircle' : tone === 'info' ? 'info' : 'checkCircle', 18)}<div>${text}</div>`.__html;
    t.appendChild(el);
    setTimeout(function () { if (el.parentNode) el.parentNode.removeChild(el); }, 4200);
  };
  // Confirmación de acciones irreversibles (ConfirmDialog)
  App.confirm = function (o) {
    App.dialog({
      title: o.title, alert: true,
      body: html`<p>${o.text}</p>${o.extra || ''}`,
      actions: html`${C.btn(o.cancel || 'Cancelar', { variant: 'outline', action: 'dialog-close' })}${C.btn(o.confirm, { variant: o.destructive ? 'destructive' : 'primary', action: 'confirm-ok', icon: o.icon })}`
    });
    S.ui.confirmFn = o.onConfirm;
  };
  // Simula una espera (carga) y luego ejecuta fn
  App.busy = function (key, ms, fn) { S.ui['busy_' + key] = true; render(); setTimeout(function () { S.ui['busy_' + key] = false; fn(); render(); }, ms); };
  App.isBusy = function (key) { return !!S.ui['busy_' + key]; };

  // ---------- Foco al re-renderizar ----------
  function captureFocus() {
    var a = document.activeElement;
    if (!a || a === document.body) return null;
    var info = { id: a.id, action: a.getAttribute('data-action'), key: a.getAttribute('data-key') || a.getAttribute('data-tab') || a.getAttribute('data-page') || a.getAttribute('data-id') };
    try { if (a.selectionStart != null) { info.s = a.selectionStart; info.e = a.selectionEnd; } } catch (e) { /* no aplica */ }
    return info;
  }
  function restoreFocus(info) {
    if (!info) return;
    var el = info.id ? document.getElementById(info.id) : null;
    if (!el && info.action) {
      var sel = '[data-action="' + info.action + '"]';
      var cands = document.querySelectorAll(sel);
      for (var i = 0; i < cands.length; i++) {
        var c = cands[i], k = c.getAttribute('data-key') || c.getAttribute('data-tab') || c.getAttribute('data-page') || c.getAttribute('data-id');
        if (k === info.key) { el = c; break; }
      }
    }
    if (el) { el.focus({ preventScroll: true }); try { if (info.s != null && el.setSelectionRange) el.setSelectionRange(info.s, info.e); } catch (e) { /* no aplica */ } }
  }

  // ---------- Enlace de datos genérico ----------
  function setPath(obj, path, val) {
    var p = path.split('.'), o = obj;
    for (var i = 0; i < p.length - 1; i++) { if (o[p[i]] == null) o[p[i]] = /^\d+$/.test(p[i + 1]) ? [] : {}; o = o[p[i]]; }
    o[p[p.length - 1]] = val;
  }
  function getPath(obj, path) { return path.split('.').reduce(function (o, k) { return o == null ? undefined : o[k]; }, obj); }
  App.get = function (path) { return getPath(S, path); };
  App.set = function (path, v) { setPath(S, path, v); };
  function readValue(el) {
    var t = el.getAttribute('data-type');
    if (el.type === 'checkbox') return el.checked;
    if (t === 'int') return parseInt(el.value.replace(/[^0-9-]/g, ''), 10) || 0;
    if (t === 'money') return U.parseMoney(el.value);
    return el.value;
  }

  // ---------- Acciones comunes ----------
  A['nav-toggle'] = function (el) { var g = el.getAttribute('data-group'); S.openGroups[g] = el.getAttribute('aria-expanded') !== 'true'; persist(); render(); };
  A.collapse = function () { S.collapsed = !S.collapsed; S.flyout = null; persist(); render(); };
  A.flyout = function (el) { var g = el.getAttribute('data-group'); var r = el.getBoundingClientRect(); S.flyout = S.flyout && S.flyout.id === g ? null : { id: g, top: Math.min(r.top, window.innerHeight - 320) }; render(); var f = document.querySelector('.nav-flyout a'); if (f) f.focus(); };
  A['flyout-close'] = function () { S.flyout = null; };
  A['mobile-menu'] = function () { S.mobileOpen = !S.mobileOpen; render(); };
  A.pop = function (el) { var p = el.getAttribute('data-pop'); S.pop = S.pop === p ? null : p; render(); };
  A['pop-close'] = function () { S.pop = null; };
  A.theme = function () { S.theme = S.theme === 'dark' ? 'light' : 'dark'; persist(); render(); };
  A.tab = function (el) { S.tabs[el.getAttribute('data-group')] = el.getAttribute('data-tab'); render(); };
  A['portal-tab'] = function (el, ev) { ev.preventDefault(); S.tabs[el.getAttribute('data-group')] = el.getAttribute('data-tab'); render(); };
  A['dt-sort'] = function (el) { var id = el.getAttribute('data-dt'), k = el.getAttribute('data-key'); var t = S.tables[id] = S.tables[id] || {}; t.dir = t.sort === k && t.dir !== 'desc' ? 'desc' : 'asc'; t.sort = k; render(); };
  A['dt-page'] = function (el) { var id = el.getAttribute('data-dt'); (S.tables[id] = S.tables[id] || {}).page = parseInt(el.getAttribute('data-page'), 10); render(); };
  A['dt-clear'] = function (el) { S.tables[el.getAttribute('data-dt')] = {}; render(); };
  A.retry = function () { S.screenState = 'datos'; persist(); render(); App.toast('Datos cargados.'); };
  A['dialog-close'] = function () { App.closeDialog(); };
  A['dialog-backdrop'] = function (el, ev) { if (ev.target === el) App.closeDialog(); };
  A['drawer-close'] = function () { App.closeDrawer(); };
  A['confirm-ok'] = function () { var fn = S.ui.confirmFn; S.ui.confirmFn = null; App.closeDialog(); if (fn) fn(); };
  A.skip = function (el, ev) { ev.preventDefault(); var m = document.getElementById('main'); var h = document.getElementById('page-title'); (h || m).focus(); };
  A.go = function (el) { go(el.getAttribute('data-href')); };
  A.noop = function () { };
  A['toast'] = function (el) { App.toast(el.getAttribute('data-msg'), el.getAttribute('data-tone')); };

  CH['set-role'] = function (el) { S.role = el.value; S.pop = null; persist(); var r = parseHash(); var m = match(r.path); if (!m || !REG.canAccess(m.sc, S.role)) go(REG.homeFor(S.role)); else render(); App.toast('Rol cambiado a ' + roleLabel() + '. El menú muestra solo sus áreas.', 'info'); };
  CH['set-state'] = function (el) { S.screenState = el.value; persist(); render(); };
  CH['toggle-module'] = function (el) { S.modules[el.value] = el.checked; persist(); render(); };

  // ---------- Guion de la demo ----------
  var DEMO = [
    ['Panel por rol', 'Vencimientos tributarios, folios, documentos por estado y actividad.', '/dashboard', 'ADMIN'],
    ['Menú por rol', 'Cambia el rol a Ventas en la barra inferior: el menú muestra solo sus áreas. Vuelve a Administrador.', '/dashboard', null],
    ['Emitir factura', 'RUT con validación del dígito verificador, IVA 19% en vivo, vista previa, firma y envío al SII.', '/invoices/new?new=1', 'ADMIN'],
    ['Seguimiento en el SII', 'En el detalle, simula la respuesta: aceptado, con reparos o rechazado, y corrige.', '/invoices/f1024', 'ADMIN'],
    ['Folios (CAF)', 'Medidor por tipo de documento, alerta de folios bajos y carga de un CAF.', '/sii/caf', 'ADMIN'],
    ['Formulario 29', 'Revisar códigos y diferencias con el RCV, confirmar y declarar.', '/accounting/f29', 'ADMIN'],
    ['Banco y conciliación', 'Conecta un banco y concilia con sugerencias.', '/banking', 'ADMIN'],
    ['Cierre mensual con IA', 'Pendientes, sugerencias de la IA que se aceptan o descartan, y cierre.', '/accounting/close', 'ADMIN'],
    ['Portal del empleado', 'Layout propio: liquidaciones y datos personales.', '/employee', 'EMPLOYEE'],
    ['Portal del cliente B2B', 'Layout propio: pedidos, nuevo pedido y pago.', '/portal', 'B2B_PORTAL'],
    ['Estado real del producto', 'Mapa con las 43 pantallas y su estado: disponible, parcial, en desarrollo o planificado.', '/mapa', 'ADMIN'],
    ['Modo oscuro y estados', 'En la barra inferior: modo oscuro y estados vacío, cargando y error.', '/invoices', 'ADMIN']
  ];
  A['demo-open'] = function () {
    App.drawer({ title: 'Guion de la demo', icon: 'list', body: function () {
      return html`<p class="muted">Recorrido sugerido de unos 10 minutos. Cada paso abre la pantalla con el rol adecuado.</p>
        <ol class="timeline" style="margin-top:12px">${DEMO.map(function (d, i) { return html`<li><span class="tl-ico badge t-brand" style="padding:0;justify-content:center">${i + 1}</span><div><strong>${d[0]}</strong><div class="muted small">${d[1]}</div><div style="margin-top:6px">${C.btn('Ir', { size: 'sm', action: 'demo-go', attrs: 'data-i="' + i + '"' })}</div></div></li>`; })}</ol>`;
    } });
  };
  A['demo-go'] = function (el) {
    var d = DEMO[Number(el.getAttribute('data-i'))];
    if (d[3] && S.role !== d[3]) { S.role = d[3]; persist(); }
    S.drawer = null; renderOverlays();
    go(d[2]);
  };

  // ---------- Paleta de comandos (Ctrl+K) ----------
  A['cmd-open'] = function () {
    S.ui.cmdQ = ''; S.ui.cmdSel = 0;
    App.dialog({ title: 'Ir a…', desc: false, body: cmdBody });
  };
  function cmdItems() {
    var q = (S.ui.cmdQ || '').toLowerCase();
    return REG.SCREENS.filter(function (s) {
      return s.route.indexOf(':') < 0 && s.layout !== 'redirect' && REG.canAccess(s, S.role) && (!s.module || S.modules[s.module]) && (!q || (s.title + ' ' + s.area).toLowerCase().indexOf(q) >= 0);
    }).slice(0, 12);
  }
  function cmdBody() {
    var items = cmdItems();
    return html`<label class="sr-only" for="cmd-q">Buscar pantalla</label><input class="input" id="cmd-q" type="search" placeholder="Escribe el nombre de una pantalla" value="${S.ui.cmdQ || ''}" data-cmd="1" autocomplete="off" autofocus aria-describedby="cmd-help">
      <ul class="cmd-list" id="cmd-list" aria-label="Resultados">${items.map(function (s, i) { return html`<li><a href="#${s.route}" class="${i === S.ui.cmdSel ? 'sel' : ''}" ${i === S.ui.cmdSel ? raw('aria-current="true"') : ''} data-action="dialog-close">${s.title}<span class="cmd-area">${s.area}</span></a></li>`; })}</ul>
      ${items.length ? '' : html`<p class="muted" style="padding:12px 4px">No hay pantallas con ese nombre para tu rol.</p>`}
      <p class="small muted" id="cmd-help" style="margin-top:8px">${icon('keyboard', 13)} Usa las flechas y Enter para ir a la pantalla marcada. Esc para cerrar.</p><p class="sr-only" aria-live="polite">${items.length} resultados${items[S.ui.cmdSel || 0] ? '. Marcada: ' + items[S.ui.cmdSel || 0].title : ''}</p>`;
  }

  // ---------- Asistente IA (panel lateral) ----------
  A['ai-open'] = function () {
    S.ui.chat = S.ui.chat || [{ who: 'bot', text: 'Hola. Puedo responder sobre tus documentos, impuestos y saldos. Indico siempre en qué datos me baso.', src: null }];
    App.drawer({ title: 'Asistente', icon: 'sparkles', body: chatBody, foot: chatFoot });
  };
  function chatBody() {
    return html`<div class="stack-sm">
      ${C.alert('info', 'Qué datos se envían', 'Para responder, el asistente envía al proveedor de IA el RUT del emisor y del receptor, la descripción, el monto y la fecha de los documentos consultados (RIESGOS R-13). Hoy cualquier rol puede usarlo.')}
      <div class="stack-sm" style="margin-top:8px">${S.ui.chat.map(function (m) { return html`<div class="chat-msg ${m.who === 'me' ? 'me' : 'bot'}">${m.text}${m.src ? html`<div class="chat-src">Fuente: ${m.src}</div>` : ''}</div>`; })}</div>
      ${S.ui.chat.length < 3 ? html`<div class="stack-sm" style="margin-top:8px"><span class="small muted">Preguntas frecuentes</span>${['¿Cuánto IVA debo pagar en octubre?', '¿Qué facturas siguen sin respuesta del SII?', '¿Cuántos folios me quedan?'].map(function (q) { return html`<button type="button" class="btn btn-outline btn-sm" style="justify-content:flex-start" data-action="ai-ask" data-q="${q}">${q}</button>`; })}</div>` : ''}
    </div>`;
  }
  function chatFoot() {
    return html`<form class="row" data-action-submit="ai-send" style="flex-wrap:nowrap"><label class="sr-only" for="ai-q">Mensaje para el asistente</label><input class="input" id="ai-q" placeholder="Escribe tu pregunta" autocomplete="off">${C.btn('Enviar', { variant: 'primary', type: 'submit', icon: 'send' })}</form>`;
  }
  var AI_ANS = {
    '¿Cuánto IVA debo pagar en octubre?': ['Según la vista previa del F29 de octubre 2026, el IVA determinado es $5.630.193 (débito $9.842.023 menos crédito $4.211.830). La declaración vence el 20-11-2026.', 'Formulario 29, octubre 2026'],
    '¿Qué facturas siguen sin respuesta del SII?': ['La factura N° 1.024 a Constructora Mar de Fondo fue enviada el 14-11-2026 y sigue sin respuesta. Hoy el sistema no consulta el estado automáticamente.', 'Facturas y documentos, estado "Enviado al SII"'],
    '¿Cuántos folios me quedan?': ['Quedan 76 folios de factura electrónica (tipo 33). Al ritmo de octubre se agotan en unos 9 días. No hay CAF cargado para guías de despacho (tipo 52).', 'Folios (CAF)']
  };
  A['ai-ask'] = function (el) { askAi(el.getAttribute('data-q')); };
  function askAi(q) {
    S.ui.chat.push({ who: 'me', text: q });
    var a = AI_ANS[q] || ['En este prototipo solo respondo las preguntas de ejemplo. En el producto, la respuesta citaría los documentos usados.', null];
    S.ui.chat.push({ who: 'bot', text: a[0], src: a[1] });
    App.refreshOverlay();
    var i = document.getElementById('ai-q'); if (i) i.focus();
  }

  // ---------- Eventos ----------
  document.addEventListener('click', function (ev) {
    var t = ev.target;
    if (S.pop && !t.closest('.rel') && !t.closest('.cb-pop')) { S.pop = null; render(); }
    if (S.flyout && !t.closest('.nav-flyout') && !t.closest('[data-action="flyout"]')) { S.flyout = null; render(); }
    var el = t.closest('[data-action]');
    var link = t.closest('a[href^="#/"]');
    if (el && !el.disabled) {
      var name = el.getAttribute('data-action');
      if (A[name]) {
        if (el.tagName === 'BUTTON' || el.getAttribute('href') === '#') ev.preventDefault();
        A[name](el, ev);
        if (MEM && link && !ev.defaultPrevented) { ev.preventDefault(); go(link.getAttribute('href').slice(1)); }
        return;
      }
    }
    if (MEM && link) { ev.preventDefault(); go(link.getAttribute('href').slice(1)); return; }
    var tr = t.closest('tr[data-href]');
    if (tr && !t.closest('a,button,input,select,label')) { go(tr.getAttribute('data-href').replace(/^#/, '')); }
  });
  document.addEventListener('input', function (ev) {
    var el = ev.target;
    if (el.getAttribute('data-dt')) {
      var id = el.getAttribute('data-dt'), f = el.getAttribute('data-dt-field');
      var tb = S.tables[id] = S.tables[id] || {}; tb[f] = el.value; tb.page = 1; render(); return;
    }
    if (el.getAttribute('data-cmd')) { S.ui.cmdQ = el.value; S.ui.cmdSel = 0; App.refreshOverlay(); return; }
    if (el.getAttribute('data-money')) {
      var n = U.parseMoney(el.value); el.value = n ? U.int(n) : '';
    }
    if (el.getAttribute('data-bind') && el.getAttribute('data-live')) {
      setPath(S, el.getAttribute('data-bind'), readValue(el));
      if (el.getAttribute('data-rerender') !== null) { if (el.closest('#overlay-root')) App.refreshOverlay(); else render(); }
    }
  });
  document.addEventListener('change', function (ev) {
    var el = ev.target;
    if (el.tagName === 'SELECT' && el.getAttribute('data-dt')) {
      var id = el.getAttribute('data-dt'); var tb = S.tables[id] = S.tables[id] || {}; tb[el.getAttribute('data-dt-field')] = el.value; tb.page = 1; render(); return;
    }
    if (el.getAttribute('data-rut')) { el.value = U.formatRut(el.value); }
    if (el.getAttribute('data-bind')) {
      var nv = readValue(el), path = el.getAttribute('data-bind');
      // Si ya se guardó al escribir (data-live), no re-renderizar: evita perder el clic que provocó el blur
      if (el.getAttribute('data-live') && getPath(S, path) === nv) { var cb = el.getAttribute('data-action-change'); if (cb && CH[cb]) CH[cb](el, ev); return; }
      setPath(S, path, nv);
      if (el.getAttribute('data-rerender') !== null) { if (el.closest('#overlay-root')) App.refreshOverlay(); else render(); }
    }
    var ch = el.getAttribute('data-action-change');
    if (ch && CH[ch]) CH[ch](el, ev);
  });
  document.addEventListener('submit', function (ev) {
    var f = ev.target, a = f.getAttribute('data-action-submit');
    if (!a) return;
    ev.preventDefault();
    if (a === 'ai-send') { var i = document.getElementById('ai-q'); if (i && i.value.trim()) askAi(i.value.trim()); return; }
    if (A[a]) A[a](f, ev);
  });
  document.addEventListener('keydown', function (ev) {
    if ((ev.ctrlKey || ev.metaKey) && (ev.key === 'k' || ev.key === 'K')) { ev.preventDefault(); A['cmd-open'](); return; }
    if (ev.key === 'Escape') {
      if (S.dialog) { App.closeDialog(); return; }
      if (S.drawer) { App.closeDrawer(); return; }
      if (S.pop || S.flyout || S.mobileOpen) { S.pop = null; S.flyout = null; S.mobileOpen = false; render(); return; }
    }
    // Navegación por teclado en la paleta de comandos
    if (S.dialog && ev.target.getAttribute && ev.target.getAttribute('data-cmd')) {
      var items = cmdItems();
      if (ev.key === 'ArrowDown') { ev.preventDefault(); S.ui.cmdSel = Math.min(items.length - 1, (S.ui.cmdSel || 0) + 1); App.refreshOverlay(); }
      if (ev.key === 'ArrowUp') { ev.preventDefault(); S.ui.cmdSel = Math.max(0, (S.ui.cmdSel || 0) - 1); App.refreshOverlay(); }
      if (ev.key === 'Enter' && items[S.ui.cmdSel || 0]) { ev.preventDefault(); App.closeDialog(); go(items[S.ui.cmdSel || 0].route); }
    }
    // Pestañas con flechas
    if ((ev.key === 'ArrowRight' || ev.key === 'ArrowLeft') && ev.target.getAttribute && ev.target.getAttribute('role') === 'tab') {
      var tabsEls = Array.prototype.slice.call(ev.target.parentNode.querySelectorAll('[role="tab"]'));
      var i = tabsEls.indexOf(ev.target) + (ev.key === 'ArrowRight' ? 1 : -1);
      i = (i + tabsEls.length) % tabsEls.length; tabsEls[i].click();
      var again = document.querySelector('[role="tab"][data-tab="' + tabsEls[i].getAttribute('data-tab') + '"]'); if (again) again.focus();
    }
    // Trampa de foco en diálogos y cajones
    if (ev.key === 'Tab' && (S.dialog || S.drawer)) {
      var box = document.querySelector(S.dialog ? '.dialog' : '.drawer'); if (!box) return;
      var f = box.querySelectorAll('button:not([disabled]), a[href], input:not([disabled]), select, textarea, [tabindex]:not([tabindex="-1"])');
      if (!f.length) return;
      var first = f[0], last = f[f.length - 1];
      if (ev.shiftKey && document.activeElement === first) { ev.preventDefault(); last.focus(); }
      else if (!ev.shiftKey && document.activeElement === last) { ev.preventDefault(); first.focus(); }
    }
  });
  if (!MEM) window.addEventListener('hashchange', render);
  window.addEventListener('DOMContentLoaded', render);
})();
