/* Contabilidad y auditoría: libros, plan de cuentas, estados financieros, cuentas corrientes, activos, cierres, auditoría y reglas. */
(function () {
  'use strict';
  var U = window.U, C = window.C, D = window.D, html = U.html, icon = U.icon, raw = U.raw, V = window.V, A = window.A, CH = window.CH, S = window.S;

  function acc(code) { return D.accounts.filter(function (a) { return a[0] === code; })[0]; }
  function sumPrefix(p) { return D.accounts.filter(function (a) { return a[2] !== null && a[0].indexOf(p) === 0; }).reduce(function (s, a) { return s + a[2]; }, 0); }

  // ======================= Libro diario =======================
  V.ledger = function () {
    return {
      subtitle: 'Asientos contables registrados, con su debe y haber.', endpoint: '/accounting/entries',
      actions: C.btn('Exportar a Excel', { icon: 'download', action: 'toast', attrs: 'data-msg="Libro diario exportado."' }),
      empty: { icon: 'book', title: 'No hay asientos en este período', text: 'Los asientos se generan al emitir documentos, registrar pagos o cerrar el mes.' },
      body: html`<div class="stack">
        ${C.alert('info', null, 'La herramienta "Limpiar RCV de noviembre" que hoy aparece en esta pantalla se retira: borra datos de un mes puntual y sigue activa en producción (PLAN B-11).')}
        ${C.dataTable({ id: 'led', search: 'Buscar por glosa o número', searchKeys: ['n', 'desc'], caption: 'Libro diario',
          columns: [
            { key: 'n', label: 'N°', render: function (r) { return html`<span class="cell-main">${r.n}</span>`; } },
            { key: 'date', label: 'Fecha', render: function (r) { return U.date(r.date); } },
            { key: 'desc', label: 'Glosa', render: function (r) { return html`${r.desc}${r.warn ? html` ${C.badge('Revisar', 'warning', 'alert')}` : ''}`; } },
            { key: 'lines', label: 'Cuentas', sort: false, render: function (r) { return html`<span class="small muted">${r.lines.map(function (l) { return l[1]; }).join(', ')}</span>`; } },
            { key: 'total', label: 'Monto', num: true, render: function (r) { return U.clp(r.total); } },
            { key: 'act', label: '', sort: false, render: function (r) { return C.btn('Ver', { size: 'sm', variant: 'ghost', action: 'entry-open', attrs: 'data-id="' + r.id + '"' }); } }
          ], rows: D.entries }, S.tables.led)}
      </div>`
    };
  };
  A['entry-open'] = function (el) {
    var e = D.entries.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0];
    App.drawer({ title: 'Asiento N° ' + e.n, icon: 'book', body: html`<p class="muted">${U.date(e.date)}, ${e.desc}</p>
      ${e.warn ? html`<div style="margin:12px 0">${C.alert('warning', 'Documento rechazado por el SII', 'Este asiento se creó antes de la respuesta del SII y debe reversarse (RIESGOS R-11).')}</div>` : ''}
      <table class="dt" style="margin-top:12px"><thead><tr><th scope="col">Cuenta</th><th scope="col" class="num">Debe</th><th scope="col" class="num">Haber</th></tr></thead><tbody>${e.lines.map(function (l) { return html`<tr><td><span class="mono">${l[0]}</span> ${l[1]}</td><td class="num">${l[2] ? U.clp(l[2]) : ''}</td><td class="num">${l[3] ? U.clp(l[3]) : ''}</td></tr>`; })}</tbody>
      <tfoot><tr><td>Totales</td><td class="num">${U.clp(e.lines.reduce(function (s, l) { return s + l[2]; }, 0))}</td><td class="num">${U.clp(e.lines.reduce(function (s, l) { return s + l[3]; }, 0))}</td></tr></tfoot></table>
      ${e.ref && D.invById(e.ref) ? html`<p style="margin-top:12px"><a href="#/invoices/${e.ref}">Ver el documento de origen</a></p>` : ''}` });
  };

  // ======================= Plan de cuentas y libro mayor =======================
  V.accounts = function (ctx) {
    var tab = S.tabs.accounts || ctx.query.tab || 'plan';
    var body;
    if (tab === 'plan') body = html`<div class="card"><table class="dt"><caption class="sr-only">Plan de cuentas</caption><thead><tr><th scope="col">Código</th><th scope="col">Cuenta</th><th scope="col" class="num">Saldo</th><th scope="col"><span class="sr-only">Acciones</span></th></tr></thead><tbody>
      ${D.accounts.map(function (a) { var lvl = a[0].split('.').length; var head = a[2] === null; return html`<tr><td class="mono">${a[0]}</td><td style="padding-left:${12 + (lvl - 1) * 18}px;${head ? 'font-weight:600' : ''}">${a[1]}</td><td class="num">${head ? '' : U.clp(Math.abs(a[2]))}</td><td>${head ? '' : C.btn('Ver mayor', { size: 'sm', variant: 'ghost', action: 'acc-mayor', attrs: 'data-code="' + a[0] + '"' })}</td></tr>`; })}
      </tbody></table></div>`;
    else if (tab === 'mayor') {
      var code = S.ui.mayor || '1.1.03', a = acc(code);
      var moves = D.entries.filter(function (e) { return e.lines.some(function (l) { return l[0] === code; }); });
      var run = 0;
      body = html`<div class="stack"><div class="card card-pad" style="max-width:420px">${C.field('Cuenta', C.select('my-acc', D.accounts.filter(function (x) { return x[2] !== null; }).map(function (x) { return [x[0], x[0] + ' ' + x[1]]; }), code, { attrs: 'data-action-change="acc-sel"' }), { id: 'my-acc' })}</div>
        <div class="card"><div class="card-head"><h2>${a[0]} ${a[1]}</h2></div><table class="dt"><thead><tr><th scope="col">Fecha</th><th scope="col">Asiento</th><th scope="col">Glosa</th><th scope="col" class="num">Debe</th><th scope="col" class="num">Haber</th><th scope="col" class="num">Saldo</th></tr></thead><tbody>
        ${moves.length ? moves.slice().reverse().map(function (e) { var l = e.lines.filter(function (x) { return x[0] === code; })[0]; run += l[2] - l[3]; return html`<tr><td>${U.date(e.date)}</td><td>${e.n}</td><td>${e.desc}</td><td class="num">${l[2] ? U.clp(l[2]) : ''}</td><td class="num">${l[3] ? U.clp(l[3]) : ''}</td><td class="num">${U.clp(run)}</td></tr>`; }) : html`<tr><td colspan="6" class="muted" style="text-align:center;padding:24px">Sin movimientos en noviembre para esta cuenta.</td></tr>`}
        </tbody></table></div></div>`;
    } else body = html`<div class="card card-pad stack" style="max-width:640px"><h2>Saldos de apertura</h2><p class="muted">Carga los saldos iniciales de cada cuenta al comenzar a usar el sistema o al iniciar un año.</p>
      <div class="row">${C.btn('Descargar plantilla', { icon: 'download', action: 'toast', attrs: 'data-msg="Plantilla descargada."' })}${C.btn('Cargar saldos', { variant: 'primary', icon: 'upload', action: 'toast', attrs: 'data-msg="Saldos de apertura cargados (POST /accounting/opening-balance/upload)."' })}</div>
      <p class="small muted">Saldos cargados el 02-01-2026 para 24 cuentas.</p></div>`;
    return {
      subtitle: 'Estructura de cuentas, movimientos por cuenta y saldos de apertura.', endpoint: '/accounting/accounts',
      actions: C.btn('Nueva cuenta', { variant: 'primary', icon: 'plus', action: 'acc-new' }),
      body: html`${C.tabs('accounts', [{ id: 'plan', label: 'Plan de cuentas' }, { id: 'mayor', label: 'Libro mayor' }, { id: 'open', label: 'Saldos de apertura' }], tab)}${body}`
    };
  };
  A['acc-mayor'] = function (el) { S.ui.mayor = el.getAttribute('data-code'); S.tabs.accounts = 'mayor'; App.render(); };
  CH['acc-sel'] = function (el) { S.ui.mayor = el.value; App.render(); };
  A['acc-new'] = function () {
    App.dialog({ title: 'Nueva cuenta', body: html`<div class="stack">${C.field('Código', C.input('ac-c', '', { placeholder: '5.2.06' }), { id: 'ac-c', req: true })}${C.field('Nombre', C.input('ac-n', '', { placeholder: 'Publicidad' }), { id: 'ac-n', req: true })}${C.field('Tipo', C.select('ac-t', ['Activo', 'Pasivo', 'Patrimonio', 'Ingreso', 'Gasto']), { id: 'ac-t' })}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Crear cuenta', { variant: 'primary', action: 'acc-save' })}` });
  };
  A['acc-save'] = function () { App.closeDialog(); App.toast('Cuenta creada.'); };

  // ======================= Balance =======================
  V.balance = function () {
    var act = sumPrefix('1'), pas = -sumPrefix('2'), pat = -sumPrefix('3'), res = -(sumPrefix('4') + sumPrefix('5'));
    function rows(prefix, sign) {
      return D.accounts.filter(function (a) { return a[0].indexOf(prefix) === 0 && a[0] !== prefix; }).map(function (a) {
        var lvl = a[0].split('.').length, head = a[2] === null;
        return html`<tr><td style="padding-left:${12 + (lvl - 1) * 16}px;${head ? 'font-weight:600' : ''}"><span class="mono small muted">${a[0]}</span> ${a[1]}</td><td class="num">${head ? '' : U.clp(a[2] * sign)}</td><td class="num muted">${head ? '' : U.clp(Math.round(a[2] * sign * 0.94))}</td></tr>`;
      });
    }
    var ok = act === pas + pat + res;
    return {
      subtitle: 'Situación financiera al 31-10-2026 comparada con el mes anterior.', endpoint: '/accounting/balance-sheet',
      actions: html`${C.btn('Exportar PDF', { icon: 'download', action: 'toast', attrs: 'data-msg="Balance exportado."' })}`,
      body: html`<div class="stack">
        <div class="grid g4">${C.kpi('Total activos', U.clp(act))}${C.kpi('Total pasivos', U.clp(pas))}${C.kpi('Patrimonio', U.clp(pat))}${C.kpi('Resultado del ejercicio', U.clp(res), null)}</div>
        ${ok ? C.alert('success', 'El balance cuadra', 'Activos = pasivos + patrimonio + resultado.') : C.alert('destructive', 'El balance no cuadra', 'Diferencia de ' + U.clp(act - pas - pat - res) + '.')}
        ${C.alert('info', null, 'Hoy el árbol de cuentas de esta pantalla usa datos de ejemplo: GET /accounting/accounts/tree no existe (MAPA-FUNCIONAL §4).')}
        <div class="grid g2">
          <div class="card"><div class="card-head"><h2>Activos</h2></div><table class="dt compact"><thead><tr><th scope="col">Cuenta</th><th scope="col" class="num">Octubre</th><th scope="col" class="num">Septiembre</th></tr></thead><tbody>${rows('1', 1)}</tbody><tfoot><tr><td>Total activos</td><td class="num">${U.clp(act)}</td><td class="num">${U.clp(Math.round(act * 0.94))}</td></tr></tfoot></table></div>
          <div class="card"><div class="card-head"><h2>Pasivos y patrimonio</h2></div><table class="dt compact"><thead><tr><th scope="col">Cuenta</th><th scope="col" class="num">Octubre</th><th scope="col" class="num">Septiembre</th></tr></thead><tbody>${rows('2', -1)}${rows('3', -1)}<tr><td style="padding-left:28px">Resultado del ejercicio</td><td class="num">${U.clp(res)}</td><td class="num muted">${U.clp(Math.round(res * 0.94))}</td></tr></tbody><tfoot><tr><td>Total pasivos y patrimonio</td><td class="num">${U.clp(pas + pat + res)}</td><td class="num">${U.clp(Math.round((pas + pat + res) * 0.94))}</td></tr></tfoot></table></div>
        </div></div>`
    };
  };

  // ======================= Estado de resultados =======================
  V.income = function () {
    var v = -acc('4.1.01')[2], cv = acc('5.1.01')[2], rem = acc('5.2.01')[2], arr = acc('5.2.02')[2], dep = acc('5.2.03')[2];
    var mb = v - cv, ro = mb - rem - arr - dep;
    var lines = [['Ventas', v, 1], ['Costo de ventas', -cv, 0], ['Margen bruto', mb, 2], ['Remuneraciones', -rem, 0], ['Arriendos', -arr, 0], ['Depreciación', -dep, 0], ['Resultado operacional', ro, 2]];
    return {
      subtitle: 'Ingresos, costos y resultado de octubre 2026.', endpoint: '/reports/income-statement',
      actions: C.btn('Exportar', { icon: 'download', action: 'toast', attrs: 'data-msg="Estado de resultados exportado."' }),
      body: html`<div class="grid g-main">
        <div class="card"><table class="dt"><caption class="sr-only">Estado de resultados</caption><thead><tr><th scope="col">Concepto</th><th scope="col" class="num">Octubre</th><th scope="col" class="num">Septiembre</th><th scope="col" class="num">Variación</th></tr></thead><tbody>
          ${lines.map(function (l) { var prev = Math.round(l[1] * 0.86); var dv = l[1] - prev; return html`<tr ${l[2] === 2 ? raw('style="background:var(--muted);font-weight:600"') : ''}><td>${l[0]}</td><td class="num">${U.clp(l[1])}</td><td class="num muted">${U.clp(prev)}</td><td class="num ${dv >= 0 ? 'up' : 'down'}">${U.clp(dv, { plus: true })}</td></tr>`; })}
        </tbody></table></div>
        <div class="stack">${C.kpi('Margen bruto', Math.round(mb / v * 100) + '%', 'Sobre ventas netas')}${C.kpi('Resultado operacional', U.clp(ro), Math.round(ro / v * 100) + '% de las ventas', { tone: ro >= 0 ? 'up' : 'down' })}
          <div class="card card-pad">${C.barChart(D.salesSeries.slice(0, 5), [{ key: 'v', label: 'Ventas', color: '--chart-1' }, { key: 'c', label: 'Costos', color: '--chart-3' }], { unit: ' M', title: 'Ventas y costos por mes, en millones de pesos' })}</div>
        </div></div>`
    };
  };

  // ======================= Cuentas corrientes =======================
  V.partners = function () {
    var rows = D.parties.map(function (p) {
      var bal = p.kind === 'Cliente' ? D.invoices.filter(function (i) { return i.party === p.id && (i.status === 'ACCEPTED' || i.status === 'ACCEPTED_WITH_OBJECTIONS') && i.tipo !== 61; }).reduce(function (s, i) { return s + i.total; }, 0) : -D.purchaseOrders.filter(function (o) { return o.party === p.id && o.status !== 'Borrador'; }).reduce(function (s, o) { return s + o.total; }, 0);
      return { id: p.id, name: p.name, rut: p.rut, kind: p.kind, bal: bal };
    });
    return {
      subtitle: 'Saldos por cliente y proveedor.', endpoint: '/accounting/ledger/partners',
      body: C.dataTable({ id: 'par', search: 'Buscar por nombre o RUT', searchKeys: ['name', 'rut'], filters: [{ key: 'kind', label: 'Tipo', options: ['Cliente', 'Proveedor'] }],
        columns: [
          { key: 'name', label: 'Nombre', render: function (r) { return html`<span class="cell-main">${r.name}</span><div class="cell-sub">${r.rut}</div>`; } },
          { key: 'kind', label: 'Tipo', render: function (r) { return C.badge(r.kind, r.kind === 'Cliente' ? 'brand' : 'neutral'); } },
          { key: 'bal', label: 'Saldo', num: true, render: function (r) { return html`<span class="money ${r.bal < 0 ? 'down' : ''}">${U.clp(r.bal)}</span>`; } },
          { key: 'act', label: '', sort: false, render: function (r) { return C.btn('Movimientos', { size: 'sm', variant: 'ghost', action: 'par-moves', attrs: 'data-id="' + r.id + '"' }); } }
        ], rows: rows }, S.tables.par)
    };
  };
  A['par-moves'] = function (el) {
    var p = D.party(el.getAttribute('data-id'));
    var inv = D.invoices.filter(function (i) { return i.party === p.id && i.status !== 'DRAFT'; });
    App.drawer({ title: p.name, icon: 'users', body: html`<p class="muted">${p.rut}, ${p.city}</p><div style="margin:12px 0">${C.devTag({ dev: 'planificado', real: 'Solo backend', missing: 'El detalle usa /accounting/ledger/partners/{rut}/movements, que existe en el backend sin uso.', src: 'MAPA-FUNCIONAL.md §2.2, §6' })}</div>
      ${inv.length ? html`<table class="dt compact"><thead><tr><th scope="col">Documento</th><th scope="col">Fecha</th><th scope="col" class="num">Monto</th></tr></thead><tbody>${inv.map(function (i) { return html`<tr><td><a href="#/invoices/${i.id}" data-action="drawer-close">${window.VENTAS.docName(i)}</a></td><td>${U.date(i.date)}</td><td class="num">${U.clp(i.tipo === 61 ? -i.total : i.total)}</td></tr>`; })}</tbody></table>` : C.empty({ icon: 'inbox', title: 'Sin movimientos', text: 'No hay documentos ni pagos registrados para este proveedor en el período.' })}` });
  };

  // ======================= Activos fijos =======================
  V.assets = function () {
    return {
      subtitle: 'Bienes de la empresa y su depreciación.', endpoint: '/fixed-assets',
      actions: html`${C.btn('Calcular depreciación', { icon: 'refresh', action: 'dep-run', loading: App.isBusy('dep'), loadingLabel: 'Calculando…' })}${C.btn('Nuevo activo', { variant: 'primary', icon: 'plus', action: 'asset-new' })}`,
      empty: { icon: 'building', title: 'Aún no hay activos fijos', text: 'Registra vehículos, maquinaria o equipos para calcular su depreciación.' },
      body: C.dataTable({ id: 'ast', search: 'Buscar activo', searchKeys: ['name', 'cat'], filters: [{ key: 'cat', label: 'Categoría', options: ['Vehículos', 'Maquinaria', 'Equipos computacionales'] }],
        columns: [
          { key: 'name', label: 'Activo', render: function (r) { return html`<span class="cell-main">${r.name}</span>`; } },
          { key: 'cat', label: 'Categoría' },
          { key: 'date', label: 'Adquisición', render: function (r) { return U.date(r.date); } },
          { key: 'life', label: 'Vida útil', num: true, render: function (r) { return r.life + ' años'; } },
          { key: 'cost', label: 'Costo', num: true, render: function (r) { return U.clp(r.cost); } },
          { key: 'dep', label: 'Depreciación acumulada', num: true, render: function (r) { return U.clp(r.dep); } },
          { key: 'net', label: 'Valor libro', num: true, sortGet: function (r) { return r.cost - r.dep; }, render: function (r) { return U.clp(r.cost - r.dep); } }
        ], rows: D.assets, totals: function (rows) { return { cost: U.clp(rows.reduce(function (s, r) { return s + r.cost; }, 0)), dep: U.clp(rows.reduce(function (s, r) { return s + r.dep; }, 0)), net: U.clp(rows.reduce(function (s, r) { return s + r.cost - r.dep; }, 0)) }; } }, S.tables.ast)
    };
  };
  A['dep-run'] = function () { App.busy('dep', 900, function () { App.toast('Depreciación de noviembre calculada: ' + U.clp(548000) + '.'); }); };
  A['asset-new'] = function () {
    S.wiz.asset = { name: '', cat: 'Vehículos', cost: 0, life: 7 };
    App.dialog({ title: 'Nuevo activo fijo', body: html`<div class="stack">${C.field('Nombre', C.input('as-n', '', { attrs: 'data-bind="wiz.asset.name"' }), { id: 'as-n', req: true })}
      <div class="grid g2">${C.field('Categoría', C.select('as-c', ['Vehículos', 'Maquinaria', 'Equipos computacionales'], 'Vehículos', { attrs: 'data-bind="wiz.asset.cat"' }), { id: 'as-c' })}${C.field('Vida útil (años)', C.input('as-l', 7, { attrs: 'data-bind="wiz.asset.life" data-type="int" inputmode="numeric"' }), { id: 'as-l' })}</div>
      ${C.moneyField('as-v', 'Costo de adquisición', 0, { attrs: 'data-bind="wiz.asset.cost" data-type="money"', hint: 'Valor neto, sin IVA.' })}
      ${C.alert('info', null, 'Hoy no se pueden crear activos desde la pantalla, aunque existe POST /fixed-assets.')}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Crear activo', { variant: 'primary', action: 'asset-save' })}` });
  };
  A['asset-save'] = function () { var a = S.wiz.asset; if (!a.name || !a.cost) { App.toast('Completa el nombre y el costo.', 'bad'); return; } D.assets.unshift({ id: 'af' + Date.now(), name: a.name, cat: a.cat, date: D.TODAY, cost: a.cost, life: a.life || 7, dep: 0 }); App.closeDialog(); App.render(); App.toast('Activo creado.'); };

  // ======================= Cierre mensual =======================
  function closeTabs(active) { return C.tabs('close-kind', [{ id: 'm', label: 'Mensual' }, { id: 'a', label: 'Anual (F22)' }], active); }
  A['tab'] = (function (orig) { return function (el) { if (el.getAttribute('data-group') === 'close-kind') { App.go(el.getAttribute('data-tab') === 'a' ? '/accounting/annual-close' : '/accounting/close'); return; } orig(el); }; })(A.tab);

  V.close = function () {
    var w = S.wiz.close = S.wiz.close || { step: 0, ran: false, accepted: {}, dismissed: {} };
    var steps = [{ label: 'Pendientes', sub: 'Antes de cerrar' }, { label: 'Auditoría IA', sub: 'Sugerencias' }, { label: 'Ajustes', sub: 'Asientos propuestos' }, { label: 'Confirmar', sub: 'Cerrar período' }, { label: 'Cerrado', sub: 'Octubre 2026' }];
    var findings = [
      { id: 'x1', t: 'Factura de compra duplicada', d: 'La factura N° 55120 de Distribuidora Andes Mayorista está registrada dos veces.', adj: 'Reversar asiento duplicado por ' + U.clp(4760000), basis: 'Mismo RUT, folio y monto en los asientos 798 y 801.' },
      { id: 'x2', t: 'Asiento de una factura rechazada', d: 'La factura N° 1.021 fue rechazada por el SII, pero su venta sigue contabilizada.', adj: 'Reversar venta por ' + U.clp(D.invById('f1021').total), basis: 'Estado "Rechazado" en el detalle del documento.' },
      { id: 'x3', t: 'Depreciación de octubre sin registrar', d: 'No hay asiento de depreciación para octubre.', adj: 'Registrar depreciación por ' + U.clp(548000), basis: 'Activos fijos activos con vida útil vigente.' }
    ];
    var body;
    if (w.step === 0) {
      var checks = [
        ['ok', 'Documentos de venta contabilizados', 'Todos los documentos aceptados tienen asiento.'],
        ['warn', '2 movimientos bancarios sin conciliar', 'Comisión de mantención y un depósito en efectivo.', C.btn('Conciliar', { size: 'sm', href: '#/banking/reconciliation' })],
        ['warn', '1 documento sin respuesta del SII', 'Factura N° 1.024.', C.btn('Ver', { size: 'sm', href: '#/invoices/f1024' })],
        ['ok', 'Remuneraciones calculadas', 'Octubre 2026.'],
        ['ok', 'Borrador del F29 listo', 'Total a pagar ' + U.clp(window.F29.total) + '.']
      ];
      body = html`<div class="card">${checks.map(function (c, i) { return html`<div class="row" style="padding:14px 16px;gap:12px;flex-wrap:nowrap;${i ? 'border-top:1px solid var(--border)' : ''}"><span class="badge t-${c[0] === 'ok' ? 'success' : 'warning'}" style="height:28px;width:28px;padding:0;justify-content:center">${icon(c[0] === 'ok' ? 'check' : 'alert', 14)}</span><div style="flex:1"><strong>${c[1]}</strong><div class="muted">${c[2]}</div></div>${c[3] || ''}</div>`; })}</div>
        <p class="small muted" style="margin-top:8px">Puedes continuar con advertencias; quedarán registradas en el cierre.</p>`;
    } else if (w.step === 1) {
      body = !w.ran ? html`<div class="card">${C.empty({ icon: 'sparkles', title: 'Revisa el mes con la IA antes de cerrar', text: 'La auditoría busca duplicados, documentos rechazados contabilizados y asientos faltantes. Sus resultados son sugerencias: nada cambia sin tu aprobación.', action: C.btn('Ejecutar auditoría', { variant: 'primary', icon: 'sparkles', action: 'close-run', loading: App.isBusy('audit'), loadingLabel: 'Revisando octubre…' }) })}</div>`
        : html`<div class="stack">${findings.map(function (f) {
          var st = w.accepted[f.id] ? 'acc' : w.dismissed[f.id] ? 'dis' : '';
          return html`<div class="card card-pad stack-sm"><div class="row-between"><div class="row">${C.badge('Sugerencia de la IA', 'brand', 'sparkles')}<strong>${f.t}</strong></div>${st === 'acc' ? C.badge('Ajuste aceptado', 'success', 'check') : st === 'dis' ? C.badge('Descartada', 'neutral') : ''}</div>
            <p>${f.d}</p><p class="small muted">En qué se basa: ${f.basis}</p>
            ${st ? C.btn('Deshacer', { size: 'sm', variant: 'ghost', action: 'close-undo', attrs: 'data-id="' + f.id + '"' }) : html`<div class="row">${C.btn('Aceptar ajuste: ' + f.adj, { size: 'sm', variant: 'primary', action: 'close-acc', attrs: 'data-id="' + f.id + '"' })}${C.btn('Descartar', { size: 'sm', variant: 'ghost', action: 'close-dis', attrs: 'data-id="' + f.id + '"' })}</div>`}</div>`;
        })}</div>`;
    } else if (w.step === 2) {
      var acc2 = findings.filter(function (f) { return w.accepted[f.id]; });
      body = acc2.length ? html`<div class="card"><div class="card-head"><h2>Asientos de ajuste a registrar</h2></div><table class="dt"><thead><tr><th scope="col">Ajuste</th><th scope="col">Origen</th></tr></thead><tbody>${acc2.map(function (f) { return html`<tr><td>${f.adj}</td><td>${C.badge('Sugerencia aceptada', 'brand', 'sparkles')}</td></tr>`; })}</tbody></table></div>`
        : html`<div class="card">${C.empty({ icon: 'check', title: 'Sin ajustes', text: 'No aceptaste sugerencias. Puedes continuar al cierre.' })}</div>`;
    } else if (w.step === 3) {
      body = html`<div class="card card-pad stack" style="max-width:640px"><h2>Cerrar octubre 2026</h2><p class="muted">Después de cerrar no podrás registrar ni modificar asientos con fecha de octubre. Para corregir algo, un administrador tendrá que reabrir el período.</p>
        <ul style="margin:0;padding-left:18px"><li>${Object.keys(w.accepted).length} ajustes registrados</li><li>2 advertencias pendientes (conciliación y SII)</li></ul>
        <div>${C.btn('Cerrar período', { variant: 'primary', icon: 'lock', action: 'close-do', loading: App.isBusy('close'), loadingLabel: 'Cerrando…' })}</div></div>`;
    } else {
      body = html`<div class="card card-pad stack" style="max-width:640px"><div class="row">${C.badge('Período cerrado', 'success', 'lock')}<span class="muted">${U.dateTime(w.at)}</span></div><h2>Octubre 2026 está cerrado</h2><p class="muted">Los reportes y el F29 de octubre usan ahora cifras definitivas.</p><div class="row">${C.btn('Ver balance', { href: '#/accounting/balance' })}${C.btn('Declarar F29', { variant: 'primary', href: '#/accounting/f29' })}</div></div>`;
    }
    var foot = w.step < 3 ? html`<div class="row-between" style="margin-top:16px"><div>${w.step ? C.btn('Atrás', { icon: 'chevLeft', action: 'close-back' }) : ''}</div>${C.btn('Continuar', { variant: 'primary', action: 'close-next', disabled: w.step === 1 && !w.ran })}</div>` : (w.step === 3 ? html`<div style="margin-top:16px">${C.btn('Atrás', { icon: 'chevLeft', action: 'close-back' })}</div>` : '');
    return { title: 'Cierres', subtitle: 'Cierre contable mensual con revisión asistida.', endpoint: '/period-close/audit',
      body: html`${closeTabs('m')}${C.stepper(steps, w.step, w.step === 0 ? {} : { 0: 'warn' })}${body}${foot}` };
  };
  A['close-next'] = function () { S.wiz.close.step++; App.render(); };
  A['close-back'] = function () { S.wiz.close.step--; App.render(); };
  A['close-run'] = function () { App.busy('audit', 1300, function () { S.wiz.close.ran = true; App.toast('Auditoría lista: 3 sugerencias.'); }); };
  A['close-acc'] = function (el) { S.wiz.close.accepted[el.getAttribute('data-id')] = true; App.render(); };
  A['close-dis'] = function (el) { S.wiz.close.dismissed[el.getAttribute('data-id')] = true; App.render(); };
  A['close-undo'] = function (el) { var id = el.getAttribute('data-id'); delete S.wiz.close.accepted[id]; delete S.wiz.close.dismissed[id]; App.render(); };
  A['close-do'] = function () {
    App.confirm({ title: 'Cerrar octubre 2026', text: 'No podrás registrar ni modificar asientos de octubre después del cierre.', confirm: 'Cerrar período', icon: 'lock',
      onConfirm: function () { App.busy('close', 1000, function () { S.wiz.close.step = 4; S.wiz.close.at = new Date().toISOString(); App.toast('Octubre 2026 cerrado.'); }); } });
  };

  // ======================= Cierre anual (F22) =======================
  V['annual-close'] = function () {
    var w = S.wiz.annual = S.wiz.annual || { step: 0 };
    var steps = [{ label: 'Meses cerrados', sub: 'Año 2026' }, { label: 'Calcular F22', sub: 'Renta anual' }, { label: 'Revisar', sub: 'Códigos' }, { label: 'Exportar', sub: 'Abril 2027' }];
    var months = U.MESES.map(function (m, i) { return { m: m, closed: i < 10 }; });
    var base = 61420000, tax = Math.round(base * 0.25), ppm = 1480000;
    var body;
    if (w.step === 0) body = html`<div class="card card-pad"><div class="grid g4">${months.map(function (m) { return html`<div class="row" style="gap:8px">${m.closed ? C.badge('Cerrado', 'success', 'lock') : C.badge('Abierto', 'warning')}<span style="text-transform:capitalize">${m.m}</span></div>`; })}</div>
      <div style="margin-top:16px">${C.alert('warning', 'Noviembre y diciembre siguen abiertos', 'Puedes calcular un F22 preliminar; el definitivo requiere los 12 meses cerrados.')}</div></div>`;
    else if (w.step === 1) body = html`<div class="card">${w.calc ? C.empty({ icon: 'check', title: 'Cálculo listo', text: 'Revisa los códigos en el paso siguiente.' }) : C.empty({ icon: 'calculator', title: 'Calcular la renta del año tributario 2027', text: 'Usa los estados financieros de 2026 y los PPM pagados. Hoy esta pantalla no llama al cálculo del backend (POST /annual-close/f22/calculate/{año}).', action: C.btn('Calcular F22 preliminar', { variant: 'primary', icon: 'calculator', action: 'f22-calc', loading: App.isBusy('f22'), loadingLabel: 'Calculando…' }) })}</div>`;
    else if (w.step === 2) body = html`<div class="grid g-main"><div class="card"><table class="dt"><thead><tr><th scope="col">Concepto</th><th scope="col" class="num">Monto</th></tr></thead><tbody>
        <tr><td>Renta líquida imponible</td><td class="num">${U.clp(base)}</td></tr><tr><td>Impuesto de primera categoría (tasa configurada 25%)</td><td class="num">${U.clp(tax)}</td></tr>
        <tr><td>PPM pagados en el año</td><td class="num">${U.clp(-ppm)}</td></tr><tr style="font-weight:600;background:var(--muted)"><td>Saldo a pagar</td><td class="num">${U.clp(tax - ppm)}</td></tr></tbody></table></div>
      <div class="stack">${C.alert('warning', 'Confirma la tasa según tu régimen', 'La tasa de primera categoría depende del régimen tributario de la empresa y del año. Verifícala antes de declarar.')}${C.alert('info', null, 'Cifras preliminares: noviembre y diciembre están abiertos.')}</div></div>`;
    else body = html`<div class="card card-pad stack" style="max-width:600px"><h2>F22 preliminar listo</h2><p class="muted">Descarga el archivo para revisarlo con tu contador o cargarlo en el sitio del SII en abril de 2027.</p><div>${C.btn('Descargar F22', { variant: 'primary', icon: 'download', action: 'toast', attrs: 'data-msg="F22 preliminar descargado."' })}</div></div>`;
    var foot = w.step < 3 ? html`<div class="row-between" style="margin-top:16px"><div>${w.step ? C.btn('Atrás', { icon: 'chevLeft', action: 'f22-back' }) : ''}</div>${C.btn('Continuar', { variant: 'primary', action: 'f22-next', disabled: w.step === 1 && !w.calc })}</div>` : '';
    return { title: 'Cierres', crumbs: [], subtitle: 'Cierre del año y cálculo de la declaración anual de renta.', endpoint: '/annual-close/f22/calculate/2026',
      body: html`${closeTabs('a')}${C.stepper(steps, w.step, w.step > 0 ? { 0: 'warn' } : {})}${body}${foot}` };
  };
  A['f22-calc'] = function () { App.busy('f22', 1300, function () { S.wiz.annual.calc = true; App.toast('F22 preliminar calculado.'); }); };
  A['f22-next'] = function () { S.wiz.annual.step++; App.render(); };
  A['f22-back'] = function () { S.wiz.annual.step--; App.render(); };

  // ======================= Auditoría (unificada) =======================
  function auditView(ctx, fromSii) {
    var tab = S.tabs.audit || ctx.query.tab || (fromSii ? 'mirror' : 'mirror');
    var body;
    if (tab === 'mirror') body = html`<div class="stack">
      <div class="grid g3">${C.kpi('Ventas: RCV vs ERP', '112 / 112', 'Cuadran', { tone: 'up', ic: 'checkCircle' })}${C.kpi('Compras: RCV vs ERP', '48 / 45', '3 faltan en el ERP', { tone: 'down', ic: 'alert' })}${C.kpi('Diferencia en compras', U.clp(2190400), 'Crédito fiscal no aprovechado')}</div>
      ${C.dataTable({ id: 'mir', caption: 'Documentos del RCV que no están en el ERP', columns: [
        { key: 'f', label: 'Documento', render: function (r) { return html`<span class="cell-main">${r.f}</span>`; } }, { key: 'p', label: 'Proveedor' }, { key: 'd', label: 'Fecha', render: function (r) { return U.date(r.d); } },
        { key: 'm', label: 'Total', num: true, render: function (r) { return U.clp(r.m); } }, { key: 'a', label: '', sort: false, render: function () { return C.btn('Registrar en el ERP', { size: 'sm', action: 'toast', attrs: 'data-msg="Documento registrado en el ERP."' }); } }
      ], rows: [{ f: 'Factura N° 55098', p: 'Distribuidora Andes Mayorista SpA', d: '2026-10-08', m: 1190000 }, { f: 'Factura N° 3290', p: 'Plásticos Biobío Ltda.', d: '2026-10-17', m: 535500 }, { f: 'Factura N° 7712', p: 'Energía Limpia Atacama SpA', d: '2026-10-28', m: 464900 }] }, S.tables.mir)}</div>`;
    else if (tab === 'alerts') body = html`<div class="stack">${C.alert('info', 'Planificado', 'El backend ya detecta duplicados, montos inusuales y diferencias con el SII (/audit/alerts, /duplicates, /suspicious); esta pestaña es nueva.')}
      ${C.dataTable({ id: 'aal', filters: [{ key: 'sev', label: 'Severidad', options: ['Alta', 'Media', 'Baja'] }, { key: 'kind', label: 'Tipo', options: ['Duplicado', 'Monto inusual', 'Espejo SII'] }], columns: [
        { key: 'sev', label: 'Severidad', render: function (r) { return C.badge(r.sev, r.sev === 'Alta' ? 'destructive' : r.sev === 'Media' ? 'warning' : 'neutral'); } },
        { key: 'kind', label: 'Tipo' }, { key: 'desc', label: 'Descripción' }, { key: 'date', label: 'Detectada', render: function (r) { return U.date(r.date); } },
        { key: 'amount', label: 'Monto', num: true, render: function (r) { return U.clp(r.amount); } },
        { key: 'a', label: '', sort: false, render: function (r) { return html`<div class="row" style="flex-wrap:nowrap">${C.btn('Revisar', { size: 'sm', action: 'toast', attrs: 'data-msg="Alerta marcada como revisada."' })}${C.btn('Descartar', { size: 'sm', variant: 'ghost', action: 'toast', attrs: 'data-msg="Alerta descartada." data-tone="info"' })}</div>`; } }
      ], rows: D.auditAlerts }, S.tables.aal)}</div>`;
    else body = html`<div class="card card-pad stack" style="max-width:640px">${C.devTag({ dev: 'planificado', real: 'Solo backend', missing: 'Usa /audit/report y /sii/audit/report, que existen en el backend sin pantalla.', src: 'MAPA-FUNCIONAL.md §6' })}<h2>Reporte de auditoría</h2><p class="muted">Resumen de alertas, conciliación con el SII y ajustes del período, para entregar a un auditor externo.</p>
      ${C.periodPicker('aud-per', 2026, 10, { change: 'noop' })}<div>${C.btn('Generar reporte PDF', { variant: 'primary', icon: 'download', action: 'toast', attrs: 'data-msg="Reporte de auditoría generado."' })}</div></div>`;
    return {
      title: 'Auditoría', crumbs: [], subtitle: 'Conciliación con el SII, alertas automáticas y reporte para auditores.', endpoint: '/accounting/audit/sii-mirror',
      body: html`${fromSii ? html`<div style="margin-bottom:12px">${C.alert('info', 'Esta dirección se unificó', 'Auditoría SII y Auditoría contable eran la misma pantalla. En el rediseño ambas abren Auditoría (PLAN §3.4).', C.btn('Ir a Auditoría', { size: 'sm', href: '#/accounting/audit' }))}</div>` : ''}
        ${C.tabs('audit', [{ id: 'mirror', label: 'Espejo SII', icon: 'scale' }, { id: 'alerts', label: 'Alertas y duplicados', icon: 'alert', badge: String(D.auditAlerts.length), badgeTone: 'warning' }, { id: 'report', label: 'Reporte', icon: 'fileText' }], tab)}${body}`
    };
  }
  V.audit = function (ctx) { return auditView(ctx, false); };
  V['sii-audit'] = function (ctx) { return auditView(ctx, true); };

  // ======================= Reglas de clasificación =======================
  V.rules = function () {
    return {
      subtitle: 'Cómo se asigna automáticamente la cuenta contable de cada documento.', endpoint: '/learning/rules',
      actions: C.btn('Nueva regla', { variant: 'primary', icon: 'plus', action: 'rule-edit' }),
      empty: { icon: 'rule', title: 'Aún no hay reglas', text: 'Crea una regla para que los documentos de un proveedor o con cierta glosa se clasifiquen solos.' },
      body: html`<div class="stack">${C.alert('warning', 'Hoy editar una regla no guarda cambios', 'El backend responde "Regla actualizada" sin guardar nada (RIESGOS R-19). Esta pantalla se habilita cuando se corrija.')}
        ${C.dataTable({ id: 'rul', columns: [
          { key: 'match', label: 'Cuando', render: function (r) { return html`<span class="cell-main">${r.match}</span>`; } }, { key: 'account', label: 'Clasificar en' },
          { key: 'hits', label: 'Aplicada', num: true, render: function (r) { return r.hits + ' veces'; } },
          { key: 'by', label: 'Creada por', render: function (r) { return r.by === 'IA' ? C.badge('Aprendida por la IA', 'brand', 'sparkles') : r.by; } },
          { key: 'a', label: '', sort: false, render: function (r) { return html`<div class="row" style="flex-wrap:nowrap">${C.btn('Editar', { size: 'sm', variant: 'ghost', action: 'rule-edit', attrs: 'data-id="' + r.id + '"' })}${C.btn('Eliminar', { size: 'sm', variant: 'ghost', action: 'rule-del', attrs: 'data-id="' + r.id + '"' })}</div>`; } }
        ], rows: D.rules }, S.tables.rul)}</div>`
    };
  };
  A['rule-edit'] = function (el) {
    var r = D.rules.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0] || { match: '', account: '' };
    App.dialog({ title: r.id ? 'Editar regla' : 'Nueva regla', body: html`<div class="stack">${C.field('Cuando el documento', C.input('ru-m', r.match, { placeholder: 'RUT del proveedor o texto de la glosa' }), { id: 'ru-m', req: true })}${C.field('Clasificar en la cuenta', C.select('ru-a', D.accounts.filter(function (a) { return a[2] !== null && a[0].charAt(0) === '5'; }).map(function (a) { return a[0] + ' ' + a[1]; }), r.account), { id: 'ru-a' })}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Guardar regla', { variant: 'primary', action: 'rule-save' })}` });
  };
  A['rule-save'] = function () { App.closeDialog(); App.toast('Regla guardada.'); };
  A['rule-del'] = function (el) {
    var id = el.getAttribute('data-id');
    App.confirm({ title: 'Eliminar la regla', text: 'Los documentos futuros dejarán de clasificarse con esta regla. Los ya clasificados no cambian.', confirm: 'Eliminar regla', destructive: true, icon: 'trash', onConfirm: function () { D.rules.splice(D.rules.indexOf(D.rules.filter(function (x) { return x.id === id; })[0]), 1); App.render(); App.toast('Regla eliminada.'); } });
  };
})();
