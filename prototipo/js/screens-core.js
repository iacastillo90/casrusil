/* Acceso (login, registro, invitación, onboarding), panel por rol y mapa del prototipo. */
(function () {
  'use strict';
  var U = window.U, C = window.C, D = window.D, REG = window.REG, html = U.html, icon = U.icon, raw = U.raw, V = window.V, A = window.A, CH = window.CH, S = window.S;

  // ======================= Login / registro / invitación =======================
  V.login = function () {
    var w = S.wiz.login = S.wiz.login || { email: 'mfuentes@quillayverde.cl', pass: '', err: null };
    return { body: html`<div class="card card-pad stack"><div><h1 tabindex="-1" id="page-title">Iniciar sesión</h1><p class="muted">Ingresa con tu email de trabajo.</p></div>
      ${w.err ? C.alert('destructive', null, w.err) : ''}
      <form class="stack" data-action-submit="login-go" novalidate>
        ${C.field('Email', C.input('lg-e', w.email, { type: 'email', attrs: 'autocomplete="username" data-bind="wiz.login.email"' }), { id: 'lg-e', req: true })}
        ${C.field('Contraseña', C.input('lg-p', '', { type: 'password', attrs: 'autocomplete="current-password" data-bind="wiz.login.pass"', err: w.err }), { id: 'lg-p', req: true, hint: 'Para el prototipo, cualquier contraseña de 4 caracteres o más.' })}
        ${C.btn('Ingresar', { variant: 'primary', type: 'submit', loading: App.isBusy('login'), loadingLabel: 'Ingresando…' })}
      </form>
      <p class="small muted">¿Tu empresa aún no usa el sistema? <a href="#/register">Crea una cuenta</a>. ¿Te invitaron? <a href="#/invite">Acepta la invitación</a>.</p></div>` };
  };
  A['login-go'] = function () {
    var w = S.wiz.login, p = document.getElementById('lg-p'); w.pass = p ? p.value : '';
    if (w.pass.length < 4) { w.err = 'El email o la contraseña no coinciden. Revisa e intenta de nuevo.'; App.render(); var f = document.getElementById('lg-p'); if (f) f.focus(); return; }
    w.err = null; App.busy('login', 700, function () { App.go(REG.homeFor(S.role)); });
  };
  V.register = function () {
    var w = S.wiz.reg = S.wiz.reg || { rut: '' };
    return { body: html`<div class="card card-pad stack"><div><h1 tabindex="-1" id="page-title">Crear cuenta</h1><p class="muted">Registra tu empresa. Después configuraremos el certificado y el SII.</p></div>
      <form class="stack" data-action-submit="reg-go" novalidate>
        ${C.rutField('rg-r', 'RUT de la empresa', w.rut, { req: true, attrs: 'data-bind="wiz.reg.rut" data-rerender' })}
        ${C.field('Razón social', C.input('rg-n', ''), { id: 'rg-n', req: true })}
        ${C.field('Tu nombre', C.input('rg-u', '', { attrs: 'autocomplete="name"' }), { id: 'rg-u', req: true })}
        ${C.field('Email', C.input('rg-e', '', { type: 'email', attrs: 'autocomplete="email"' }), { id: 'rg-e', req: true })}
        ${C.field('Contraseña', C.input('rg-p', '', { type: 'password', attrs: 'autocomplete="new-password"' }), { id: 'rg-p', req: true, hint: 'Al menos 10 caracteres.' })}
        ${C.btn('Crear cuenta', { variant: 'primary', type: 'submit' })}
      </form><p class="small muted">¿Ya tienes cuenta? <a href="#/login">Inicia sesión</a>.</p></div>` };
  };
  A['reg-go'] = function () { var r = S.wiz.reg; if (!U.validRut(r.rut)) { App.toast('Revisa el RUT de la empresa.', 'bad'); var f = document.getElementById('rg-r'); if (f) f.focus(); return; } App.go('/onboarding'); App.toast('Cuenta creada. Sigamos con la configuración.'); };
  V.invite = function () {
    return { body: html`<div class="card card-pad stack"><div><h1 tabindex="-1" id="page-title">Aceptar invitación</h1><p class="muted">${D.company.name} te invitó como <strong>Auditor</strong>.</p></div>
      ${C.alert('info', 'Planificado', 'El backend valida y registra invitaciones (/sso/invite/validate, /sso/register-invite), pero no hay pantalla.')}
      ${C.field('Email', C.input('in-e', 'rvergara@auditorasur.cl', { readonly: true }), { id: 'in-e' })}${C.field('Tu nombre', C.input('in-n', 'Rosa Vergara Cid'), { id: 'in-n', req: true })}${C.field('Crea una contraseña', C.input('in-p', '', { type: 'password' }), { id: 'in-p', req: true })}
      ${C.btn('Aceptar e ingresar', { variant: 'primary', action: 'inv-accept' })}<p class="small muted">La invitación vence el 22-11-2026.</p></div>` };
  };
  A['inv-accept'] = function () { S.role = 'AUDITOR'; App.go('/dashboard'); App.toast('Bienvenida. Tu rol es Auditor.'); };

  // ======================= Onboarding =======================
  var SUGGEST = { 'Comercio mayorista': ['b2b', 'wms'], 'Comercio minorista': ['pos'], Manufactura: ['mrp', 'wms'], Transporte: ['fleet'], Energía: ['energy', 'fleet', 'sustainability'], Servicios: [] };
  V.onboarding = function () {
    var w = S.wiz.onb = S.wiz.onb || { step: 0, rut: '76.481.205-0', name: D.company.name, giro: D.company.giro, industry: 'Comercio mayorista', cert: false, pass: '', env: 'cert', ai: true, mods: null, errors: {} };
    if (!w.mods) { w.mods = {}; REG.MODULES.forEach(function (m) { w.mods[m[0]] = (SUGGEST[w.industry] || []).indexOf(m[0]) >= 0; }); }
    var steps = [{ label: 'Empresa' }, { label: 'Certificado' }, { label: 'Preferencias' }];
    var body;
    if (w.step === 0) body = html`<div class="stack">
      ${C.rutField('ob-r', 'RUT de la empresa', w.rut, { req: true, attrs: 'data-bind="wiz.onb.rut" data-rerender' })}
      ${C.field('Razón social', C.input('ob-n', w.name, { attrs: 'data-bind="wiz.onb.name"' }), { id: 'ob-n', req: true })}
      ${C.field('Giro', C.input('ob-g', w.giro, { attrs: 'data-bind="wiz.onb.giro"' }), { id: 'ob-g', hint: 'Tal como aparece en el SII.' })}
      ${C.field('Industria', C.select('ob-i', Object.keys(SUGGEST), w.industry, { attrs: 'data-action-change="onb-ind"' }), { id: 'ob-i', hint: 'La usamos para sugerirte módulos.' })}</div>`;
    else if (w.step === 1) body = html`<div class="stack">
      <p class="muted">El certificado digital (.pfx o .p12) permite firmar los documentos que envías al SII. Lo entrega una entidad certificadora a nombre de una persona autorizada.</p>
      ${w.cert ? html`<div class="card card-pad stack-sm"><div class="row-between"><strong>certificado-vrojas.pfx</strong>${C.btn('Cambiar', { size: 'sm', variant: 'ghost', action: 'onb-cert-clear' })}</div>
          <dl style="display:grid;grid-template-columns:auto 1fr;gap:4px 12px;margin:0"><dt class="muted">Titular</dt><dd style="margin:0">${D.company.cert.holder}</dd><dt class="muted">RUT</dt><dd style="margin:0">${U.rut(D.company.cert.holderRut)}</dd><dt class="muted">Vence</dt><dd style="margin:0">${U.date(D.company.cert.validUntil)}</dd></dl></div>
        ${C.field('Contraseña del certificado', C.input('ob-p', w.pass, { type: 'password', attrs: 'data-bind="wiz.onb.pass" autocomplete="off"', err: w.errors.pass }), { id: 'ob-p', req: true, err: w.errors.pass })}
        ${C.alert('info', null, 'El certificado y su contraseña se guardarán cifrados. Hoy el sistema los guarda sin cifrar y no lee el RUT del titular (RIESGOS R-01, R-10).')}`
      : html`<label class="card card-pad" style="display:flex;flex-direction:column;align-items:center;gap:8px;border-style:dashed;cursor:pointer">${icon('key', 24)}<strong>Elegir certificado</strong><span class="small muted">Archivo .pfx o .p12</span><input type="file" accept=".pfx,.p12" class="sr-only" data-action-change="onb-cert"></label>
        <div class="row" style="justify-content:center">${C.btn('Usar certificado de ejemplo', { variant: 'link', action: 'onb-cert-sample' })}</div>
        <p class="small muted">Puedes hacerlo más tarde en Configuración, pero no podrás emitir documentos hasta cargarlo.</p>`}</div>`;
    else body = html`<div class="stack">
      <fieldset style="border:0;padding:0;margin:0" class="stack-sm"><legend class="label" style="margin-bottom:6px">Ambiente del SII</legend>
        <label class="check"><input type="radio" name="ob-env" value="cert" ${w.env === 'cert' ? raw('checked') : ''} data-bind="wiz.onb.env"><span><strong>Certificación (recomendado al empezar)</strong><br><span class="small muted">Para probar. Los documentos no tienen validez tributaria.</span></span></label>
        <label class="check"><input type="radio" name="ob-env" value="prod" ${w.env === 'prod' ? raw('checked') : ''} data-bind="wiz.onb.env"><span><strong>Producción</strong><br><span class="small muted">Documentos con validez tributaria.</span></span></label></fieldset>
      <fieldset style="border:0;padding:0;margin:0" class="stack-sm"><legend class="label" style="margin-bottom:6px">Módulos sugeridos para ${w.industry.toLowerCase()}</legend>
        ${REG.MODULES.map(function (m) { return html`<label class="check"><input type="checkbox" ${w.mods[m[0]] ? raw('checked') : ''} data-bind="wiz.onb.mods.${m[0]}">${m[1]}${(SUGGEST[w.industry] || []).indexOf(m[0]) >= 0 ? html` ${C.badge('Sugerido', 'brand')}` : ''}</label>`; })}</fieldset>
      <label class="check"><input type="checkbox" ${w.ai ? raw('checked') : ''} data-bind="wiz.onb.ai"><span><strong>Activar el asistente IA</strong><br><span class="small muted">Para responder, envía a un proveedor externo RUT, descripción, monto y fecha de los documentos consultados.</span></span></label>
    </div>`;
    if (w.step === 3) return { wide: false, body: html`<div class="card card-pad stack"><h1 tabindex="-1" id="page-title">Todo listo</h1><p class="muted">${w.name} quedó configurada en ambiente de ${w.env === 'cert' ? 'certificación' : 'producción'}. El siguiente paso es cargar tus folios (CAF) para emitir.</p>
      <div class="row">${C.btn('Cargar folios', { href: '#/sii/caf' })}${C.btn('Ir al panel', { variant: 'primary', href: '#/dashboard' })}</div></div>` };
    return { wide: true, body: html`<div class="card card-pad stack"><div><h1 tabindex="-1" id="page-title">Configura tu empresa</h1><p class="muted">Tres pasos. Puedes salir y retomar donde quedaste.</p></div>
      ${C.stepper(steps, w.step)}${body}
      <div class="row-between">${w.step ? C.btn('Atrás', { icon: 'chevLeft', action: 'onb-back' }) : html`<span></span>`}<div class="row">${w.step === 1 && !w.cert ? C.btn('Hacerlo más tarde', { variant: 'ghost', action: 'onb-skip' }) : ''}${C.btn(w.step < 2 ? 'Continuar' : 'Terminar', { variant: 'primary', action: 'onb-next' })}</div></div></div>` };
  };
  CH['onb-ind'] = function (el) { var w = S.wiz.onb; w.industry = el.value; w.mods = null; App.render(); };
  CH['onb-cert'] = function () { S.wiz.onb.cert = true; App.render(); };
  A['onb-cert-sample'] = function () { S.wiz.onb.cert = true; App.render(); };
  A['onb-cert-clear'] = function () { S.wiz.onb.cert = false; App.render(); };
  A['onb-back'] = function () { S.wiz.onb.step--; App.render(); };
  A['onb-skip'] = function () { S.wiz.onb.step = 2; App.render(); };
  A['onb-next'] = function () {
    var w = S.wiz.onb; w.errors = {};
    if (w.step === 0 && !U.validRut(w.rut)) { App.toast('Revisa el RUT de la empresa.', 'bad'); return; }
    if (w.step === 1 && w.cert && (w.pass || '').length < 4) { w.errors.pass = 'Ingresa la contraseña del certificado.'; App.render(); return; }
    if (w.step === 2) { S.env = w.env; REG.MODULES.forEach(function (m) { S.modules[m[0]] = !!w.mods[m[0]]; }); }
    w.step++; App.render();
  };

  // ======================= Panel por rol =======================
  function statusCounts() {
    var c = {}; D.invoices.forEach(function (i) { c[i.status] = (c[i.status] || 0) + 1; }); return c;
  }
  V.dashboard = function () {
    var role = REG.effectiveRole(S.role), u = REG.USERS_BY_ROLE[S.role][0].split(' ')[0];
    var f29 = window.F29, sc = statusCounts(), cs = D.cafs.filter(function (c) { return c.tipo === 33; }).reduce(function (s, c) { return s + (c.to - c.from + 1 - c.used); }, 0);
    var activity = [];
    D.invoices.forEach(function (i) { (i.history || []).forEach(function (h) { activity.push({ when: h[0], text: window.VENTAS.docName(i) + ': ' + h[2], href: '#/invoices/' + i.id, st: h[1] }); }); });
    activity.sort(function (a, b) { return a.when < b.when ? 1 : -1; });
    var docsCard = html`<div class="card"><div class="card-head"><h2>Documentos por estado</h2><span class="spacer"></span><a href="#/invoices" class="small">Ver todos</a></div><div class="card-body stack-sm">${Object.keys(C.DTE).map(function (k) { return html`<div class="row-between">${C.status(k)}<strong>${sc[k] || 0}</strong></div>`; })}</div></div>`;
    var activityCard = html`<div class="card"><div class="card-head"><h2>Actividad reciente</h2><span class="spacer"></span>${C.devTag({ dev: 'parcial', real: 'Con fallas', missing: 'Hoy la actividad reciente llama a GET /dashboard/recent-activity, que no existe. Aquí se arma con el historial de los documentos.', src: 'MAPA-FUNCIONAL.md §2.9, §4' })}</div><ol class="timeline card-body">${activity.slice(0, 5).map(function (a) { var d = C.DTE[a.st] || C.DTE.DRAFT; return html`<li><span class="tl-ico badge ${d.tone}" style="padding:0;justify-content:center">${icon(d.icon, 14)}</span><div><a href="${a.href}">${a.text}</a><div class="small muted">${U.dateTime(a.when)}</div></div></li>`; })}</ol></div>`;
    var deadlines = html`<div class="card"><div class="card-head"><h2>Próximos vencimientos</h2></div><div class="card-body stack-sm">
      ${[['20-11-2026', 'F29 de octubre', U.clp(f29.total), '#/accounting/f29'], ['13-12-2026', 'Cotizaciones previsionales (Previred)', U.clp(1642300), '#/payroll'], ['08-01-2027', 'Vence el certificado digital', '', '#/settings?tab=cert']].map(function (d) { return html`<a href="${d[3]}" class="deadline"><span class="row" style="gap:10px;flex-wrap:nowrap">${icon('calendar', 16)}<span><strong style="display:block">${d[1]}</strong><span class="small muted">${d[0]}</span></span></span><span class="money">${d[2]}</span></a>`; })}</div></div>`;
    var body;
    if (role === 'ADMIN' || role === 'ACCOUNTANT') body = html`<div class="stack">
      <div class="grid g4">${C.kpi('Ventas de octubre', U.clp(51800120), '+17% vs septiembre', { tone: 'up', ic: 'trending' })}${C.kpi('F29 de octubre a pagar', U.clp(f29.total), 'Vence el 20-11-2026', { ic: 'calendar' })}${C.kpi('Por cobrar', U.clp(18240600), '9 documentos')}${C.kpi('Folios de factura', U.int(cs), 'Alcanzan para unos 9 días', { tone: cs < 100 ? 'down' : '', ic: 'layers' })}</div>
      <div class="grid g-main"><div class="stack"><div class="card"><div class="card-head"><h2>Ventas y costos por mes</h2></div><div class="card-body">${C.barChart(D.salesSeries, [{ key: 'v', label: 'Ventas netas', color: '--chart-1' }, { key: 'c', label: 'Costos', color: '--chart-3' }], { unit: ' M', title: 'Ventas netas y costos por mes, en millones de pesos' })}</div></div>${activityCard}</div>
        <div class="stack">${deadlines}${docsCard}<div class="card card-pad stack-sm"><div class="row-between"><h3>Salud financiera</h3>${C.badge('Buena', 'success', 'shield')}</div><p class="muted">Liquidez de 3,15 veces y deuda de corto plazo bajo control.</p><a href="#/treasury" class="small">Ver flujo de caja</a></div></div></div></div>`;
    else if (role === 'SALES') body = html`<div class="stack"><div class="grid g4">${C.kpi('Ventas de noviembre', U.clp(23400000))}${C.kpi('Cotizaciones abiertas', String(D.quotes.filter(function (q) { return q.status === 'Enviada'; }).length))}${C.kpi('Oportunidades en curso', String(D.leads.filter(function (l) { return l.stage !== 'Ganado'; }).length))}${C.kpi('Documentos rechazados', String(sc.REJECTED || 0), 'Requieren corrección', { tone: 'down' })}</div>
      <div class="grid g-main">${activityCard}${docsCard}</div></div>`;
    else if (role === 'TREASURER') body = html`<div class="stack"><div class="grid g4">${C.kpi('Saldo en bancos', U.clp(61105750))}${C.kpi('Por cobrar a 30 días', U.clp(18240600))}${C.kpi('Por pagar a 30 días', U.clp(11870300))}${C.kpi('Movimientos sin conciliar', '2', null, { ic: 'merge' })}</div>
      <div class="grid g2">${C.alert('warning', 'Banco Litoral requiere reautorización', 'Sus movimientos no se sincronizan desde el 14-11-2026.', C.btn('Ir a Bancos', { size: 'sm', href: '#/banking' }))}${C.alert('info', '2 movimientos por conciliar', 'Una comisión y un depósito en efectivo.', C.btn('Conciliar', { size: 'sm', href: '#/banking/reconciliation' }))}</div></div>`;
    else {
      var cards = {
        OPERATIONS: [C.kpi('Productos bajo el mínimo', String(D.products.filter(function (p) { return p.stock < p.min; }).length), null, { tone: 'down', ic: 'alert' }), C.kpi('Órdenes de compra en camino', '1'), C.kpi('Picking pendiente', '2'), C.kpi('Cargadores con falla', '1')],
        HR: [C.kpi('Trabajadores', String(D.employees.length)), C.kpi('Líquido de octubre', U.clp(D.employees.reduce(function (s, e) { return s + e.liquido; }, 0))), C.kpi('Previred vence', '13-12-2026'), C.kpi('Liquidaciones por emitir', '0', null, { tone: 'up' })],
        AUDITOR: [C.kpi('Alertas abiertas', String(D.auditAlerts.length), null, { ic: 'alert' }), C.kpi('Diferencias con el SII', '3', 'Compras de octubre'), C.kpi('Documentos rechazados', String(sc.REJECTED || 0)), C.kpi('Último cierre', 'Septiembre 2026')]
      }[role] || [];
      body = html`<div class="stack">${C.alert('info', 'Panel para tu rol: propuesta nueva', 'Hoy el panel solo tiene datos para Administración, Contabilidad, Ventas y Tesorería (PLAN §3.2, decisión D-09).')}<div class="grid g4">${cards}</div></div>`;
    }
    return { title: 'Hola, ' + u, subtitle: 'Resumen de ' + D.company.name + ' al ' + U.date(D.TODAY) + '.', endpoint: '/dashboard/finance/metrics', body: body };
  };

  // ======================= Mapa del prototipo =======================
  V.mapa = function () {
    var list = REG.SCREENS.filter(function (s) { return s.dev; });
    var counts = {}; list.forEach(function (s) { counts[s.dev] = (counts[s.dev] || 0) + 1; });
    var inMapa = list.filter(function (s) { return /^§/.test(s.mapa || ''); });
    var areas = [];
    list.forEach(function (s) { if (areas.indexOf(s.area) < 0) areas.push(s.area); });
    var role = S.role;
    var backend = [
      ['POST /invoices (crear factura)', 'invoice-new'], ['/accounting/accounts, /ledger/{cuenta}, /opening-balance', 'accounts'], ['/periods/closed, /periods/{p}/status', 'close'],
      ['/reports/weekly/generate, /reports/balance-sheet', 'reports'], ['/audit/alerts, /duplicates, /suspicious, /reconciliation, /report', 'audit'], ['/sii/audit/report, /accounting/audit/sii-mirror', 'sii-audit'],
      ['/learning/rules (CRUD)', 'rules'], ['POST /accounting/cleanup/november-rcv (se retira)', 'ledger'], ['POST /sii/ops/fetch-rcv, /invoices/import/sii-csv', 'sii'],
      ['POST /annual-close/f22/calculate/{año}', 'annual-close'], ['/treasury/cash-flow-daily, /financial-ratios, /cash-entry', 'treasury'], ['/accounting/ledger/partners/{rut}/movements', 'partners'],
      ['/users/*, /roles/*', 'settings'], ['/organizations/*', 'settings'], ['PUT /sso/users/{id}/role', 'settings'], ['/sso/invite/validate, /register-invite', 'invite'],
      ['Inventario: crear, editar, eliminar, movimientos', 'inventory'], ['WMS: crear ubicación y picking', 'wms'], ['MRP: crear BOM y orden de trabajo', 'mrp'], ['POST /fixed-assets', 'assets'],
      ['/ai/knowledge', 'knowledge'], ['/dms/ai-invoicing/process', 'documents'], ['/notifications/subscribe, /preferences', 'notifications'], ['/sustainability/score', 'sustainability'],
      ['Energía: 70 endpoints (OCPP, precios, mantenimiento, reportes, alertas, modelos)', 'energy']
    ];
    var proposals = ['received', 'b2b-orders'];
    function rowFor(s) {
      var ok = REG.canAccess(s, role), off = s.module && !S.modules[s.module];
      var href = s.route.indexOf(':') >= 0 ? '#/invoices/f1024' : '#' + s.route;
      return html`<tr><td><a href="${href}" class="cell-main">${s.title}</a>${s.navParent ? html`<div class="cell-sub">Dentro de ${REG.BY_ID[s.navParent].title}</div>` : ''}</td>
        <td class="mono small">${s.route}</td><td class="mono small">${s.code || '—'}</td>
        <td>${C.devTag(s)}</td><td class="small">${s.mapa === 'backend' ? 'Backend sin pantalla (§6)' : s.mapa === 'propuesta' ? 'Propuesta nueva' : 'Pantalla ' + s.mapa}</td>
        <td class="small">${!ok ? html`<span class="muted">Sin acceso para ${REG.ROLE_LABEL[role]}</span>` : off ? html`<span class="muted">Módulo desactivado</span>` : html`<span class="up">Visible</span>`}</td></tr>`;
    }
    return {
      subtitle: 'Todas las pantallas del producto, con su estado real de desarrollo y un enlace a cada una.',
      body: html`<div class="stack">
        <div class="grid g4">${['disponible', 'parcial', 'desarrollo', 'planificado'].map(function (k) { return html`<div class="card kpi"><div class="k-label">${C.DEV[k].label}</div><div class="k-value">${counts[k] || 0}</div><div class="k-foot">${C.DEV[k].expl}</div></div>`; })}</div>
        ${C.alert('info', 'Cobertura', inMapa.length + ' de las 43 pantallas de MAPA-FUNCIONAL.md están en el prototipo, más ' + list.filter(function (s) { return s.mapa === 'backend'; }).length + ' funcionalidades del backend sin pantalla y ' + list.filter(function (s) { return s.mapa === 'propuesta'; }).length + ' propuestas nuevas. Pasa el cursor o el foco por cada etiqueta para ver qué falta.')}
        ${areas.map(function (a) {
          var rows = list.filter(function (s) { return s.area === a; });
          return html`<section class="map-area"><h2>${a} <span class="muted small">${rows.length}</span></h2><div class="card table-wrap"><table class="dt compact"><thead><tr><th scope="col">Pantalla</th><th scope="col">Ruta del prototipo</th><th scope="col">Ruta actual</th><th scope="col">Estado</th><th scope="col">Origen</th><th scope="col">Con el rol actual</th></tr></thead><tbody>${rows.map(rowFor)}</tbody></table></div></section>`;
        })}
        <section class="map-area"><h2>Funcionalidades del backend sin pantalla (MAPA-FUNCIONAL §6)</h2><div class="card table-wrap"><table class="dt compact"><thead><tr><th scope="col">Endpoints</th><th scope="col">Dónde aparecen en el prototipo</th></tr></thead><tbody>
          ${backend.map(function (b) { var s = REG.BY_ID[b[1]]; return html`<tr><td class="mono small">${b[0]}</td><td><a href="#${s.route.indexOf(':') >= 0 ? '/invoices/new' : s.route}">${s.title}</a></td></tr>`; })}
        </tbody></table></div></section>
        <section class="map-area"><h2>Componentes fuera de una ruta</h2><div class="card card-pad stack-sm">
          <div class="row-between"><span><strong>Asistente IA</strong> <span class="muted">(panel lateral en el encabezado, MAPA-FUNCIONAL §2.7)</span></span>${C.btn('Abrir asistente', { size: 'sm', icon: 'sparkles', action: 'ai-open' })}</div>
          <div class="row-between"><span><strong>Buscador de pantallas</strong> <span class="muted">(Ctrl + K, propuesta nueva)</span></span>${C.btn('Abrir buscador', { size: 'sm', icon: 'search', action: 'cmd-open' })}</div>
        </div></section>
      </div>`
    };
  };
})();
