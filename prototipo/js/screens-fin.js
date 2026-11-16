/* Tesorería, bancos, conciliación, remuneraciones y portal del empleado. */
(function () {
  'use strict';
  var U = window.U, C = window.C, D = window.D, html = U.html, icon = U.icon, raw = U.raw, V = window.V, A = window.A, CH = window.CH, S = window.S;

  // ======================= Flujo de caja (Tesorería) =======================
  var cashMoves = [
    { id: 'cm1', date: '2026-11-14', desc: 'Venta mesón sucursal', kind: 'Ingreso', amount: 186000 },
    { id: 'cm2', date: '2026-11-12', desc: 'Compra de insumos de aseo', kind: 'Egreso', amount: 42900 },
    { id: 'cm3', date: '2026-11-08', desc: 'Fondo fijo reparto', kind: 'Egreso', amount: 120000 }
  ];
  V.treasury = function () {
    var tab = S.tabs.treasury || 'summary';
    var bank = D.banks.reduce(function (s, b) { return s + b.balance; }, 0);
    var forecast = [{ m: 'Sem 1', v: 18.4, c: 12.1 }, { m: 'Sem 2', v: 11.2, c: 15.8 }, { m: 'Sem 3', v: 16.9, c: 9.4 }, { m: 'Sem 4', v: 14.1, c: 13.3 }];
    var body;
    if (tab === 'summary') body = html`<div class="stack">
      <div class="grid g4">${C.kpi('Saldo en bancos', U.clp(bank), '2 cuentas conectadas')}${C.kpi('Por cobrar a 30 días', U.clp(18240600), '9 documentos')}${C.kpi('Por pagar a 30 días', U.clp(11870300), '4 proveedores y remuneraciones')}${C.kpi('Caja proyectada en 30 días', U.clp(bank + 18240600 - 11870300), 'Pronóstico IA', { tone: 'up', ic: 'trending' })}</div>
      <div class="grid g-main">
        <div class="card"><div class="card-head"><h2>Pronóstico de caja, próximas 4 semanas</h2><span class="spacer"></span>${C.badge('Pronóstico IA', 'brand', 'sparkles')}</div><div class="card-body">${C.barChart(forecast, [{ key: 'v', label: 'Entradas', color: '--chart-2' }, { key: 'c', label: 'Salidas', color: '--chart-5' }], { unit: ' M', title: 'Entradas y salidas proyectadas por semana, en millones de pesos' })}
          <p class="small muted" style="margin-top:8px">En qué se basa: vencimientos de facturas emitidas y recibidas, remuneraciones y el comportamiento de pago de cada cliente.</p></div></div>
        <div class="stack">
          <div class="card card-pad stack-sm"><h3>Indicadores económicos</h3><div class="row-between"><span class="muted">UF</span><strong class="money">$39.982</strong></div><div class="row-between"><span class="muted">Dólar observado</span><strong class="money">$948</strong></div><div class="row-between"><span class="muted">UTM noviembre</span><strong class="money">$69.611</strong></div><span class="small muted">Valores ficticios del prototipo.</span></div>
          <div class="card card-pad stack-sm"><div class="row-between"><h3>Ratios financieros</h3>${C.badge('Planificado', 'info')}</div><div class="row-between"><span class="muted">Liquidez corriente</span><strong>3,15</strong></div><div class="row-between"><span class="muted">Prueba ácida</span><strong>2,62</strong></div><div class="row-between"><span class="muted">Endeudamiento</span><strong>0,27</strong></div><span class="small muted">Usa /treasury/financial-ratios, que existe sin pantalla.</span></div>
        </div>
      </div>
      ${C.alert('info', 'La conciliación bancaria tiene su propia pantalla', 'Antes estaba repetida aquí como pestaña (PLAN §3.4).', C.btn('Ir a Conciliación', { size: 'sm', href: '#/banking/reconciliation' }))}
    </div>`;
    else if (tab === 'cash') body = html`<div class="stack">${C.dataTable({ id: 'cash', search: 'Buscar movimiento', searchKeys: ['desc'], filters: [{ key: 'kind', label: 'Tipo', options: ['Ingreso', 'Egreso'] }], columns: [
        { key: 'date', label: 'Fecha', render: function (r) { return U.date(r.date); } }, { key: 'desc', label: 'Descripción', render: function (r) { return html`<span class="cell-main">${r.desc}</span>`; } },
        { key: 'kind', label: 'Tipo', render: function (r) { return C.badge(r.kind, r.kind === 'Ingreso' ? 'success' : 'neutral', r.kind === 'Ingreso' ? 'trending' : 'trendingDown'); } },
        { key: 'amount', label: 'Monto', num: true, render: function (r) { return html`<span class="${r.kind === 'Ingreso' ? 'up' : 'down'}">${U.clp(r.kind === 'Ingreso' ? r.amount : -r.amount, { plus: true })}</span>`; } }
      ], rows: cashMoves, empty: { icon: 'wallet', title: 'Sin movimientos de caja', text: 'Registra ingresos y egresos en efectivo.' } }, S.tables.cash)}</div>`;
    else body = html`<div class="card card-pad stack" style="max-width:640px"><div class="row-between"><h2>Flujo de caja diario</h2>${C.badge('Planificado', 'info')}</div><p class="muted">Saldo inicial, entradas, salidas y saldo final de cada día del mes.</p>
      <table class="dt compact"><thead><tr><th scope="col">Día</th><th scope="col" class="num">Entradas</th><th scope="col" class="num">Salidas</th><th scope="col" class="num">Saldo</th></tr></thead><tbody>
      ${[['10-11-2026', 2353820, 7140000, 54210000], ['11-11-2026', 380000, 0, 54590000], ['12-11-2026', 0, 12900, 54577100], ['13-11-2026', 981988, 0, 55559088], ['14-11-2026', 4807600, 0, 60366688]].map(function (r) { return html`<tr><td>${r[0]}</td><td class="num up">${U.clp(r[1])}</td><td class="num down">${U.clp(-r[2])}</td><td class="num">${U.clp(r[3])}</td></tr>`; })}
      </tbody></table><span class="small muted">Usa /treasury/cash-flow-daily, que existe sin pantalla.</span></div>`;
    return {
      title: 'Flujo de caja', subtitle: 'Liquidez actual y proyectada de la empresa.', endpoint: '/treasury/forecast',
      actions: html`${C.btn('Subir cartola', { icon: 'upload', action: 'stmt-upload' })}${C.btn('Registrar movimiento', { variant: 'primary', icon: 'plus', action: 'cash-new' })}`,
      body: html`${C.tabs('treasury', [{ id: 'summary', label: 'Resumen' }, { id: 'cash', label: 'Movimientos de caja' }, { id: 'daily', label: 'Flujo diario' }], tab)}${body}`
    };
  };
  A['cash-new'] = function () {
    S.wiz.cash = { kind: 'Ingreso', desc: '', amount: 0 };
    App.dialog({ title: 'Registrar movimiento de caja', body: html`<div class="stack">${C.field('Tipo', C.select('cs-k', ['Ingreso', 'Egreso'], 'Ingreso', { attrs: 'data-bind="wiz.cash.kind"' }), { id: 'cs-k' })}${C.field('Descripción', C.input('cs-d', '', { attrs: 'data-bind="wiz.cash.desc"' }), { id: 'cs-d', req: true })}${C.moneyField('cs-a', 'Monto', 0, { attrs: 'data-bind="wiz.cash.amount" data-type="money"' })}
      ${C.alert('info', null, 'Hoy este registro es simulado: el front llama a /treasury/cash-entries y el backend tiene /treasury/cash-entry (MAPA-FUNCIONAL §5).')}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Registrar', { variant: 'primary', action: 'cash-save' })}` });
  };
  A['cash-save'] = function () { var c = S.wiz.cash; if (!c.desc || !c.amount) { App.toast('Completa la descripción y el monto.', 'bad'); return; } cashMoves.unshift({ id: 'cm' + Date.now(), date: D.TODAY, desc: c.desc, kind: c.kind, amount: c.amount }); S.tabs.treasury = 'cash'; App.closeDialog(); App.render(); App.toast('Movimiento registrado.'); };
  A['stmt-upload'] = function () {
    App.dialog({ title: 'Subir cartola bancaria', body: html`<p>Sube la cartola en PDF o Excel si tu banco no está conectado.</p><label class="card card-pad" style="display:flex;flex-direction:column;align-items:center;gap:8px;border-style:dashed;margin-top:12px;cursor:pointer">${icon('upload', 24)}<strong>Elegir archivo</strong><input type="file" class="sr-only"></label>
      <div style="margin-top:12px">${C.alert('warning', 'Sin backend', 'La subida de cartolas es simulada y no existe un endpoint para recibirla (MAPA-FUNCIONAL §5). Conectar el banco es la alternativa disponible.')}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Conectar banco', { variant: 'primary', href: '#/banking', attrs: 'data-action="dialog-close"' })}` });
  };

  // ======================= Bancos =======================
  V.banking = function () {
    return {
      subtitle: 'Cuentas conectadas por Open Banking. Sus movimientos alimentan la conciliación.', endpoint: '/banking/connections',
      actions: C.btn('Conectar banco', { variant: 'primary', icon: 'plus', action: 'bank-connect' }),
      empty: { icon: 'bank', title: 'No hay bancos conectados', text: 'Conecta tu banco para traer los movimientos automáticamente y conciliarlos.', action: C.btn('Conectar banco', { variant: 'primary', icon: 'plus', action: 'bank-connect' }) },
      body: html`<div class="stack"><div class="grid g2">${D.banks.map(function (b) {
        var bad = b.status !== 'Conectada';
        return html`<div class="card card-pad stack-sm"><div class="row-between"><div class="row">${icon('bank', 20)}<strong>${b.bank}</strong></div>${C.badge(b.status, bad ? 'warning' : 'success', bad ? 'alert' : 'checkCircle')}</div>
          <span class="muted">${b.acc}</span><div style="font-size:24px;font-weight:700" class="money">${U.clp(b.balance)}</div><span class="small muted">Última sincronización: ${U.dateTime(b.sync)}</span>
          <div class="row">${bad ? C.btn('Volver a autorizar', { size: 'sm', variant: 'primary', action: 'bank-reauth', attrs: 'data-id="' + b.id + '"' }) : C.btn('Sincronizar ahora', { size: 'sm', icon: 'refresh', action: 'bank-sync', attrs: 'data-id="' + b.id + '"', loading: App.isBusy('sync-' + b.id), loadingLabel: 'Sincronizando…' })}${C.btn('Ver movimientos', { size: 'sm', variant: 'ghost', href: '#/banking/reconciliation' })}</div></div>`;
      })}</div>
      ${C.alert('info', null, 'Esta pantalla existe y funciona, pero hoy no tiene acceso desde el menú. En el rediseño está junto a la conciliación (PLAN §3.1).')}</div>`
    };
  };
  A['bank-sync'] = function (el) { var id = el.getAttribute('data-id'); App.busy('sync-' + id, 1000, function () { D.banks.filter(function (b) { return b.id === id; })[0].sync = new Date().toISOString(); App.toast('Movimientos sincronizados. 2 nuevos por conciliar.'); }); };
  A['bank-reauth'] = function (el) { var b = D.banks.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; S.ui.bank = { step: 1, bank: b.bank, reauth: b.id }; App.dialog({ title: 'Volver a autorizar ' + b.bank, body: bankBody, actions: bankActions }); };
  A['bank-connect'] = function () { S.ui.bank = { step: 0, bank: 'Banco Araucaria' }; App.dialog({ title: 'Conectar banco', body: bankBody, actions: bankActions }); };
  function bankBody() {
    var s = S.ui.bank;
    var steps = C.stepper([{ label: 'Banco' }, { label: 'Autorizar' }, { label: 'Listo' }], s.step);
    if (s.step === 0) return html`${steps}${C.field('Banco', C.select('bk-b', ['Banco Araucaria', 'Banco Cumbre', 'Banco Litoral', 'Banco Austral Andino'], s.bank, { attrs: 'data-bind="ui.bank.bank"' }), { id: 'bk-b' })}<p class="small muted" style="margin-top:8px">Solo pedimos permiso de lectura: no podemos hacer pagos ni transferencias.</p>`;
    if (s.step === 1) return html`${steps}<p>Te llevaremos al sitio de <strong>${s.bank}</strong> para que autorices el acceso de solo lectura a tus cuentas por 90 días.</p><ul style="padding-left:18px;margin:8px 0"><li>Saldos de tus cuentas</li><li>Movimientos de los últimos 12 meses</li></ul><p class="small muted">Puedes revocar el acceso en cualquier momento desde tu banco o desde esta pantalla.</p>`;
    return html`${steps}${C.alert('success', s.bank + ' conectado', 'Trajimos 124 movimientos de la cuenta corriente. 2 están pendientes de conciliar.')}`;
  }
  function bankActions() {
    var s = S.ui.bank;
    if (s.step === 0) return html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Continuar', { variant: 'primary', action: 'bank-step' })}`;
    if (s.step === 1) return html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn(App.isBusy('bk') ? 'Esperando autorización…' : 'Ir a ' + s.bank, { variant: 'primary', icon: App.isBusy('bk') ? null : 'external', action: 'bank-auth', loading: App.isBusy('bk') })}`;
    return html`${C.btn('Cerrar', { action: 'dialog-close' })}${C.btn('Ir a conciliar', { variant: 'primary', action: 'bank-done' })}`;
  }
  A['bank-step'] = function () { S.ui.bank.step = 1; App.refreshOverlay(); };
  A['bank-auth'] = function () {
    S.ui.busy_bk = true; App.refreshOverlay();
    setTimeout(function () {
      S.ui.busy_bk = false; var s = S.ui.bank; s.step = 2;
      if (s.reauth) { D.banks.filter(function (b) { return b.id === s.reauth; })[0].status = 'Conectada'; }
      else if (!D.banks.some(function (b) { return b.bank === s.bank; })) D.banks.push({ id: 'bk' + Date.now(), bank: s.bank, acc: 'Cuenta corriente ****' + String(Math.floor(Math.random() * 9000 + 1000)), balance: 7350200, sync: new Date().toISOString(), status: 'Conectada' });
      App.render(); App.refreshOverlay();
    }, 1200);
  };
  A['bank-done'] = function () { App.closeDialog(); App.go('/banking/reconciliation'); };

  // ======================= Conciliación bancaria =======================
  V.reconciliation = function () {
    var w = S.wiz.rec = S.wiz.rec || { selB: null, selL: null, done: {}, ignored: {} };
    var pendB = D.bankMoves.filter(function (m) { return !w.done[m.id] && !w.ignored[m.id]; });
    var usedL = {}; Object.keys(w.done).forEach(function (k) { usedL[w.done[k]] = true; });
    var pendL = D.ledgerRecords.filter(function (r) { return !usedL[r.id]; });
    var total = D.bankMoves.length, ok = Object.keys(w.done).length + Object.keys(w.ignored).length;
    var suggestions = pendB.filter(function (m) { return m.match && !usedL[m.match]; });
    var selB = D.bankMoves.filter(function (m) { return m.id === w.selB; })[0], selL = D.ledgerRecords.filter(function (r) { return r.id === w.selL; })[0];
    var diff = selB && selL ? selB.amount - selL.amount : null;
    function item(x, side, sel, suggested) {
      return html`<button type="button" class="recon-item ${suggested ? 'suggested' : ''}" aria-pressed="${sel ? 'true' : 'false'}" data-action="rec-pick" data-side="${side}" data-id="${x.id}">
        <span class="grow"><span style="display:block;font-weight:500">${x.desc}</span><span class="small muted">${U.date(x.date)}${suggested ? ', tiene una coincidencia sugerida' : ''}</span></span>
        <span class="money strong ${x.amount < 0 ? 'down' : ''}">${U.clp(x.amount)}</span></button>`;
    }
    return {
      subtitle: 'Cruza los movimientos del banco con los registros contables.', endpoint: '/banking/transactions',
      actions: html`${C.btn('Sincronizar bancos', { icon: 'refresh', href: '#/banking' })}${suggestions.length ? C.btn('Aceptar ' + suggestions.length + ' sugerencias', { variant: 'primary', icon: 'sparkles', action: 'rec-auto' }) : ''}`,
      empty: { icon: 'merge', title: 'No hay movimientos por conciliar', text: 'Conecta un banco o sincroniza para traer movimientos nuevos.', action: C.btn('Ir a Bancos', { variant: 'primary', href: '#/banking' }) },
      body: html`<div class="stack">
        <div class="card card-pad"><div class="row-between"><strong>${ok} de ${total} movimientos resueltos</strong><span class="muted">${pendB.length} pendientes</span></div><div class="meter" style="margin-top:8px" role="progressbar" aria-valuemin="0" aria-valuemax="${total}" aria-valuenow="${ok}" aria-label="Avance de la conciliación"><span class="avail" style="width:${ok / total * 100}%"></span></div></div>
        ${pendB.length ? html`<div class="recon">
          <section aria-label="Movimientos del banco"><h2 style="margin-bottom:8px">Banco <span class="muted small">(${pendB.length})</span></h2><div class="stack-sm">${pendB.map(function (m) { return item(m, 'b', w.selB === m.id, !!(m.match && !usedL[m.match])); })}</div></section>
          <section aria-label="Registros contables"><h2 style="margin-bottom:8px">Registros contables <span class="muted small">(${pendL.length})</span></h2><div class="stack-sm">${pendL.map(function (r) { return item(r, 'l', w.selL === r.id, selB && selB.match === r.id); })}</div></section>
        </div>
        <div class="card card-pad row-between" style="position:sticky;bottom:calc(var(--ctrl-h) + 8px)" aria-live="polite">
          <span>${selB && selL ? (diff === 0 ? html`Los montos coinciden.` : html`<span style="color:var(--destructive)">Diferencia de ${U.clp(diff)}.</span>`) : selB ? 'Elige el registro contable que corresponde.' : 'Elige un movimiento del banco.'}</span>
          <div class="row">${selB && !selB.match ? C.btn('Crear asiento', { action: 'rec-entry' }) : ''}${C.btn('Ignorar movimiento', { variant: 'ghost', action: 'rec-ignore', disabled: !selB })}${C.btn('Conciliar', { variant: 'primary', icon: 'merge', action: 'rec-match', disabled: !(selB && selL && diff === 0) })}</div>
        </div>` : html`<div class="card">${C.empty({ icon: 'checkCircle', title: 'Conciliación al día', text: 'Todos los movimientos del banco están conciliados o ignorados.' })}</div>`}
      </div>`
    };
  };
  A['rec-pick'] = function (el) { var w = S.wiz.rec, side = el.getAttribute('data-side'), id = el.getAttribute('data-id'); if (side === 'b') { w.selB = w.selB === id ? null : id; w.selL = null; } else w.selL = w.selL === id ? null : id; App.render(); };
  A['rec-match'] = function () { var w = S.wiz.rec; w.done[w.selB] = w.selL; w.selB = w.selL = null; App.render(); App.toast('Movimiento conciliado.'); };
  A['rec-ignore'] = function () { var w = S.wiz.rec; w.ignored[w.selB] = true; w.selB = w.selL = null; App.render(); App.toast('Movimiento ignorado.', 'info'); };
  A['rec-entry'] = function () { var w = S.wiz.rec, m = D.bankMoves.filter(function (x) { return x.id === w.selB; })[0]; w.done[w.selB] = 'new-' + m.id; w.selB = null; App.render(); App.toast('Asiento creado en ' + (m.amount < 0 ? 'Gastos bancarios' : 'Otros ingresos') + ' y conciliado.'); };
  A['rec-auto'] = function () { var w = S.wiz.rec, n = 0; D.bankMoves.forEach(function (m) { if (m.match && !w.done[m.id] && !w.ignored[m.id]) { w.done[m.id] = m.match; n++; } }); w.selB = w.selL = null; App.render(); App.toast(n + ' movimientos conciliados con las sugerencias.'); };

  // ======================= Remuneraciones =======================
  V.payroll = function () {
    var w = S.wiz.pay = S.wiz.pay || { period: '2026-10', calc: true };
    var E = D.employees, sum = function (k) { return E.reduce(function (s, e) { return s + e[k]; }, 0); };
    var haberes = sum('base') + sum('grat');
    return {
      subtitle: 'Cálculo mensual, liquidaciones y archivo para Previred.', endpoint: '/payroll/employees',
      actions: html`${C.btn('Archivo Previred', { icon: 'download', action: 'toast', attrs: 'data-msg="Archivo Previred de octubre descargado."' })}${C.btn('Calcular remuneraciones', { variant: 'primary', icon: 'calculator', action: 'pay-calc', loading: App.isBusy('pay'), loadingLabel: 'Calculando…' })}`,
      empty: { icon: 'users', title: 'Aún no hay trabajadores', text: 'Agrega a tus trabajadores para calcular sus remuneraciones.' },
      body: html`<div class="stack">
        <div class="row" style="align-items:flex-end">${C.periodPicker('pay-per', 2026, 10, { value: w.period, change: 'pay-period', label: 'Período de remuneraciones' })}<span class="small muted" style="padding-bottom:8px">Hoy el cálculo usa siempre el período 1, fijo en el código.</span></div>
        <div class="grid g4">${C.kpi('Trabajadores', String(E.length))}${C.kpi('Total haberes', U.clp(haberes))}${C.kpi('Descuentos legales', U.clp(sum('afp') + sum('salud') + sum('ces') + sum('tax')))}${C.kpi('Total líquido a pagar', U.clp(sum('liquido')))}</div>
        ${C.alert('warning', null, 'Hoy la lista de trabajadores no carga: GET /payroll/employees no existe (MAPA-FUNCIONAL §4). El PDF de la liquidación tampoco.')}
        ${C.dataTable({ id: 'pay', search: 'Buscar por nombre o RUT', searchKeys: ['name', 'rut'], caption: 'Remuneraciones del período', columns: [
          { key: 'name', label: 'Trabajador', render: function (r) { return html`<span class="cell-main">${r.name}</span><div class="cell-sub">${r.rut}, ${r.role}</div>`; } },
          { key: 'base', label: 'Sueldo base', num: true, render: function (r) { return U.clp(r.base); } },
          { key: 'grat', label: 'Gratificación', num: true, render: function (r) { return U.clp(r.grat); } },
          { key: 'afp', label: 'AFP', num: true, render: function (r) { return U.clp(-r.afp); } },
          { key: 'salud', label: 'Salud', num: true, render: function (r) { return html`${U.clp(-r.salud)}<div class="cell-sub">${r.health}</div>`; } },
          { key: 'tax', label: 'Impuesto único', num: true, render: function (r) { return U.clp(-r.tax); } },
          { key: 'liquido', label: 'Líquido', num: true, render: function (r) { return html`<strong>${U.clp(r.liquido)}</strong>`; } },
          { key: 'a', label: '', sort: false, render: function (r) { return C.btn('Liquidación', { size: 'sm', variant: 'ghost', icon: 'download', action: 'toast', attrs: 'data-msg="Liquidación de ' + r.name + ' descargada."' }); } }
        ], rows: E, totals: function (rows) { return { base: U.clp(rows.reduce(function (s, r) { return s + r.base; }, 0)), liquido: U.clp(rows.reduce(function (s, r) { return s + r.liquido; }, 0)) }; } }, S.tables.pay)}
      </div>`
    };
  };
  CH['pay-period'] = function (el) { S.wiz.pay.period = el.value; App.render(); };
  A['pay-calc'] = function () { var p = S.wiz.pay.period.split('-'); App.busy('pay', 1100, function () { App.toast('Remuneraciones de ' + U.period(Number(p[0]), Number(p[1])).toLowerCase() + ' calculadas.'); }); };

  // ======================= Portal del empleado =======================
  V.employee = function () {
    var e = D.employees[4], tab = S.tabs['portal-employee'] || 'payslips';
    var role = window.REG.effectiveRole(S.role);
    var body;
    if (tab === 'payslips') body = html`<div class="stack">
      ${role === 'USER' ? C.alert('warning', 'Hoy esta pantalla no funciona para empleados', 'El backend solo permite ver liquidaciones a Administración y Personas (PayrollController.java:20). El rediseño requiere ese cambio (PLAN B-07).') : ''}
      <div class="grid g-main">
        <div class="card"><div class="card-head"><h2>Mis liquidaciones</h2></div>
          <table class="dt"><thead><tr><th scope="col">Período</th><th scope="col">Pagada el</th><th scope="col" class="num">Líquido</th><th scope="col"><span class="sr-only">Descargar</span></th></tr></thead><tbody>
          ${D.payslips.map(function (p) { return html`<tr><td class="cell-main">${U.period(p.period[0], p.period[1])}</td><td>${U.date(p.paid)}</td><td class="num">${U.clp(e.liquido)}</td><td>${C.btn('Descargar PDF', { size: 'sm', variant: 'ghost', icon: 'download', action: 'toast', attrs: 'data-msg="Liquidación de ' + U.period(p.period[0], p.period[1]).toLowerCase() + ' descargada."' })}</td></tr>`; })}
          </tbody></table></div>
        <div class="card card-pad stack-sm"><h3>Octubre 2026</h3>
          <div class="totals"><div><span>Sueldo base</span><span>${U.clp(e.base)}</span></div><div><span>Gratificación</span><span>${U.clp(e.grat)}</span></div><div><span>AFP</span><span>${U.clp(-e.afp)}</span></div><div><span>Salud (${e.health})</span><span>${U.clp(-e.salud)}</span></div><div><span>Seguro de cesantía</span><span>${U.clp(-e.ces)}</span></div><div class="grand"><span>Líquido</span><span>${U.clp(e.liquido)}</span></div></div>
        </div>
      </div></div>`;
    else body = html`<div class="card card-pad" style="max-width:560px"><dl style="display:grid;grid-template-columns:auto 1fr;gap:8px 16px;margin:0">
      <dt class="muted">Nombre</dt><dd style="margin:0">${e.name}</dd><dt class="muted">RUT</dt><dd style="margin:0">${e.rut}</dd><dt class="muted">Cargo</dt><dd style="margin:0">${e.role}</dd>
      <dt class="muted">Fecha de ingreso</dt><dd style="margin:0">${U.date(e.hire)}</dd><dt class="muted">Previsión de salud</dt><dd style="margin:0">${e.health}</dd></dl>
      <p class="small muted" style="margin-top:12px">Para cambiar tus datos, escribe al área de Personas.</p></div>`;
    return { title: tab === 'payslips' ? 'Mis liquidaciones' : 'Mis datos', hideTitle: false, endpoint: '/payroll/employee/payslips', body: body };
  };
})();
