/* Componentes de interfaz reutilizables (PLAN-REDISENO §5). Devuelven HTML seguro (U.html). */
(function () {
  'use strict';
  var U = window.U, html = U.html, icon = U.icon, raw = U.raw;

  // ---------- Estados del documento tributario (PLAN §6) ----------
  var DTE = {
    DRAFT: { label: 'Borrador', tone: 't-neutral', icon: 'filePen', desc: 'Aún no tiene validez tributaria.' },
    SIGNED: { label: 'Firmado', tone: 't-signed', icon: 'fileSignature', desc: 'Firmado con el certificado. Falta enviarlo al SII.' },
    SENT: { label: 'Enviado al SII', tone: 't-info', icon: 'send', desc: 'En revisión. El SII aún no responde.' },
    ACCEPTED: { label: 'Aceptado', tone: 't-success', icon: 'checkCircle', desc: 'El SII aceptó el documento.' },
    ACCEPTED_WITH_OBJECTIONS: { label: 'Aceptado con reparos', tone: 't-warning', icon: 'alert', desc: 'Válido, pero el SII informó observaciones.' },
    REJECTED: { label: 'Rechazado', tone: 't-destructive', icon: 'xCircle', desc: 'El SII no lo aceptó. No tiene validez.' },
    CANCELLED: { label: 'Anulado', tone: 't-neutral status-cancelled', icon: 'ban', desc: 'Anulado por una nota de crédito.' }
  };
  function status(code) {
    var s = DTE[code] || { label: code, tone: 't-outline', icon: 'info' };
    return html`<span class="badge ${s.tone}">${icon(s.icon, 13)}<span class="lbl">${s.label}</span></span>`;
  }

  function badge(text, tone, ic) {
    return html`<span class="badge ${'t-' + (tone || 'neutral')}">${ic ? icon(ic, 13) : ''}${text}</span>`;
  }

  // ---------- Etiqueta de estado del desarrollo (solo prototipo) ----------
  var DEV = {
    disponible: { label: 'Disponible', icon: 'checkCircle', expl: 'Conectada al backend: todas sus llamadas existen.' },
    parcial: { label: 'Parcial', icon: 'alert', expl: 'Funciona, pero le faltan acciones o alguna llamada falla.' },
    desarrollo: { label: 'En desarrollo', icon: 'wrench', expl: 'Su función principal no funciona hoy: rota, simulada o sin API.' },
    planificado: { label: 'Planificado', icon: 'calendar', expl: 'Solo existe en el backend, o es una propuesta nueva del rediseño.' }
  };
  var devTagSeq = 0;
  function devTag(sc) {
    if (!sc || !sc.dev) return '';
    var d = DEV[sc.dev]; var id = 'dt-tip-' + (++devTagSeq);
    return html`<span class="devtag">
      <button type="button" class="dev-${sc.dev}" aria-describedby="${id}">${icon(d.icon, 13)}${d.label}</button>
      <span class="tooltip" role="tooltip" id="${id}">
        <strong>${d.label}</strong>${sc.real ? html`<span style="display:block;margin-bottom:4px">Estado real: ${sc.real}</span>` : ''}
        <span>${sc.missing || d.expl}</span>
        <span class="src" style="display:block">Fuente: ${sc.src || 'MAPA-FUNCIONAL.md'}</span>
      </span>
    </span>`;
  }

  // ---------- Botones ----------
  function btn(label, o) {
    o = o || {};
    var cls = 'btn btn-' + (o.variant || 'outline') + (o.size ? ' btn-' + o.size : '') + (o.loading ? ' loading' : '');
    var ic = o.loading ? icon('refresh', 15, 'spin') : (o.icon ? icon(o.icon, 15) : '');
    var attrs = raw(o.attrs || '');
    if (o.href) return html`<a class="${cls}" href="${o.href}" ${attrs}>${ic}${label}</a>`;
    return html`<button type="${o.type || 'button'}" class="${cls}" ${o.action ? raw('data-action="' + U.esc(o.action) + '"') : ''} ${o.disabled ? raw('disabled') : ''} ${o.loading ? raw('aria-busy="true"') : ''} ${attrs}>${ic}${o.loading ? (o.loadingLabel || label) : label}</button>`;
  }
  function iconBtn(ic, label, action, attrs) {
    return html`<button type="button" class="icon-btn" aria-label="${label}" title="${label}" data-action="${action}" ${raw(attrs || '')}>${icon(ic, 18)}</button>`;
  }

  // ---------- Alertas ----------
  var ALERT_ICON = { info: 'info', warning: 'alert', destructive: 'xCircle', success: 'checkCircle', brand: 'sparkles' };
  function alert(tone, title, body, actions) {
    return html`<div class="alert t-${tone}" role="${tone === 'destructive' ? 'alert' : 'status'}">
      ${icon(ALERT_ICON[tone] || 'info', 18)}
      <div class="a-body">${title ? html`<div class="a-title">${title}</div>` : ''}${body ? html`<div>${body}</div>` : ''}
      ${actions ? html`<div class="a-actions">${actions}</div>` : ''}</div>
    </div>`;
  }

  // ---------- Estados de pantalla ----------
  function empty(o) {
    o = o || {};
    return html`<div class="state-box">
      <div class="s-icon">${icon(o.icon || 'inbox', 24)}</div>
      <h2>${o.title || 'Aún no hay registros'}</h2>
      <p>${o.text || 'Cuando agregues el primero, aparecerá aquí.'}</p>
      ${o.action ? html`<div class="row">${o.action}</div>` : ''}
    </div>`;
  }
  function errorState(o) {
    o = o || {};
    return html`<div class="state-box err" role="alert">
      <div class="s-icon">${icon('xCircle', 24)}</div>
      <h2>${o.title || 'No se pudieron cargar los datos'}</h2>
      <p>${o.text || 'El servidor no respondió. Tus datos no se modificaron. Vuelve a intentarlo; si el problema sigue, avisa al administrador.'}</p>
      <div class="row">${btn('Reintentar', { variant: 'primary', icon: 'refresh', action: 'retry' })}</div>
      <details class="tech"><summary>Detalle técnico</summary><p class="mono">${o.tech || 'GET ' + (o.endpoint || '/api/v1/…') + ', 503 Service Unavailable, 15-11-2026 09:42'}</p></details>
    </div>`;
  }
  function noAccess(sc, roleLabel) {
    return html`<div class="state-box lock">
      <div class="s-icon">${icon('lock', 24)}</div>
      <h2>No tienes acceso a ${sc.title}</h2>
      <p>Tu rol (${roleLabel}) no puede usar esta pantalla. Roles con acceso: ${(sc.roles || []).map(roleName).join(', ')}. Si necesitas entrar, pide al administrador que te asigne uno de esos roles.</p>
      <div class="row">${btn('Volver al inicio', { variant: 'primary', href: '#/' })}</div>
    </div>`;
  }
  function moduleOff(sc, isAdmin) {
    return html`<div class="state-box">
      <div class="s-icon">${icon('layers', 24)}</div>
      <h2>El módulo ${sc.moduleLabel || ''} no está activado</h2>
      <p>Esta empresa no usa ${sc.title}. ${isAdmin ? 'Puedes activarlo en Configuración, pestaña Módulos.' : 'Solo un administrador puede activarlo.'}</p>
      <div class="row">${isAdmin ? btn('Ir a Módulos', { variant: 'primary', href: '#/settings?tab=modules' }) : btn('Volver al inicio', { variant: 'outline', href: '#/' })}</div>
    </div>`;
  }
  function loading(kind) {
    var rows = [];
    for (var i = 0; i < 7; i++) rows.push(html`<div class="row" style="gap:16px;padding:12px 16px;border-bottom:1px solid var(--border)"><div class="sk" style="height:14px;width:18%"></div><div class="sk" style="height:14px;width:34%"></div><div class="sk" style="height:14px;width:14%;margin-left:auto"></div></div>`);
    return html`<div aria-busy="true" aria-live="polite"><span class="sr-only">Cargando…</span>
      ${kind !== 'table' ? html`<div class="grid g4" style="margin-bottom:16px">${[1, 2, 3, 4].map(function () { return html`<div class="card kpi"><div class="sk" style="height:12px;width:50%"></div><div class="sk" style="height:26px;width:70%;margin-top:10px"></div></div>`; })}</div>` : ''}
      <div class="card"><div class="dt-toolbar"><div class="sk" style="height:30px;width:240px"></div></div>${rows}</div></div>`;
  }

  // ---------- KPI ----------
  function kpi(label, value, foot, o) {
    o = o || {};
    return html`<div class="card kpi"><div class="k-label">${label}</div><div class="k-value">${value}</div>${foot ? html`<div class="k-foot ${o.tone || ''}">${o.ic ? icon(o.ic, 13) : ''}${foot}</div>` : ''}</div>`;
  }

  // ---------- Tabs ----------
  function tabs(group, items, active) {
    return html`<div class="tabs" role="tablist" aria-label="${group}">${items.map(function (t) {
      var on = t.id === active;
      return html`<button type="button" class="tab" role="tab" aria-selected="${on ? 'true' : 'false'}" tabindex="${on ? '0' : '-1'}" data-action="tab" data-group="${group}" data-tab="${t.id}">${t.icon ? icon(t.icon, 15) : ''}${t.label}${t.badge ? html` <span class="badge t-${t.badgeTone || 'neutral'}">${t.badge}</span>` : ''}</button>`;
    })}</div>`;
  }

  // ---------- Stepper ----------
  function stepper(steps, current, marks) {
    marks = marks || {};
    return html`<ol class="stepper" aria-label="Pasos">${steps.map(function (s, i) {
      var st = marks[i] || (i < current ? 'done' : i === current ? 'current' : '');
      return html`<li class="${st}" ${i === current ? raw('aria-current="step"') : ''}>
        <span class="step-dot">${st === 'done' ? icon('check', 14) : st === 'warn' ? icon('alert', 13) : st === 'error' ? icon('x', 13) : i + 1}</span>
        <span class="step-text"><strong>${s.label}</strong>${s.sub ? html`<span>${s.sub}</span>` : ''}</span>
      </li>`;
    })}</ol>`;
  }

  // ---------- Campos de formulario ----------
  function field(label, control, o) {
    o = o || {};
    return html`<div class="field" ${o.style ? raw('style="' + o.style + '"') : ''}>
      <label for="${o.id || ''}">${label}${o.req ? html` <span class="req" aria-hidden="true">*</span><span class="sr-only">(obligatorio)</span>` : ''}</label>
      ${control}
      ${o.err ? html`<span class="err" id="${(o.id || '') + '-err'}">${icon('xCircle', 13)}${o.err}</span>` : ''}
      ${o.ok ? html`<span class="ok">${icon('checkCircle', 13)}${o.ok}</span>` : ''}
      ${o.hint ? html`<span class="hint" id="${(o.id || '') + '-hint'}">${o.hint}</span>` : ''}
    </div>`;
  }
  function input(id, value, o) {
    o = o || {};
    return html`<input class="input ${o.cls || ''}" id="${id}" name="${o.name || id}" type="${o.type || 'text'}" value="${value == null ? '' : value}" ${o.placeholder ? raw('placeholder="' + U.esc(o.placeholder) + '"') : ''} ${o.err ? raw('aria-invalid="true" aria-describedby="' + id + '-err"') : (o.hint ? raw('aria-describedby="' + id + '-hint"') : '')} ${o.readonly ? raw('readonly') : ''} ${o.attrs ? raw(o.attrs) : ''}>`;
  }
  function select(id, options, value, o) {
    o = o || {};
    return html`<select class="select" id="${id}" name="${o.name || id}" ${o.attrs ? raw(o.attrs) : ''}>${options.map(function (op) {
      var v = Array.isArray(op) ? op[0] : op, l = Array.isArray(op) ? op[1] : op;
      return html`<option value="${v}" ${String(v) === String(value) ? raw('selected') : ''}>${l}</option>`;
    })}</select>`;
  }
  // RUT: formatea al salir del campo y valida el dígito verificador
  function rutField(id, label, value, o) {
    o = o || {};
    var err = null, ok = null;
    if (value) { if (U.validRut(value)) ok = o.okText || 'RUT válido'; else err = 'El dígito verificador no corresponde. Revisa el RUT.'; }
    return field(label, input(id, value ? U.formatRut(value) : '', { placeholder: '12.345.678-5', err: err, attrs: 'data-rut="1" inputmode="text" autocomplete="off"' + (o.attrs ? ' ' + o.attrs : '') }), { id: id, req: o.req, err: err, ok: ok, hint: err ? null : (o.hint || 'Con o sin puntos y guion.') });
  }
  // Monto CLP con separador de miles
  function moneyField(id, label, value, o) {
    o = o || {};
    return field(label, html`<div class="input-group"><span class="prefix">$</span>${input(id, value ? U.int(value) : '', { cls: 'money', attrs: 'data-money="1" inputmode="numeric"' + (o.attrs ? ' ' + o.attrs : ''), hint: o.hint })}</div>`, { id: id, req: o.req, hint: o.hint });
  }
  function periodPicker(id, y, m, o) {
    o = o || {};
    var opts = [];
    for (var i = 0; i < 12; i++) {
      var mm = m - i, yy = y; while (mm < 1) { mm += 12; yy -= 1; }
      var closed = o.closedUntil && (yy < o.closedUntil[0] || (yy === o.closedUntil[0] && mm <= o.closedUntil[1]));
      opts.push([yy + '-' + mm, U.period(yy, mm) + (closed ? ' (cerrado)' : '')]);
    }
    return field(o.label || 'Período', select(id, opts, (o.value || (y + '-' + m)), { attrs: 'data-action-change="' + (o.change || 'period') + '"' }), { id: id });
  }

  // ---------- Tabla de datos ----------
  // cfg: { id, columns:[{key,label,num,render,sort,width}], rows, search, searchKeys, filters:[{key,label,options,get}], rowHref, totals, pageSize, empty, toolbar, compact }
  function dataTable(cfg, st) {
    st = st || {};
    var rows = cfg.rows.slice();
    var q = (st.q || '').toLowerCase().trim();
    if (q) rows = rows.filter(function (r) { return (cfg.searchKeys || []).some(function (k) { var v = typeof k === 'function' ? k(r) : r[k]; return String(v == null ? '' : v).toLowerCase().indexOf(q) >= 0; }); });
    (cfg.filters || []).forEach(function (f) {
      var v = st['f_' + f.key];
      if (v && v !== '*') rows = rows.filter(function (r) { return String(f.get ? f.get(r) : r[f.key]) === v; });
    });
    if (st.sort) {
      var col = cfg.columns.filter(function (c) { return c.key === st.sort; })[0];
      if (col) {
        var get = col.sortGet || function (r) { return r[col.key]; };
        rows.sort(function (a, b) { var x = get(a), y = get(b); var r = x > y ? 1 : x < y ? -1 : 0; return st.dir === 'desc' ? -r : r; });
      }
    }
    var total = rows.length, size = cfg.pageSize || 10, pages = Math.max(1, Math.ceil(total / size));
    var page = Math.min(st.page || 1, pages);
    var shown = rows.slice((page - 1) * size, page * size);
    var filtered = q || (cfg.filters || []).some(function (f) { var v = st['f_' + f.key]; return v && v !== '*'; });

    var toolbar = (cfg.search || cfg.filters || cfg.toolbar) ? html`<div class="dt-toolbar">
      ${cfg.search ? html`<div class="dt-search">${icon('search', 15)}<label class="sr-only" for="${cfg.id}-q">Buscar</label><input class="input" id="${cfg.id}-q" type="search" placeholder="${cfg.search}" value="${st.q || ''}" data-dt="${cfg.id}" data-dt-field="q"></div>` : ''}
      ${(cfg.filters || []).map(function (f) {
        return html`<label class="sr-only" for="${cfg.id}-f-${f.key}">${f.label}</label><select class="select" id="${cfg.id}-f-${f.key}" data-dt="${cfg.id}" data-dt-field="f_${f.key}"><option value="*">${f.label}: todos</option>${f.options.map(function (o) { var v = Array.isArray(o) ? o[0] : o, l = Array.isArray(o) ? o[1] : o; return html`<option value="${v}" ${st['f_' + f.key] === String(v) ? raw('selected') : ''}>${l}</option>`; })}</select>`;
      })}
      ${filtered ? btn('Limpiar filtros', { variant: 'ghost', size: 'sm', icon: 'x', action: 'dt-clear', attrs: 'data-dt="' + cfg.id + '"' }) : ''}
      <span style="flex:1"></span>${cfg.toolbar || ''}
    </div>` : '';

    if (!total) {
      return html`<div class="card">${toolbar}${filtered ? empty({ icon: 'search', title: 'Ningún resultado con estos filtros', text: 'Prueba con otro término o limpia los filtros.', action: btn('Limpiar filtros', { action: 'dt-clear', attrs: 'data-dt="' + cfg.id + '"' }) }) : empty(cfg.empty)}</div>`;
    }
    var head = html`<tr>${cfg.columns.map(function (c) {
      var sorted = st.sort === c.key;
      var aria = sorted ? (st.dir === 'desc' ? 'descending' : 'ascending') : 'none';
      return html`<th scope="col" class="${c.num ? 'num' : ''}" ${c.width ? raw('style="width:' + c.width + '"') : ''} ${c.sort !== false && c.label ? raw('aria-sort="' + aria + '"') : ''}>${c.sort !== false && c.label ? html`<button type="button" class="sort" data-action="dt-sort" data-dt="${cfg.id}" data-key="${c.key}">${c.label}${sorted ? icon(st.dir === 'desc' ? 'chevDown' : 'chevDown', 12, st.dir === 'desc' ? '' : 'up-arrow') : ''}</button>` : (c.label ? c.label : html`<span class="sr-only">Acciones</span>`)}</th>`;
    })}</tr>`;
    var body = shown.map(function (r) {
      var href = cfg.rowHref ? cfg.rowHref(r) : null;
      return html`<tr ${href ? raw('class="clickable" data-href="' + U.esc(href) + '"') : ''}>${cfg.columns.map(function (c, i) {
        var v = c.render ? c.render(r) : r[c.key];
        if (i === 0 && href) v = html`<a href="${href}" class="cell-main" style="color:inherit;text-decoration:none">${v}</a>`;
        return html`<td class="${c.num ? 'num' : ''}">${v}</td>`;
      })}</tr>`;
    });
    var foot = cfg.totals ? html`<tfoot><tr>${cfg.columns.map(function (c, i) { var t = cfg.totals(rows)[c.key]; return html`<td class="${c.num ? 'num' : ''}">${i === 0 ? 'Total (' + total + ')' : (t == null ? '' : t)}</td>`; })}</tr></tfoot>` : '';
    return html`<div class="card">${toolbar}<div class="table-wrap"><table class="dt ${cfg.compact ? 'compact' : ''}"><caption class="sr-only">${cfg.caption || ''}</caption><thead>${head}</thead><tbody>${body}</tbody>${foot}</table></div>
      <div class="dt-foot"><span>Mostrando ${(page - 1) * size + 1} a ${Math.min(page * size, total)} de ${total}</span><span class="spacer"></span>
        ${btn('Anterior', { size: 'sm', variant: 'ghost', icon: 'chevLeft', action: 'dt-page', disabled: page <= 1, attrs: 'data-dt="' + cfg.id + '" data-page="' + (page - 1) + '"' })}
        <span>Página ${page} de ${pages}</span>
        ${btn('Siguiente', { size: 'sm', variant: 'ghost', action: 'dt-page', disabled: page >= pages, attrs: 'data-dt="' + cfg.id + '" data-page="' + (page + 1) + '"' })}
      </div></div>`;
  }

  // ---------- Gráfico de barras agrupadas (SVG) ----------
  function barChart(data, series, o) {
    o = o || {};
    var W = 560, H = 200, pl = 36, pb = 24, pt = 10;
    var max = 0; data.forEach(function (d) { series.forEach(function (s) { max = Math.max(max, d[s.key]); }); });
    max = Math.ceil(max / 10) * 10 || 10;
    var gw = (W - pl) / data.length, bw = Math.min(22, (gw - 12) / series.length);
    var parts = [];
    for (var g = 0; g <= 4; g++) { var y = pt + (H - pt - pb) * g / 4; parts.push('<line class="gridline" x1="' + pl + '" x2="' + W + '" y1="' + y + '" y2="' + y + '"/><text x="' + (pl - 6) + '" y="' + (y + 4) + '" text-anchor="end">' + Math.round(max * (1 - g / 4)) + '</text>'); }
    data.forEach(function (d, i) {
      var x0 = pl + i * gw + (gw - bw * series.length - 4 * (series.length - 1)) / 2;
      series.forEach(function (s, j) {
        var h = (H - pt - pb) * d[s.key] / max;
        parts.push('<rect x="' + (x0 + j * (bw + 4)) + '" y="' + (H - pb - h) + '" width="' + bw + '" height="' + h + '" rx="3" fill="var(' + s.color + ')"><title>' + U.esc(d.m + ', ' + s.label + ': ' + d[s.key] + (o.unit || '')) + '</title></rect>');
      });
      parts.push('<text x="' + (pl + i * gw + gw / 2) + '" y="' + (H - 6) + '" text-anchor="middle">' + U.esc(d.m) + '</text>');
    });
    var desc = data.map(function (d) { return d.m + ': ' + series.map(function (s) { return s.label + ' ' + d[s.key] + (o.unit || ''); }).join(', '); }).join('; ');
    return html`<figure style="margin:0"><svg class="chart" viewBox="0 0 ${W} ${H}" role="img" aria-label="${o.title || 'Gráfico'}. ${desc}">${raw(parts.join(''))}</svg>
      <figcaption class="legend" style="margin-top:6px">${series.map(function (s) { return html`<span><i style="background:var(${s.color})"></i>${s.label}</span>`; })}</figcaption></figure>`;
  }

  // ---------- Medidor de folios ----------
  function meter(used, total, low) {
    var avail = Math.max(0, total - used), pct = total ? used / total * 100 : 100;
    var cls = avail === 0 ? 'out' : (avail < (low || 100) ? 'low' : '');
    return html`<div class="meter ${cls}" role="meter" aria-valuemin="0" aria-valuemax="${total}" aria-valuenow="${avail}" aria-label="Folios disponibles: ${avail} de ${total}"><span class="used" style="width:${pct}%"></span><span class="avail" style="width:${100 - pct}%"></span></div>`;
  }

  // ---------- Recorrido del documento (elemento distintivo) ----------
  function dteTrack(inv) {
    var order = ['DRAFT', 'SIGNED', 'SENT'];
    var st = inv.status;
    var idx = order.indexOf(st); if (idx < 0) idx = 3;
    var resolved = { ACCEPTED: ['res-ok', 'checkCircle', 'Aceptado'], ACCEPTED_WITH_OBJECTIONS: ['res-warn', 'alert', 'Aceptado con reparos'], REJECTED: ['res-bad', 'xCircle', 'Rechazado'], CANCELLED: ['res-cancel', 'ban', 'Anulado'] }[st];
    function when(code) { var h = (inv.history || []).filter(function (x) { return x[1] === code; })[0]; return h ? U.dateTime(h[0]) : ''; }
    var stages = [
      { key: 'DRAFT', label: 'Borrador', ic: 'filePen', sub: when('DRAFT') || 'Creado' },
      { key: 'SIGNED', label: 'Firmado', ic: 'fileSignature', sub: when('SIGNED') || (idx >= 1 ? 'Con certificado' : 'Pendiente') },
      { key: 'SENT', label: 'Enviado al SII', ic: 'send', sub: inv.trackId ? 'TrackID ' + inv.trackId : 'Pendiente' },
      { key: 'RES', label: resolved ? resolved[2] : 'Respuesta del SII', ic: resolved ? resolved[1] : 'hourglass', sub: resolved ? (when(st) || 'Recibida') : (idx === 2 ? 'Esperando respuesta' : 'Pendiente') }
    ];
    return html`<div class="card"><div class="dte-track" aria-label="Recorrido del documento">${stages.map(function (s, i) {
      var reached = i < idx || (i === idx && i < 3);
      var cls = (i < idx ? 'reached' : '') + (i === idx && i < 3 ? ' now reached' : '') + (i === 3 && resolved ? ' ' + resolved[0] : '');
      return html`<div class="dte-stage ${cls}"><span class="dte-node">${icon(s.ic, 16)}</span><strong>${s.label}</strong><span>${s.sub}</span>${i === idx && i < 3 ? html`<span class="sr-only">(etapa actual)</span>` : ''}</div>`;
    })}</div></div>`;
  }

  // ---------- Vista previa del documento impreso ----------
  function docPreview(inv, D) {
    var p = D.party(inv.party), c = D.company;
    var signed = inv.status !== 'DRAFT';
    return html`<article class="doc-paper" aria-label="Vista previa del documento">
      <div class="doc-top">
        <div><div style="font-size:18px;font-weight:700">${c.name}</div><div class="muted">${c.giro}</div><div class="muted">${c.address}</div><div class="muted">${c.email}</div></div>
        <div class="doc-folio">R.U.T.: ${c.rut}<br>${D.TIPOS[inv.tipo].toUpperCase()}<br>N° ${inv.folio ? U.int(inv.folio) : '(se asigna al firmar)'}<div style="font-size:11px;font-weight:600">S.I.I. – CONCEPCIÓN</div></div>
      </div>
      <div class="doc-grid">
        <div><span class="muted">Señor(es): </span><strong>${p.name}</strong></div><div><span class="muted">Fecha de emisión: </span>${U.date(inv.date)}</div>
        <div><span class="muted">R.U.T.: </span>${p.rut}</div><div><span class="muted">Vencimiento: </span>${U.date(inv.due)}</div>
        <div><span class="muted">Ciudad: </span>${p.city}</div><div><span class="muted">Forma de pago: </span>${inv.payment}</div>
      </div>
      <table><thead><tr><th>Descripción</th><th class="num">Cantidad</th><th class="num">Precio unitario</th><th class="num">Total</th></tr></thead>
      <tbody>${inv.lines.map(function (l) { return html`<tr><td>${l.desc}</td><td class="num">${U.int(l.qty)} ${l.unit}</td><td class="num">${U.clp(l.price)}</td><td class="num">${U.clp(l.qty * l.price)}</td></tr>`; })}</tbody></table>
      <div class="doc-bottom">
        <div class="timbre"><div class="timbre-code ${signed ? '' : 'blank'}" role="img" aria-label="${signed ? 'Timbre electrónico PDF417' : 'El timbre se genera al firmar'}"></div>
          <div class="small" style="text-align:center;margin-top:4px">${signed ? 'Timbre Electrónico SII, Res. 80 de 2014, Verifique documento: www.sii.cl' : 'El timbre electrónico se genera al firmar'}</div></div>
        <div class="doc-totals">
          ${inv.exento ? html`<div><span>Monto exento</span><span>${U.clp(inv.exento)}</span></div>` : ''}
          <div><span>Monto neto</span><span>${U.clp(inv.neto)}</span></div>
          <div><span>IVA 19%</span><span>${U.clp(inv.iva)}</span></div>
          <div class="grand"><span>Total</span><span>${U.clp(inv.total)}</span></div>
        </div>
      </div>
    </article>`;
  }

  function roleName(r) { return (window.REG && window.REG.ROLE_LABEL[r]) || r; }

  window.C = {
    DTE: DTE, status: status, badge: badge, devTag: devTag, DEV: DEV, btn: btn, iconBtn: iconBtn, alert: alert, empty: empty,
    errorState: errorState, noAccess: noAccess, moduleOff: moduleOff, loading: loading, kpi: kpi, tabs: tabs, stepper: stepper,
    field: field, input: input, select: select, rutField: rutField, moneyField: moneyField, periodPicker: periodPicker,
    dataTable: dataTable, barChart: barChart, meter: meter, dteTrack: dteTrack, docPreview: docPreview, roleName: roleName
  };
})();
