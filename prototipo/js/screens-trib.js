/* Tributario (SII): salud tributaria, F29, folios (CAF), sincronización y documentos recibidos. */
(function () {
  'use strict';
  var U = window.U, C = window.C, D = window.D, html = U.html, icon = U.icon, raw = U.raw, V = window.V, A = window.A, CH = window.CH, S = window.S;

  // Cifras del F29 de octubre 2026 (ficticias, coherentes entre pantallas)
  var ventas = 51800120, debito = Math.round(ventas * 0.19), credito = 4211830, ppm = Math.round(ventas * 0.0025);
  var retHon = D.fees.filter(function (f) { return f.date < '2026-11-01'; }).reduce(function (s, f) { return s + f.ret; }, 0);
  var F29 = { ventas: ventas, debito: debito, credito: credito, iva: debito - credito, ppm: ppm, ret: retHon, total: debito - credito + ppm + retHon };
  window.F29 = F29;

  function tipoName(t) { return D.TIPOS[t]; }
  function cafSummary() {
    var tipos = [33, 34, 39, 52, 56, 61];
    return tipos.map(function (t) {
      var list = D.cafs.filter(function (c) { return c.tipo === t; });
      var total = list.reduce(function (s, c) { return s + (c.to - c.from + 1); }, 0), used = list.reduce(function (s, c) { return s + c.used; }, 0);
      return { tipo: t, total: total, used: used, avail: total - used };
    });
  }

  // ======================= Salud tributaria =======================
  V.compliance = function () {
    var cs = cafSummary(), f33 = cs[0];
    var sent = D.invoices.filter(function (i) { return i.status === 'SENT'; }).length;
    var rej = D.invoices.filter(function (i) { return i.status === 'REJECTED'; }).length;
    var items = [
      { tone: 'warning', ic: 'calendar', t: 'F29 de octubre por declarar', d: 'Vence el 20-11-2026. Total a pagar estimado: ' + U.clp(F29.total) + '.', a: C.btn('Revisar F29', { size: 'sm', href: '#/accounting/f29' }) },
      { tone: f33.avail < 100 ? 'warning' : 'success', ic: 'layers', t: f33.avail + ' folios de factura disponibles', d: 'Al ritmo de octubre alcanzan para unos 9 días.', a: C.btn('Ver folios', { size: 'sm', href: '#/sii/caf' }) },
      { tone: 'destructive', ic: 'layers', t: 'Sin folios de guía de despacho', d: 'No puedes emitir guías (tipo 52) hasta cargar un CAF.', a: C.btn('Cargar CAF', { size: 'sm', href: '#/sii/caf' }) },
      { tone: rej ? 'destructive' : 'success', ic: 'xCircle', t: rej + ' documento rechazado por el SII', d: 'Corrígelo y emítelo de nuevo para que tenga validez.', a: C.btn('Ver rechazados', { size: 'sm', href: '#/invoices' }) },
      { tone: sent ? 'info' : 'success', ic: 'hourglass', t: sent + ' documento sin respuesta del SII', d: 'El sistema aún no consulta el estado automáticamente (PLAN B-02).', a: C.btn('Ver documentos', { size: 'sm', href: '#/invoices' }) },
      { tone: 'warning', ic: 'scale', t: '3 facturas del RCV que no están en el ERP', d: 'Diferencia de ' + U.clp(2190400) + ' en compras de octubre.', a: C.btn('Ver en Auditoría', { size: 'sm', href: '#/accounting/audit?tab=mirror' }) },
      { tone: 'success', ic: 'key', t: 'Certificado digital vigente', d: 'Vence el 08-01-2027 (en 54 días).', a: C.btn('Ver certificado', { size: 'sm', href: '#/settings?tab=cert' }) }
    ];
    var bad = items.filter(function (i) { return i.tone === 'destructive'; }).length, warn = items.filter(function (i) { return i.tone === 'warning'; }).length;
    return {
      subtitle: 'Lo que necesita tu atención para estar al día con el SII.', endpoint: '/compliance/dashboard',
      body: html`<div class="stack">
        <div class="grid g3">${C.kpi('Requieren acción', String(bad), bad ? 'Bloquean la operación' : 'Nada urgente', { tone: bad ? 'down' : 'up', ic: bad ? 'xCircle' : 'checkCircle' })}${C.kpi('Por revisar', String(warn), 'Con plazo próximo', { ic: 'alert' })}${C.kpi('Próximo vencimiento', '20-11-2026', 'F29 de octubre')}</div>
        <div class="card">${items.map(function (it, i) {
          return html`<div class="row" style="padding:14px 16px;gap:14px;flex-wrap:nowrap;${i ? 'border-top:1px solid var(--border)' : ''}">
            <span class="badge t-${it.tone}" style="height:32px;width:32px;padding:0;justify-content:center;flex-shrink:0">${icon(it.ic, 16)}</span>
            <div style="flex:1;min-width:0"><strong>${it.t}</strong><div class="muted">${it.d}</div></div>
            <span class="sr-only">Prioridad: ${it.tone === 'destructive' ? 'urgente' : it.tone === 'warning' ? 'por revisar' : it.tone === 'info' ? 'informativo' : 'al día'}</span>${it.a}</div>`;
        })}</div>
        ${C.alert('info', 'Acuse de recibo', 'El aviso de documentos recibidos por aceptar o reclamar es simulado en el sistema actual (RIESGOS, brechas funcionales). Ver Documentos recibidos, propuesta nueva.', C.btn('Ver propuesta', { size: 'sm', href: '#/sii/received' }))}
      </div>`
    };
  };

  // ======================= Formulario 29 =======================
  V.f29 = function () {
    var w = S.wiz.f29 = S.wiz.f29 || { step: 0, period: '2026-10' };
    var steps = [{ label: 'Revisar', sub: 'Códigos y diferencias' }, { label: 'Confirmar', sub: 'Montos a declarar' }, { label: 'Declarado', sub: 'Comprobante' }];
    var p = w.period.split('-'), per = U.period(Number(p[0]), Number(p[1]));
    var rows = [
      ['Ventas y débito fiscal', null, null],
      ['563', 'Base imponible (ventas netas del período)', F29.ventas],
      ['538', 'Débito fiscal (IVA de ventas)', F29.debito],
      ['Compras y crédito fiscal', null, null],
      ['520', 'Crédito por facturas de compra recibidas', F29.credito],
      ['537', 'Total crédito fiscal', F29.credito],
      ['Determinación', null, null],
      ['089', 'IVA determinado (538 − 537)', F29.iva],
      ['062', 'PPM (0,25% de las ventas)', F29.ppm],
      ['151', 'Retención de honorarios', F29.ret],
      ['091', 'Total a pagar', F29.total]
    ];
    var table = html`<div class="card"><div class="card-head"><h2>Formulario 29, ${per}</h2><span class="spacer"></span>${C.btn('Descargar borrador', { size: 'sm', icon: 'download', action: 'toast', attrs: 'data-msg="Borrador del F29 descargado."' })}</div>
      <table class="dt"><thead><tr><th scope="col" style="width:80px">Código</th><th scope="col">Concepto</th><th scope="col" class="num">Monto</th></tr></thead><tbody>
      ${rows.map(function (r) { return r[1] === null ? html`<tr><td colspan="3" style="background:var(--muted);font-weight:600;font-size:12px">${r[0]}</td></tr>` : html`<tr><td class="mono">${r[0]}</td><td ${r[0] === '091' ? raw('class="strong"') : ''}>${r[1]}</td><td class="num ${r[0] === '091' ? 'strong' : ''}">${U.clp(r[2])}</td></tr>`; })}
      </tbody></table></div>`;
    var body;
    if (w.step === 0) body = html`<div class="grid g-main"><div class="stack">${table}</div><div class="stack">
        ${C.periodPicker('f29-per', 2026, 10, { value: w.period, change: 'f29-period', closedUntil: [2026, 9] })}
        ${C.alert('warning', 'Calculado con parámetros de 2024', 'El sistema actual usa la tabla tributaria de 2024 (RIESGOS R-07). Verifica PPM y retención antes de declarar.')}
        ${C.alert('warning', 'Diferencia con el RCV del SII', '3 facturas de compra por ' + U.clp(2190400) + ' están en el RCV y no en el ERP. Si las agregas, el crédito fiscal sube.', C.btn('Revisar en Auditoría', { size: 'sm', href: '#/accounting/audit?tab=mirror' }))}
        <div class="card card-pad"><div class="k-label muted small">Total a pagar</div><div style="font-size:28px;font-weight:700" class="money">${U.clp(F29.total)}</div><div class="small muted">Vence el 20-11-2026</div></div>
      </div></div>`;
    else if (w.step === 1) body = html`<div class="grid g-main"><div>${table}</div><div class="card card-pad stack">
        <h3>Confirma la declaración</h3>
        <div class="totals"><div><span>IVA determinado</span><span>${U.clp(F29.iva)}</span></div><div><span>PPM</span><span>${U.clp(F29.ppm)}</span></div><div><span>Retención honorarios</span><span>${U.clp(F29.ret)}</span></div><div class="grand"><span>Total a pagar</span><span>${U.clp(F29.total)}</span></div></div>
        <label class="check"><input type="checkbox" data-bind="wiz.f29.ok" data-rerender ${w.ok ? raw('checked') : ''}><span>Revisé los montos y las diferencias con el RCV.</span></label>
      </div></div>`;
    else body = html`<div class="card card-pad stack" style="max-width:640px">
        <div class="row">${C.badge('Declarado', 'success', 'checkCircle')}<span class="muted">${U.dateTime(w.at)}</span></div>
        <h2>F29 de ${per.toLowerCase()} declarado</h2>
        <p class="muted">Folio de la declaración: <strong>${w.folio}</strong>. Total declarado: <strong class="money">${U.clp(F29.total)}</strong>. Paga antes del 20-11-2026 en el sitio del SII o en tu banco.</p>
        <p class="small muted">Se envía a través de POST /reports/f29/submit, que hoy existe y funciona.</p>
        <div class="row">${C.btn('Descargar comprobante', { icon: 'download', action: 'toast', attrs: 'data-msg="Comprobante descargado."' })}${C.btn('Volver a salud tributaria', { href: '#/compliance' })}</div>
      </div>`;
    var foot = w.step < 2 ? html`<div class="row-between" style="margin-top:16px"><div>${w.step ? C.btn('Atrás', { icon: 'chevLeft', action: 'f29-back' }) : ''}</div>
      ${w.step === 0 ? C.btn('Continuar a confirmar', { variant: 'primary', action: 'f29-next' }) : C.btn('Declarar F29', { variant: 'primary', icon: 'send', action: 'f29-submit', disabled: !w.ok, loading: App.isBusy('f29'), loadingLabel: 'Declarando…' })}</div>` : '';
    return { subtitle: 'Declaración mensual de IVA y otros impuestos.', endpoint: '/accounting/f29', body: html`${C.stepper(steps, w.step)}${body}${foot}` };
  };
  CH['f29-period'] = function (el) { S.wiz.f29.period = el.value; App.render(); };
  A['f29-next'] = function () { S.wiz.f29.step = 1; App.render(); };
  A['f29-back'] = function () { S.wiz.f29.step = 0; App.render(); };
  A['f29-submit'] = function () {
    var w = S.wiz.f29, p = w.period.split('-');
    App.confirm({ title: 'Declarar el F29 de ' + U.period(Number(p[0]), Number(p[1])).toLowerCase(), text: 'Se enviará la declaración al SII por un total de ' + U.clp(F29.total) + '. Para modificarla después tendrás que presentar una rectificatoria.', confirm: 'Declarar F29', icon: 'send',
      onConfirm: function () { App.busy('f29', 1200, function () { w.step = 2; w.folio = '8812' + String(Math.floor(Math.random() * 900000 + 100000)); w.at = new Date().toISOString(); App.toast('F29 declarado.'); }); } });
  };

  // ======================= Folios (CAF) =======================
  V.caf = function () {
    var cs = cafSummary();
    return {
      subtitle: 'Rangos de folios autorizados por el SII para cada tipo de documento.', endpoint: '/sii/caf',
      actions: C.btn('Cargar CAF', { variant: 'primary', icon: 'upload', action: 'caf-upload' }),
      empty: { icon: 'layers', title: 'Aún no has cargado folios', text: 'Descarga un CAF desde el sitio del SII y cárgalo aquí para poder emitir documentos.', action: C.btn('Cargar CAF', { variant: 'primary', icon: 'upload', action: 'caf-upload' }) },
      body: html`<div class="stack">
        ${C.alert('info', null, 'La lista de CAF y el medidor de folios son del rediseño: hoy el backend no tiene un endpoint para listarlos (PLAN B-03). La carga sí funciona.')}
        <div class="grid g3">${cs.map(function (c) {
          var low = c.avail > 0 && c.avail < 100, out = c.avail === 0;
          return html`<div class="card card-pad stack-sm">
            <div class="row-between"><strong>${tipoName(c.tipo)}</strong>${out ? C.badge('Sin folios', 'destructive', 'xCircle') : low ? C.badge('Quedan pocos', 'warning', 'alert') : C.badge('Suficientes', 'success', 'checkCircle')}</div>
            <div><span style="font-size:24px;font-weight:700" class="money">${U.int(c.avail)}</span> <span class="muted">disponibles de ${U.int(c.total)}</span></div>
            ${C.meter(c.used, c.total || 1)}
            ${out ? html`<span class="small" style="color:var(--destructive)">No puedes emitir este tipo de documento.</span>` : html`<span class="small muted">Próximo folio: ${c.total ? U.int(D.cafs.filter(function (x) { return x.tipo === c.tipo && x.used < x.to - x.from + 1; })[0] ? (function () { var x = D.cafs.filter(function (y) { return y.tipo === c.tipo && y.used < y.to - y.from + 1; })[0]; return x.from + x.used; })() : 0) : '—'}</span>`}
          </div>`;
        })}</div>
        ${C.dataTable({ id: 'caf', caption: 'CAF cargados', columns: [
          { key: 'tipo', label: 'Tipo de documento', render: function (r) { return html`<span class="cell-main">${tipoName(r.tipo)}</span> <span class="cell-sub">(${r.tipo})</span>`; } },
          { key: 'from', label: 'Rango', render: function (r) { return U.int(r.from) + ' a ' + U.int(r.to); } },
          { key: 'used', label: 'Usados', num: true, render: function (r) { return U.int(r.used); } },
          { key: 'avail', label: 'Disponibles', num: true, sortGet: function (r) { return r.to - r.from + 1 - r.used; }, render: function (r) { return U.int(r.to - r.from + 1 - r.used); } },
          { key: 'issued', label: 'Autorizado', render: function (r) { return U.date(r.issued); } },
          { key: 'st', label: 'Estado', sort: false, render: function (r) { var a = r.to - r.from + 1 - r.used; return a === 0 ? C.badge('Agotado', 'neutral') : C.badge('En uso', 'brand'); } }
        ], rows: D.cafs }, S.tables.caf)}
      </div>`
    };
  };
  A['caf-upload'] = function () { S.ui.caf = { step: 0 }; App.dialog({ title: 'Cargar CAF', body: cafBody, actions: cafActions }); };
  function cafBody() {
    var s = S.ui.caf;
    if (s.step === 0) return html`<p>El CAF es el archivo XML que entrega el SII al autorizar un rango de folios. Descárgalo desde el sitio del SII (Factura electrónica › Timbraje electrónico).</p>
      <label class="card card-pad" style="display:flex;flex-direction:column;align-items:center;gap:8px;border-style:dashed;margin-top:12px;cursor:pointer">${icon('upload', 24)}<strong>Elegir archivo XML</strong><span class="small muted">Un archivo por rango de folios</span><input type="file" accept=".xml" class="sr-only" data-action-change="caf-file"></label>
      <div class="row" style="justify-content:center;margin-top:8px">${C.btn('Usar archivo de ejemplo', { variant: 'link', action: 'caf-sample' })}</div>`;
    return html`<p>Revisa los datos del archivo antes de cargarlo.</p>
      <dl style="display:grid;grid-template-columns:auto 1fr;gap:6px 16px;margin:12px 0">
        <dt class="muted">Archivo</dt><dd style="margin:0">FoliosSII764812051101.xml</dd>
        <dt class="muted">Tipo</dt><dd style="margin:0">Factura electrónica (33)</dd>
        <dt class="muted">Rango</dt><dd style="margin:0">1.101 a 1.600 (500 folios)</dd>
        <dt class="muted">RUT emisor</dt><dd style="margin:0">${D.company.rut} ${C.badge('Coincide', 'success', 'check')}</dd>
        <dt class="muted">Autorizado</dt><dd style="margin:0">15-11-2026</dd>
        <dt class="muted">Firma del SII</dt><dd style="margin:0">${C.badge('Válida', 'success', 'check')}</dd>
      </dl>${C.alert('info', null, 'La clave privada del CAF se guardará cifrada (hoy se guarda sin cifrar, DESIGN §4).')}`;
  }
  function cafActions() { return S.ui.caf.step === 0 ? C.btn('Cancelar', { action: 'dialog-close' }) : html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Cargar 500 folios', { variant: 'primary', action: 'caf-save' })}`; }
  A['caf-sample'] = function () { S.ui.caf.step = 1; App.refreshOverlay(); };
  CH['caf-file'] = function () { S.ui.caf.step = 1; App.refreshOverlay(); };
  A['caf-save'] = function () {
    if (!D.cafs.some(function (c) { return c.id === 'c7'; })) D.cafs.push({ id: 'c7', tipo: 33, from: 1101, to: 1600, used: 0, issued: D.TODAY });
    App.closeDialog(); App.render(); App.toast('CAF cargado: 500 folios de factura electrónica disponibles.');
  };

  // ======================= Sincronización SII =======================
  V.sii = function () {
    var w = S.wiz.sii = S.wiz.sii || { period: '2026-10', last: '2026-11-14T22:00', runs: [['2026-11-14T22:00', 'Octubre 2026', 48, 112, 'Completa'], ['2026-11-01T22:00', 'Octubre 2026', 41, 98, 'Completa'], ['2026-10-31T22:00', 'Septiembre 2026', 52, 120, 'Con advertencias']] };
    return {
      subtitle: 'Descarga del Registro de Compras y Ventas (RCV) desde el SII.', endpoint: '/sii/ops/fetch-rcv',
      actions: html`${C.btn('Importar CSV del SII', { icon: 'upload', action: 'toast', attrs: 'data-msg="Usa POST /invoices/import/sii-csv (existe en el backend, sin pantalla)." data-tone="info"' })}${C.btn('Descargar RCV', { variant: 'primary', icon: 'refresh', action: 'sii-fetch', loading: App.isBusy('rcv'), loadingLabel: 'Descargando…' })}`,
      body: html`<div class="stack">
        <div class="grid g3">${C.kpi('Última descarga', U.dateTime(w.last), w.runs[0][1])}${C.kpi('Compras en el RCV', String(w.runs[0][2]), '3 no están en el ERP', { ic: 'alert' })}${C.kpi('Ventas en el RCV', String(w.runs[0][3]), 'Cuadran con el ERP', { tone: 'up', ic: 'checkCircle' })}</div>
        <div class="grid g-main">
          ${C.dataTable({ id: 'rcv', caption: 'Historial de descargas', columns: [
            { key: 0, label: 'Fecha', render: function (r) { return U.dateTime(r[0]); } }, { key: 1, label: 'Período' },
            { key: 2, label: 'Compras', num: true }, { key: 3, label: 'Ventas', num: true },
            { key: 4, label: 'Resultado', render: function (r) { return C.badge(r[4], r[4] === 'Completa' ? 'success' : 'warning'); } }
          ], rows: w.runs }, S.tables.rcv)}
          <div class="card card-pad stack">${C.periodPicker('sii-per', 2026, 10, { value: w.period, change: 'sii-period' })}
            <p class="small muted">La descarga se programa cada noche. Puedes forzarla para el período elegido.</p>
            ${C.alert('info', null, 'Hoy esta pantalla llama a /sii/status y /sii/sync, que no existen. Debe usar POST /sii/ops/fetch-rcv (PLAN B-05).')}</div>
        </div></div>`
    };
  };
  CH['sii-period'] = function (el) { S.wiz.sii.period = el.value; App.render(); };
  A['sii-fetch'] = function () {
    var w = S.wiz.sii, p = w.period.split('-');
    App.busy('rcv', 1400, function () { var now = new Date().toISOString(); w.last = now; w.runs.unshift([now, U.period(Number(p[0]), Number(p[1])), 48, 112, 'Completa']); App.toast('RCV descargado: 48 compras y 112 ventas.'); });
  };

  // ======================= Documentos recibidos (propuesta nueva) =======================
  var received = [
    { id: 'r1', folio: 55133, party: 's1', date: '2026-11-13', total: 2380000, status: 'Por revisar', left: 6 },
    { id: 'r2', folio: 9921, party: 's4', date: '2026-11-10', total: 7140000, status: 'Aceptado', left: 0 },
    { id: 'r3', folio: 3310, party: 's3', date: '2026-11-08', total: 1309000, status: 'Por revisar', left: 1 },
    { id: 'r4', folio: 7745, party: 's2', date: '2026-11-03', total: 892500, status: 'Reclamado', left: 0 }
  ];
  var RTONE = { 'Por revisar': 'warning', Aceptado: 'success', Reclamado: 'destructive' };
  V.received = function () {
    return {
      subtitle: 'Facturas que tus proveedores emitieron a tu nombre. Tienes 8 días para aceptarlas o reclamarlas (Ley 19.983).',
      empty: { icon: 'inbox', title: 'No hay documentos recibidos', text: 'Cuando un proveedor te emita una factura, aparecerá aquí.' },
      body: html`<div class="stack">${C.alert('info', 'Propuesta nueva', 'La recepción de DTE de terceros y el acuse o reclamo no existen hoy en el backend (RIESGOS, brechas funcionales).')}
        ${C.dataTable({ id: 'rcvd', search: 'Buscar por folio o proveedor', searchKeys: ['folio', function (r) { return D.party(r.party).name; }], filters: [{ key: 'status', label: 'Estado', options: ['Por revisar', 'Aceptado', 'Reclamado'] }],
          columns: [
            { key: 'folio', label: 'Documento', render: function (r) { return html`<span class="cell-main">Factura N° ${U.int(r.folio)}</span>`; } },
            { key: 'party', label: 'Proveedor', render: function (r) { var p = D.party(r.party); return html`<div>${p.name}</div><div class="cell-sub">${p.rut}</div>`; } },
            { key: 'date', label: 'Recibido', render: function (r) { return U.date(r.date); } },
            { key: 'total', label: 'Total', num: true, render: function (r) { return U.clp(r.total); } },
            { key: 'status', label: 'Estado', render: function (r) { return html`${C.badge(r.status, RTONE[r.status])}${r.status === 'Por revisar' ? html` <span class="small ${r.left <= 2 ? '' : 'muted'}" style="${r.left <= 2 ? 'color:var(--destructive)' : ''}">${r.left} ${r.left === 1 ? 'día' : 'días'} para responder</span>` : ''}`; } },
            { key: 'act', label: '', sort: false, render: function (r) { return r.status === 'Por revisar' ? html`<div class="row" style="flex-wrap:nowrap">${C.btn('Aceptar', { size: 'sm', action: 'rcv-set', attrs: 'data-id="' + r.id + '" data-v="Aceptado"' })}${C.btn('Reclamar', { size: 'sm', variant: 'ghost', action: 'rcv-claim', attrs: 'data-id="' + r.id + '"' })}</div>` : ''; } }
          ], rows: received }, S.tables.rcvd)}</div>`
    };
  };
  A['rcv-set'] = function (el) { var r = received.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; r.status = el.getAttribute('data-v'); App.closeDialog(); App.render(); App.toast('Factura N° ' + U.int(r.folio) + ': ' + r.status.toLowerCase() + '.'); };
  A['rcv-claim'] = function (el) {
    var id = el.getAttribute('data-id');
    App.dialog({ title: 'Reclamar la factura', body: html`<p>El reclamo se informa al SII y al proveedor. No se puede deshacer.</p><div style="margin-top:12px">${C.field('Motivo', C.select('rc-m', ['Reclamo al contenido del documento', 'Falta parcial de mercaderías', 'Falta total de mercaderías']), { id: 'rc-m' })}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Reclamar factura', { variant: 'destructive', action: 'rcv-set', attrs: 'data-id="' + id + '" data-v="Reclamado"' })}` });
  };
})();
