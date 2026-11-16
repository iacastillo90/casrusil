/* Ventas y compras: facturas (lista, emisión, detalle), cotizaciones, oportunidades, órdenes de compra y honorarios. */
(function () {
  'use strict';
  var U = window.U, C = window.C, D = window.D, html = U.html, icon = U.icon, raw = U.raw, V = window.V, A = window.A, CH = window.CH, S = window.S;

  function tipoCorto(t) { return { 33: 'Factura', 34: 'Factura exenta', 39: 'Boleta', 52: 'Guía de despacho', 56: 'Nota de débito', 61: 'Nota de crédito' }[t] || D.TIPOS[t]; }
  function docName(i) { return tipoCorto(i.tipo) + (i.folio ? ' N° ' + U.int(i.folio) : ' (borrador)'); }
  function toIso(s) { var p = String(s || '').split('-'); return p.length === 3 && p[2].length === 4 ? p[2] + '-' + p[1] + '-' + p[0] : D.TODAY; }
  function addDays(iso, n) { var d = new Date(iso + 'T12:00:00'); d.setDate(d.getDate() + n); return d.toISOString().slice(0, 10); }
  function cafFor(tipo) {
    var list = D.cafs.filter(function (c) { return c.tipo === Number(tipo) && c.used < c.to - c.from + 1; });
    if (!list.length) return null;
    var c = list[0]; return { caf: c, next: c.from + c.used, avail: list.reduce(function (s, x) { return s + (x.to - x.from + 1 - x.used); }, 0) };
  }
  function nextId() { return 'n' + Date.now().toString(36); }
  window.VENTAS = { docName: docName, tipoCorto: tipoCorto, cafFor: cafFor };

  // ======================= Lista de facturas =======================
  V.invoices = function () {
    var inv = D.invoices;
    var month = inv.filter(function (i) { return i.date >= '2026-11-01' && ['ACCEPTED', 'ACCEPTED_WITH_OBJECTIONS', 'SENT'].indexOf(i.status) >= 0 && i.tipo !== 61; });
    var sent = inv.filter(function (i) { return i.status === 'SENT'; });
    var rej = inv.filter(function (i) { return i.status === 'REJECTED'; });
    var st = S.tables.inv || {};
    var table = C.dataTable({
      id: 'inv', caption: 'Documentos emitidos', search: 'Buscar por folio, cliente o RUT',
      searchKeys: [function (r) { return r.folio; }, function (r) { return D.party(r.party).name; }, function (r) { return D.party(r.party).rut; }],
      filters: [
        { key: 'status', label: 'Estado', options: Object.keys(C.DTE).map(function (k) { return [k, C.DTE[k].label]; }) },
        { key: 'tipo', label: 'Tipo', options: [['33', 'Factura'], ['34', 'Factura exenta'], ['39', 'Boleta'], ['61', 'Nota de crédito']] }
      ],
      columns: [
        { key: 'folio', label: 'Documento', sortGet: function (r) { return r.folio || 0; }, render: function (r) { return html`<span class="cell-main">${docName(r)}</span>`; } },
        { key: 'party', label: 'Cliente', sortGet: function (r) { return D.party(r.party).name; }, render: function (r) { var p = D.party(r.party); return html`<div>${p.name}</div><div class="cell-sub">${p.rut}</div>`; } },
        { key: 'date', label: 'Emisión', render: function (r) { return U.date(r.date); } },
        { key: 'due', label: 'Vencimiento', render: function (r) { return U.date(r.due); } },
        { key: 'total', label: 'Total', num: true, render: function (r) { return html`<span class="money">${U.clp(r.tipo === 61 ? -r.total : r.total)}</span>`; } },
        { key: 'status', label: 'Estado', render: function (r) { return C.status(r.status); } }
      ],
      rows: inv.slice().sort(function (a, b) { return a.date < b.date ? 1 : -1; }), rowHref: function (r) { return '#/invoices/' + r.id; },
      empty: { icon: 'receipt', title: 'Aún no has emitido documentos', text: 'Emite tu primera factura o importa las que ya tienes desde un archivo CSV.', action: C.btn('Emitir documento', { variant: 'primary', icon: 'plus', href: '#/invoices/new?new=1' }) }
    }, st);
    return {
      subtitle: 'Facturas, boletas y notas emitidas, con su estado en el SII.',
      actions: html`${C.btn('Importar CSV', { icon: 'upload', action: 'inv-import' })}${C.btn('Emitir documento', { variant: 'primary', icon: 'plus', href: '#/invoices/new?new=1' })}`,
      endpoint: '/invoices',
      empty: { icon: 'receipt', title: 'Aún no has emitido documentos', text: 'Emite tu primera factura o importa las que ya tienes desde un archivo CSV.', action: C.btn('Emitir documento', { variant: 'primary', icon: 'plus', href: '#/invoices/new?new=1' }) },
      body: html`<div class="stack">
        <div class="grid g4">
          ${C.kpi('Emitido en noviembre', U.clp(month.reduce(function (s, i) { return s + i.total; }, 0)), month.length + ' documentos')}
          ${C.kpi('Por cobrar', U.clp(inv.filter(function (i) { return (i.status === 'ACCEPTED' || i.status === 'ACCEPTED_WITH_OBJECTIONS') && i.due >= D.TODAY && i.tipo !== 61; }).reduce(function (s, i) { return s + i.total; }, 0)), 'Documentos aceptados no vencidos')}
          ${C.kpi('Sin respuesta del SII', String(sent.length), sent.length ? 'Enviados, sin aceptación ni rechazo' : 'Todo respondido', { tone: sent.length ? '' : 'up', ic: 'hourglass' })}
          ${C.kpi('Rechazados', String(rej.length), rej.length ? 'Requieren corrección' : 'Sin rechazos', { tone: rej.length ? 'down' : 'up', ic: rej.length ? 'xCircle' : 'checkCircle' })}
        </div>
        ${rej.length ? C.alert('destructive', 'El SII rechazó ' + rej.length + (rej.length > 1 ? ' documentos' : ' documento'), 'Corrige y vuelve a emitir para que tengan validez tributaria.', C.btn('Ver ' + docName(rej[0]), { size: 'sm', href: '#/invoices/' + rej[0].id })) : ''}
        ${table}
      </div>`
    };
  };
  A['inv-import'] = function () {
    S.ui.imp = { step: 0 };
    App.dialog({ title: 'Importar documentos desde CSV', body: importBody, actions: importActions });
  };
  function importBody() {
    var s = S.ui.imp;
    if (s.step === 0) return html`<p>Sube un archivo CSV con tus documentos emitidos. Revisaremos cada fila antes de importar.</p>
      <label class="card card-pad" style="display:flex;flex-direction:column;align-items:center;gap:8px;border-style:dashed;margin-top:12px;cursor:pointer">${icon('upload', 24)}<strong>Elegir archivo CSV</strong><span class="small muted">Columnas: tipo, folio, RUT receptor, fecha, neto, IVA, total</span><input type="file" accept=".csv" class="sr-only" data-action-change="imp-file"></label>
      <div class="row" style="justify-content:center;margin-top:8px">${C.btn('Usar archivo de ejemplo', { variant: 'link', action: 'imp-sample' })}</div>
      <div style="margin-top:12px">${C.alert('info', null, 'Hoy esta importación es simulada en el producto, aunque el backend ya tiene POST /invoices/import (MAPA-FUNCIONAL §5).')}</div>`;
    return html`<p><strong>ventas-octubre.csv</strong>: 12 filas leídas.</p>
      <div class="stack-sm" style="margin-top:12px">${C.alert('success', '10 filas listas para importar', null)}${C.alert('warning', '2 filas con problemas', 'Fila 4: el RUT 76.903.112-3 no es válido. Fila 9: el folio 1.003 ya existe. Estas filas no se importarán.')}</div>`;
  }
  function importActions() {
    return S.ui.imp.step === 0 ? C.btn('Cancelar', { action: 'dialog-close' }) : html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Importar 10 documentos', { variant: 'primary', action: 'imp-go' })}`;
  }
  A['imp-sample'] = function () { S.ui.imp.step = 1; App.refreshOverlay(); };
  CH['imp-file'] = function () { S.ui.imp.step = 1; App.refreshOverlay(); };
  A['imp-go'] = function () { App.closeDialog(); App.toast('Se importaron 10 documentos. 2 filas quedaron fuera.'); };

  // ======================= Emitir documento (flujo) =======================
  function initWiz(q) {
    var w = { step: 0, tipo: 33, rut: '', date: U.date(D.TODAY), term: '30', lines: [{ sku: 'HAR-025', desc: 'Harina especial 25 kg', qty: 10, price: 18900, exempt: false }], ref: '', motivo: 'Anula documento', errors: {} };
    if (q.tipo) w.tipo = Number(q.tipo);
    if (q.ref) { var r = D.invById(q.ref); if (r) { w.ref = r.id; w.rut = D.party(r.party).rut; w.lines = r.lines.map(function (l) { return { sku: l.sku, desc: l.desc, qty: l.qty, price: l.price, exempt: l.exempt }; }); } }
    if (q.from) {
      var f = D.invById(q.from);
      if (f) { w.tipo = f.tipo; w.rut = D.party(f.party).rut; w.lines = f.lines.map(function (l) { return { sku: l.sku, desc: l.desc, qty: l.qty, price: l.price, exempt: l.exempt }; }); w.fromId = f.status === 'DRAFT' ? f.id : null; w.retryOf = f.status === 'REJECTED' ? f.id : null; }
      var qt = D.quotes.filter(function (x) { return x.id === q.from; })[0];
      if (qt) { w.rut = D.party(qt.party).rut; w.fromQuote = qt.id; w.lines = [{ sku: 'CAF-001', desc: 'Café en grano 1 kg', qty: Math.round(qt.total / 1.19 / 15990), price: 15990, exempt: false }]; }
    }
    return w;
  }
  function wizParty(w) { var c = U.cleanRut(w.rut); return D.parties.filter(function (p) { return U.cleanRut(p.rut) === c; })[0]; }
  function wizTotals(w) {
    var neto = 0, exento = 0;
    w.lines.forEach(function (l) { var t = (l.qty || 0) * (l.price || 0); if (l.exempt || w.tipo === 34) exento += t; else neto += t; });
    var iva = w.tipo === 34 ? 0 : Math.round(neto * 0.19);
    return { neto: neto, exento: exento, iva: iva, total: neto + exento + iva };
  }
  function wizAsInvoice(w) {
    var p = wizParty(w), t = wizTotals(w), iso = toIso(w.date);
    return { tipo: w.tipo, folio: w.folio || null, party: p ? p.id : 'p1', date: iso, due: addDays(iso, Number(w.term) || 0), payment: w.term === '0' ? 'Contado' : 'Crédito ' + w.term + ' días',
      status: w.signed ? 'SIGNED' : 'DRAFT', lines: w.lines.map(function (l, i) { return { n: i + 1, sku: l.sku, desc: l.desc, qty: l.qty, unit: 'u', price: l.price, exempt: l.exempt }; }), neto: t.neto, exento: t.exento, iva: t.iva, total: t.total };
  }
  function validate(w, step) {
    var e = {};
    if (step === 0) {
      if (!w.rut) e.rut = 'Ingresa el RUT del receptor.'; else if (!U.validRut(w.rut)) e.rut = 'El dígito verificador no corresponde. Revisa el RUT.'; else if (!wizParty(w)) e.rut = 'Este RUT no está en tus clientes. Agrégalo en Cuentas corrientes antes de emitir.';
      if (!/^\d{2}-\d{2}-\d{4}$/.test(w.date)) e.date = 'Usa el formato dd-mm-aaaa.';
      if (!cafFor(w.tipo)) e.tipo = 'No hay folios disponibles para este tipo de documento.';
      if (w.tipo === 61 && !w.ref) e.ref = 'Elige el documento que corrige o anula esta nota.';
    }
    if (step === 1) {
      if (!w.lines.length) e.lines = 'Agrega al menos una línea.';
      w.lines.forEach(function (l, i) { if (!l.desc) e['d' + i] = 'Falta la descripción.'; if (!(l.qty > 0)) e['q' + i] = 'La cantidad debe ser mayor que 0.'; if (!(l.price > 0)) e['p' + i] = 'El precio debe ser mayor que $0.'; });
    }
    return e;
  }

  V['invoice-new'] = function (ctx) {
    var q = ctx.query;
    var key = JSON.stringify(q);
    if (!S.wiz.inv || q.new || S.wiz.invKey !== key) { S.wiz.inv = initWiz(q); S.wiz.invKey = key; if (q.new) { App.replaceRoute('/invoices/new'); S.wiz.invKey = '{}'; } }
    var w = S.wiz.inv;
    var steps = [{ label: 'Receptor', sub: 'Tipo y cliente' }, { label: 'Detalle', sub: 'Productos y montos' }, { label: 'Revisar', sub: 'Vista previa' }, { label: 'Firmar y enviar', sub: 'Certificado y SII' }];
    var t = wizTotals(w), body;
    var cf = cafFor(w.tipo);

    if (w.step === 0) {
      var p = wizParty(w);
      body = html`<div class="grid g-main">
        <div class="card card-pad stack">
          <div class="grid g2">
            ${C.field('Tipo de documento', C.select('w-tipo', [[33, 'Factura electrónica (33)'], [34, 'Factura exenta (34)'], [39, 'Boleta electrónica (39)'], [56, 'Nota de débito (56)'], [61, 'Nota de crédito (61)'], [52, 'Guía de despacho (52)']], w.tipo, { attrs: 'data-bind="wiz.inv.tipo" data-type="int" data-rerender' }), { id: 'w-tipo', req: true, err: w.errors.tipo })}
            <div class="field"><span class="label">Folio</span><div class="input" style="display:flex;align-items:center;background:var(--muted)" aria-live="polite">${cf ? html`Próximo disponible: <strong style="margin-left:4px">N° ${U.int(cf.next)}</strong>` : html`<span style="color:var(--destructive)">Sin folios</span>`}</div><span class="hint">Lo asigna el sistema al firmar. ${cf ? U.int(cf.avail) + ' disponibles.' : ''}</span></div>
          </div>
          ${!cf ? C.alert('destructive', 'No hay folios para ' + D.TIPOS[w.tipo].toLowerCase(), 'Solicita un CAF en el sitio del SII y cárgalo para poder emitir este tipo de documento.', C.btn('Ir a Folios (CAF)', { size: 'sm', href: '#/sii/caf' })) : (cf.avail < 100 ? C.alert('warning', 'Quedan ' + cf.avail + ' folios', 'Solicita un nuevo CAF pronto para no interrumpir la emisión.', C.btn('Ver folios', { size: 'sm', href: '#/sii/caf' })) : '')}
          ${C.rutField('w-rut', 'RUT del receptor', w.rut, { req: true, attrs: 'list="clientes-rut" data-bind="wiz.inv.rut" data-rerender', hint: 'Escribe el RUT o elige un cliente de la lista.', okText: p ? 'Cliente: ' + p.name : 'RUT válido' })}
          <datalist id="clientes-rut">${D.parties.filter(function (x) { return x.kind === 'Cliente'; }).map(function (x) { return html`<option value="${x.rut}">${x.name}</option>`; })}</datalist>
          ${w.errors.rut && w.rut && U.validRut(w.rut) ? html`<span class="small" style="color:var(--destructive)">${w.errors.rut}</span>` : ''}
          ${w.errors.rut && !w.rut ? html`<span class="small" style="color:var(--destructive)">${w.errors.rut}</span>` : ''}
          ${w.tipo === 61 || w.tipo === 56 ? html`<div class="grid g2">
            ${C.field('Documento de referencia', C.select('w-ref', [['', 'Elige un documento']].concat(D.invoices.filter(function (i) { return (i.status === 'ACCEPTED' || i.status === 'ACCEPTED_WITH_OBJECTIONS') && i.tipo === 33; }).map(function (i) { return [i.id, docName(i) + ', ' + D.party(i.party).name]; })), w.ref, { attrs: 'data-bind="wiz.inv.ref" data-rerender' }), { id: 'w-ref', req: true, err: w.errors.ref })}
            ${C.field('Motivo', C.select('w-mot', ['Anula documento', 'Corrige montos', 'Corrige texto'], w.motivo, { attrs: 'data-bind="wiz.inv.motivo"' }), { id: 'w-mot' })}
          </div>` : ''}
          <div class="grid g2">
            ${C.field('Fecha de emisión', C.input('w-date', w.date, { placeholder: 'dd-mm-aaaa', err: w.errors.date, attrs: 'data-bind="wiz.inv.date"' }), { id: 'w-date', req: true, err: w.errors.date, hint: w.errors.date ? null : 'Formato dd-mm-aaaa.' })}
            ${C.field('Forma de pago', C.select('w-term', [['0', 'Contado'], ['15', 'Crédito 15 días'], ['30', 'Crédito 30 días'], ['60', 'Crédito 60 días']], w.term, { attrs: 'data-bind="wiz.inv.term" data-rerender' }), { id: 'w-term', hint: 'Vence el ' + U.date(addDays(toIso(w.date), Number(w.term))) + '.' })}
          </div>
        </div>
        <div class="stack">
          ${p ? html`<div class="card card-pad stack-sm"><h3>${p.name}</h3><span class="muted">${p.rut}</span><span class="muted">${p.city}, ${p.email}</span><span class="small muted">Saldo pendiente: ${U.clp(D.invoices.filter(function (i) { return i.party === p.id && (i.status === 'ACCEPTED' || i.status === 'ACCEPTED_WITH_OBJECTIONS'); }).reduce(function (s, i) { return s + i.total; }, 0))}</span></div>` : html`<div class="card card-pad muted">Elige un cliente para ver sus datos.</div>`}
          ${w.retryOf ? C.alert('info', 'Corrección de un documento rechazado', 'Este documento reemplaza a ' + docName(D.invById(w.retryOf)) + ' y usará un folio nuevo.') : ''}
          ${w.fromQuote ? C.alert('info', 'Desde la cotización N° ' + D.quotes.filter(function (x) { return x.id === w.fromQuote; })[0].n, 'Revisa las líneas antes de continuar. (Propuesta nueva)') : ''}
        </div>
      </div>`;
    } else if (w.step === 1) {
      body = html`<div class="grid g-main">
        <div class="card"><div class="card-head"><h2>Líneas del documento</h2><span class="spacer"></span>${C.btn('Agregar línea', { size: 'sm', icon: 'plus', action: 'line-add' })}</div>
          <div class="table-wrap"><table class="dt"><thead><tr><th scope="col">Producto o servicio</th><th scope="col" class="num" style="width:110px">Cantidad</th><th scope="col" class="num" style="width:150px">Precio unitario</th>${w.tipo === 33 ? html`<th scope="col" style="width:80px">Exento</th>` : ''}<th scope="col" class="num">Subtotal</th><th scope="col"><span class="sr-only">Quitar</span></th></tr></thead>
          <tbody>${w.lines.map(function (l, i) {
            return html`<tr>
              <td><label class="sr-only" for="l-p-${i}">Producto línea ${i + 1}</label>${C.select('l-p-' + i, [['', 'Otro (escribir)']].concat(D.products.map(function (p) { return [p.sku, p.name]; })), l.sku, { attrs: 'data-action-change="line-product" data-i="' + i + '"' })}
                ${!l.sku ? html`<label class="sr-only" for="l-d-${i}">Descripción línea ${i + 1}</label><input class="input" style="margin-top:6px" id="l-d-${i}" value="${l.desc}" placeholder="Descripción" data-bind="wiz.inv.lines.${i}.desc" ${w.errors['d' + i] ? raw('aria-invalid="true"') : ''}>` : ''}
                ${w.errors['d' + i] ? html`<span class="small" style="color:var(--destructive)">${w.errors['d' + i]}</span>` : ''}</td>
              <td class="num"><label class="sr-only" for="l-q-${i}">Cantidad línea ${i + 1}</label><input class="input money" id="l-q-${i}" inputmode="numeric" value="${l.qty}" data-bind="wiz.inv.lines.${i}.qty" data-type="int" data-live="1" data-rerender ${w.errors['q' + i] ? raw('aria-invalid="true"') : ''}>${w.errors['q' + i] ? html`<span class="small" style="color:var(--destructive)">${w.errors['q' + i]}</span>` : ''}</td>
              <td class="num"><label class="sr-only" for="l-pr-${i}">Precio línea ${i + 1}</label><div class="input-group"><span class="prefix">$</span><input class="input money" id="l-pr-${i}" inputmode="numeric" value="${U.int(l.price)}" data-money="1" data-bind="wiz.inv.lines.${i}.price" data-type="money" data-live="1" data-rerender ${w.errors['p' + i] ? raw('aria-invalid="true"') : ''}></div>${w.errors['p' + i] ? html`<span class="small" style="color:var(--destructive)">${w.errors['p' + i]}</span>` : ''}</td>
              ${w.tipo === 33 ? html`<td><label class="check"><input type="checkbox" ${l.exempt ? raw('checked') : ''} data-bind="wiz.inv.lines.${i}.exempt" data-rerender><span class="sr-only">Línea ${i + 1} exenta de IVA</span></label></td>` : ''}
              <td class="num money">${U.clp((l.qty || 0) * (l.price || 0))}</td>
              <td>${w.lines.length > 1 ? C.iconBtn('trash', 'Quitar línea ' + (i + 1), 'line-del', 'data-i="' + i + '"') : ''}</td>
            </tr>`;
          })}</tbody></table></div>
          ${w.errors.lines ? html`<p class="small" style="color:var(--destructive);padding:8px 16px">${w.errors.lines}</p>` : ''}
        </div>
        <div class="card card-pad stack" aria-live="polite"><h3>Totales</h3><div class="totals">
          ${t.exento ? html`<div><span>Monto exento</span><span>${U.clp(t.exento)}</span></div>` : ''}
          <div><span>Monto neto</span><span>${U.clp(t.neto)}</span></div>
          <div><span>IVA 19%</span><span>${U.clp(t.iva)}</span></div>
          <div class="grand"><span>Total</span><span>${U.clp(t.total)}</span></div></div>
          <p class="small muted">El IVA se calcula sobre el monto neto y se redondea al peso.</p>
        </div>
      </div>`;
    } else if (w.step === 2) {
      var preview = wizAsInvoice(w);
      body = html`<div class="grid g-main">
        <div>${C.docPreview(preview, D)}</div>
        <div class="stack">
          <div class="card card-pad stack-sm"><h3>Antes de firmar</h3>
            <p class="muted">Revisa receptor, montos y forma de pago. Al firmar se asigna el folio N° ${cf ? U.int(cf.next) : '—'} y se genera el timbre electrónico; después no podrás editar el documento.</p>
            <div class="totals" style="margin-top:8px"><div><span>Neto</span><span>${U.clp(t.neto)}</span></div><div><span>IVA 19%</span><span>${U.clp(t.iva)}</span></div><div class="grand"><span>Total</span><span>${U.clp(t.total)}</span></div></div>
          </div>
          <div class="card card-pad stack-sm"><h3>Certificado digital</h3><p class="muted">${D.company.cert.holder}, ${U.rut(D.company.cert.holderRut)}</p><p class="small muted">Vence el ${U.date(D.company.cert.validUntil)}.</p></div>
          ${C.alert('info', null, 'Hoy el certificado no informa el RUT de su titular al sistema (RIESGOS R-10); el rediseño lo muestra para que verifiques que corresponde.')}
        </div>
      </div>`;
    } else {
      var inv = S.wiz.invCreated;
      body = html`<div class="stack">
        ${C.dteTrack(inv)}
        <div class="grid g-main">
          <div class="card card-pad stack">
            <h2>${docName(inv)} firmado</h2>
            <p class="muted">El documento tiene folio y timbre, pero aún no tiene validez: falta enviarlo al SII. Puedes enviarlo ahora o más tarde desde la lista.</p>
            <div class="row">${C.btn('Enviar al SII', { variant: 'primary', icon: 'send', action: 'wiz-send', loading: App.isBusy('send'), loadingLabel: 'Enviando al SII…' })}${C.btn('Enviar más tarde', { href: '#/invoices/' + inv.id })}</div>
          </div>
          <div class="card card-pad stack-sm"><h3>Ambiente</h3>${S.env === 'cert' ? C.alert('info', 'Certificación', 'El envío llega al ambiente de pruebas del SII y no tiene validez tributaria.') : C.alert('warning', 'Producción', 'El envío tiene validez tributaria.')}</div>
        </div>
      </div>`;
    }
    var canBack = w.step > 0 && w.step < 3;
    var foot = w.step < 3 ? html`<div class="row-between" style="margin-top:16px">
      <div>${canBack ? C.btn('Atrás', { icon: 'chevLeft', action: 'wiz-back' }) : C.btn('Cancelar', { href: '#/invoices', variant: 'ghost' })}</div>
      <div class="row">${C.btn('Guardar borrador', { variant: 'outline', action: 'wiz-draft' })}
        ${w.step < 2 ? C.btn('Continuar', { variant: 'primary', action: 'wiz-next' }) : C.btn('Firmar documento', { variant: 'primary', icon: 'fileSignature', action: 'wiz-sign', loading: App.isBusy('sign'), loadingLabel: 'Firmando…' })}</div>
    </div>` : '';
    var title = w.tipo === 61 ? 'Emitir nota de crédito' : w.tipo === 56 ? 'Emitir nota de débito' : w.tipo === 39 ? 'Emitir boleta' : w.tipo === 52 ? 'Emitir guía de despacho' : 'Emitir factura';
    return {
      title: title, subtitle: 'El sistema asigna el folio y calcula el IVA. Nada se envía al SII hasta que lo confirmes.', loadingKind: 'form',
      body: html`${C.stepper(steps, w.step)}${body}${foot}`
    };
  };
  A['wiz-next'] = function () { var w = S.wiz.inv; w.errors = validate(w, w.step); if (Object.keys(w.errors).length) { App.render(); var f = document.querySelector('[aria-invalid="true"]'); if (f) f.focus(); App.toast('Revisa los campos marcados.', 'bad'); return; } w.step++; App.render(); };
  A['wiz-back'] = function () { var w = S.wiz.inv; w.errors = {}; w.step--; App.render(); };
  A['line-add'] = function () { S.wiz.inv.lines.push({ sku: '', desc: '', qty: 1, price: 0, exempt: false }); App.render(); var i = S.wiz.inv.lines.length - 1; var el = document.getElementById('l-p-' + i); if (el) el.focus(); };
  A['line-del'] = function (el) { S.wiz.inv.lines.splice(Number(el.getAttribute('data-i')), 1); App.render(); App.toast('Línea quitada.', 'info'); };
  CH['line-product'] = function (el) {
    var i = Number(el.getAttribute('data-i')), l = S.wiz.inv.lines[i], p = D.products.filter(function (x) { return x.sku === el.value; })[0];
    l.sku = el.value; if (p) { l.desc = p.name; l.price = p.price; } else { l.desc = ''; }
    App.render();
  };
  A['wiz-draft'] = function () {
    var w = S.wiz.inv; var inv = wizAsInvoice(w);
    if (w.fromId) { var ex = D.invById(w.fromId); Object.assign(ex, inv, { id: ex.id, status: 'DRAFT' }); inv = ex; }
    else { inv.id = nextId(); inv.history = [[new Date().toISOString(), 'DRAFT', 'Borrador guardado']]; inv.status = 'DRAFT'; D.invoices.unshift(inv); w.fromId = inv.id; }
    App.toast('Borrador guardado. Puedes retomarlo desde la lista.');
  };
  A['wiz-sign'] = function () {
    var w = S.wiz.inv, cf = cafFor(w.tipo);
    App.confirm({
      title: 'Firmar ' + tipoCorto(w.tipo).toLowerCase() + ' N° ' + U.int(cf.next),
      text: 'Se firmará con el certificado de ' + D.company.cert.holder + ' y se asignará el folio N° ' + U.int(cf.next) + '. Después de firmar no podrás editar el documento; para corregirlo tendrás que emitir una nota de crédito.',
      confirm: 'Firmar documento', icon: 'fileSignature',
      onConfirm: function () {
        App.busy('sign', 900, function () {
          cf.caf.used++;
          w.folio = cf.next; w.signed = true;
          var inv = wizAsInvoice(w);
          if (w.fromId) { var ex = D.invById(w.fromId); Object.assign(ex, inv, { id: ex.id }); inv = ex; } else { inv.id = nextId(); D.invoices.unshift(inv); }
          inv.status = 'SIGNED'; inv.ref = w.ref || null;
          inv.history = (inv.history || []).concat([[new Date().toISOString(), 'SIGNED', D.company.cert.holder + ' firmó el documento']]);
          S.wiz.invCreated = inv; w.step = 3;
          App.toast(docName(inv) + ' firmado.');
        });
      }
    });
  };
  A['wiz-send'] = function () { sendToSii(S.wiz.invCreated, true); };
  function sendToSii(inv, fromWizard) {
    App.confirm({
      title: 'Enviar ' + docName(inv).toLowerCase() + ' al SII',
      text: 'Se enviará al ambiente de ' + (S.env === 'cert' ? 'certificación (pruebas)' : 'producción') + ' del SII. El SII responderá si lo acepta, lo acepta con reparos o lo rechaza.',
      confirm: 'Enviar al SII', icon: 'send',
      onConfirm: function () {
        App.busy('send', 1200, function () {
          inv.status = 'SENT'; inv.trackId = String(248117000 + Math.floor(Math.random() * 999)).padStart(10, '0');
          inv.history.push([new Date().toISOString(), 'SENT', 'Enviado al SII. TrackID ' + inv.trackId]);
          App.toast('Enviado al SII. TrackID ' + inv.trackId + '.');
          if (inv.retryOf || (S.wiz.inv && S.wiz.inv.retryOf)) { /* sin cambios adicionales */ }
          if (fromWizard) { S.wiz.inv = null; S.wiz.invKey = null; App.go('/invoices/' + inv.id); }
        });
      }
    });
  }

  // ======================= Detalle del documento =======================
  V['invoice-detail'] = function (ctx) {
    var inv = D.invById(ctx.params.id);
    if (!inv) return { title: 'Documento no encontrado', body: html`<div class="card">${C.empty({ icon: 'search', title: 'No encontramos este documento', text: 'Puede que haya sido eliminado o que el enlace sea incorrecto.', action: C.btn('Volver a la lista', { href: '#/invoices', variant: 'primary' }) })}</div>` };
    var p = D.party(inv.party), st = inv.status, tab = S.tabs['inv-' + inv.id] || 'doc';
    var acts = [];
    if (st === 'DRAFT') acts.push(C.btn('Eliminar', { icon: 'trash', action: 'inv-delete', attrs: 'data-id="' + inv.id + '"' }), C.btn('Editar', { icon: 'pencil', href: '#/invoices/new?from=' + inv.id }), C.btn('Firmar', { variant: 'primary', icon: 'fileSignature', href: '#/invoices/new?from=' + inv.id }));
    if (st === 'SIGNED') acts.push(C.btn('Descargar PDF', { icon: 'download', action: 'toast', attrs: 'data-msg="PDF descargado."' }), C.btn('Enviar al SII', { variant: 'primary', icon: 'send', action: 'inv-send', attrs: 'data-id="' + inv.id + '"', loading: App.isBusy('send'), loadingLabel: 'Enviando…' }));
    if (st === 'SENT') acts.push(C.btn('Copiar TrackID', { icon: 'copy', action: 'toast', attrs: 'data-msg="TrackID ' + inv.trackId + ' copiado."' }), C.btn('Consultar estado', { variant: 'primary', icon: 'refresh', action: 'inv-query', attrs: 'data-id="' + inv.id + '"' }));
    if (st === 'ACCEPTED' || st === 'ACCEPTED_WITH_OBJECTIONS') acts.push(C.btn('Descargar PDF', { icon: 'download', action: 'toast', attrs: 'data-msg="PDF descargado."' }), C.btn('Enviar por email', { icon: 'mail', action: 'inv-email', attrs: 'data-id="' + inv.id + '"' }), inv.tipo !== 61 ? C.btn('Emitir nota de crédito', { variant: 'primary', icon: 'filePen', href: '#/invoices/new?tipo=61&ref=' + inv.id }) : '');
    if (st === 'REJECTED') acts.push(C.btn('Corregir y emitir de nuevo', { variant: 'primary', icon: 'refresh', action: 'inv-retry', attrs: 'data-id="' + inv.id + '"' }));
    var nc = D.invoices.filter(function (x) { return x.ref === inv.id && x.tipo === 61; })[0];
    if (st === 'CANCELLED' && nc) acts.push(C.btn('Ver nota de crédito', { href: '#/invoices/' + nc.id }));

    var banner = '';
    if (st === 'REJECTED') banner = C.alert('destructive', 'El SII rechazó este documento', inv.reason || 'Motivo no informado.', html`<span class="small">Emitir de nuevo consume un folio nuevo. El asiento contable de este documento debe reversarse.</span>`);
    if (st === 'ACCEPTED_WITH_OBJECTIONS') banner = C.alert('warning', 'Aceptado con ' + (inv.objections || []).length + ' reparos', html`<ul style="margin:4px 0 0;padding-left:18px">${(inv.objections || ['Observación informada por el SII.']).map(function (o) { return html`<li>${o}</li>`; })}</ul><span class="small">El documento es válido. Corrige los datos del cliente para los próximos documentos.</span>`);
    if (st === 'SENT') banner = html`<div class="stack-sm">${C.alert('info', 'Esperando respuesta del SII', 'Enviado con TrackID ' + inv.trackId + '. La consulta automática de estado aún no está disponible (PLAN B-02): hoy hay que revisarlo en el sitio del SII.')}
      <div class="card card-pad" style="border-style:dashed"><div class="row-between"><div><strong>Simular respuesta del SII</strong><p class="small muted">Solo en el prototipo, para recorrer los tres resultados posibles.</p></div>
      <div class="row">${C.btn('Aceptado', { size: 'sm', icon: 'checkCircle', action: 'sim-sii', attrs: 'data-id="' + inv.id + '" data-res="ACCEPTED"' })}${C.btn('Con reparos', { size: 'sm', icon: 'alert', action: 'sim-sii', attrs: 'data-id="' + inv.id + '" data-res="ACCEPTED_WITH_OBJECTIONS"' })}${C.btn('Rechazado', { size: 'sm', icon: 'xCircle', action: 'sim-sii', attrs: 'data-id="' + inv.id + '" data-res="REJECTED"' })}</div></div></div></div>`;
    if (st === 'CANCELLED') banner = C.alert('info', 'Documento anulado', nc ? 'Lo anuló la nota de crédito N° ' + nc.folio + ' del ' + U.date(nc.date) + '.' : 'Anulado por una nota de crédito.');
    if (st === 'DRAFT') banner = C.alert('info', 'Borrador', 'Este documento no tiene folio ni validez tributaria hasta que lo firmes y lo envíes al SII.');
    if (inv.tipo === 61 && inv.ref) banner = html`${banner}${C.alert('info', 'Nota de crédito', html`Corrige o anula ${raw('<a href="#/invoices/' + U.esc(inv.ref) + '">' + U.esc(docName(D.invById(inv.ref))) + '</a>')}.`)}`;

    var entry = D.entries.filter(function (e) { return e.ref === inv.id; })[0];
    if (!entry && inv.status !== 'DRAFT') entry = { n: '—', date: inv.date, desc: 'Venta según ' + docName(inv), lines: [['1.1.03', 'Clientes', inv.total, 0], ['4.1.01', 'Ventas', 0, inv.neto + inv.exento], ['2.1.02', 'IVA débito fiscal', 0, inv.iva]] };
    var tabBody;
    if (tab === 'doc') tabBody = C.docPreview(inv, D);
    else if (tab === 'entry') tabBody = entry ? html`<div class="stack">
        ${st === 'REJECTED' ? C.alert('destructive', 'Asiento de un documento rechazado', 'El sistema contabilizó la venta al crear el documento, antes de la respuesta del SII (RIESGOS R-11). Debe reversarse.', C.btn('Generar asiento de reversa', { size: 'sm', action: 'toast', attrs: 'data-msg="Asiento de reversa generado (propuesta)."' })) : (st !== 'ACCEPTED' && st !== 'ACCEPTED_WITH_OBJECTIONS' ? C.alert('warning', 'Contabilizado antes de la respuesta del SII', 'El asiento se generó al crear el documento (RIESGOS R-11). En el rediseño se contabiliza al ser aceptado.') : '')}
        <div class="card"><div class="card-head"><h2>Asiento N° ${entry.n}</h2><span class="muted">${U.date(entry.date)}, ${entry.desc}</span></div>
        <table class="dt"><thead><tr><th scope="col">Cuenta</th><th scope="col" class="num">Debe</th><th scope="col" class="num">Haber</th></tr></thead>
        <tbody>${entry.lines.map(function (l) { return html`<tr><td><span class="mono">${l[0]}</span> ${l[1]}</td><td class="num">${l[2] ? U.clp(l[2]) : ''}</td><td class="num">${l[3] ? U.clp(l[3]) : ''}</td></tr>`; })}</tbody>
        <tfoot><tr><td>Totales</td><td class="num">${U.clp(entry.lines.reduce(function (s, l) { return s + l[2]; }, 0))}</td><td class="num">${U.clp(entry.lines.reduce(function (s, l) { return s + l[3]; }, 0))}</td></tr></tfoot></table></div></div>` : C.empty({ icon: 'book', title: 'Sin asiento', text: 'Los borradores no se contabilizan.' });
    else tabBody = html`<div class="card card-pad"><ol class="timeline">${(inv.history.length ? inv.history : [[inv.date + 'T09:00', st, C.DTE[st].label]]).slice().reverse().map(function (h) {
        var d = C.DTE[h[1]] || C.DTE.DRAFT;
        return html`<li><span class="tl-ico badge ${d.tone}" style="padding:0;justify-content:center">${icon(d.icon, 14)}</span><div><strong>${d.label}</strong><div class="muted">${h[2]}</div><div class="small muted">${U.dateTime(h[0])}</div></div></li>`;
      })}</ol></div>`;

    return {
      title: docName(inv), titleExtra: C.status(st), crumbs: [['Facturas y documentos', '#/invoices']],
      subtitle: p.name + ', ' + p.rut, actions: html`${acts}`, endpoint: '/invoices/' + inv.id,
      body: html`<div class="stack">${C.dteTrack(inv)}${banner}
        <div class="grid g-main">
          <div>${C.tabs('inv-' + inv.id, [{ id: 'doc', label: 'Documento', icon: 'fileText' }, { id: 'entry', label: 'Asiento contable', icon: 'book' }, { id: 'hist', label: 'Historial', icon: 'clock' }], tab)}${tabBody}</div>
          <div class="card card-pad stack-sm" style="align-self:start">
            <h3>Datos</h3>
            <dl style="display:grid;grid-template-columns:auto 1fr;gap:6px 12px;margin:0">
              <dt class="muted">Tipo</dt><dd style="margin:0">${D.TIPOS[inv.tipo]}</dd>
              <dt class="muted">Folio</dt><dd style="margin:0">${inv.folio ? U.int(inv.folio) : 'Se asigna al firmar'}</dd>
              <dt class="muted">Emisión</dt><dd style="margin:0">${U.date(inv.date)}</dd>
              <dt class="muted">Vencimiento</dt><dd style="margin:0">${U.date(inv.due)}</dd>
              <dt class="muted">Pago</dt><dd style="margin:0">${inv.payment}</dd>
              <dt class="muted">TrackID</dt><dd style="margin:0">${inv.trackId || '—'}</dd>
              <dt class="muted">Neto</dt><dd style="margin:0" class="money">${U.clp(inv.neto)}</dd>
              <dt class="muted">IVA 19%</dt><dd style="margin:0" class="money">${U.clp(inv.iva)}</dd>
              <dt class="muted">Total</dt><dd style="margin:0" class="money strong">${U.clp(inv.total)}</dd>
            </dl>
            <p class="small muted">${C.DTE[st].desc}</p>
          </div>
        </div></div>`
    };
  };
  A['inv-send'] = function (el) { sendToSii(D.invById(el.getAttribute('data-id')), false); };
  A['inv-query'] = function () { App.toast('La consulta de estado por TrackID aún no existe en el backend (PLAN B-02). Usa la simulación de abajo.', 'info'); };
  A['sim-sii'] = function (el) {
    var inv = D.invById(el.getAttribute('data-id')), res = el.getAttribute('data-res');
    inv.status = res;
    if (res === 'ACCEPTED_WITH_OBJECTIONS') inv.objections = ['Giro del receptor no coincide con el registrado en el SII.'];
    if (res === 'REJECTED') inv.reason = 'Folio fuera del rango autorizado en el CAF (código RFR-02).';
    inv.history.push([new Date().toISOString(), res, res === 'ACCEPTED' ? 'El SII aceptó el documento' : res === 'REJECTED' ? 'Rechazado por el SII' : 'Aceptado con reparos']);
    if (res !== 'REJECTED' && inv.tipo === 61 && inv.ref) { var o = D.invById(inv.ref); if (o && o.status !== 'CANCELLED') { o.status = 'CANCELLED'; o.history.push([new Date().toISOString(), 'CANCELLED', 'Anulado por la nota de crédito N° ' + inv.folio]); } }
    App.render(); App.toast('Respuesta del SII: ' + C.DTE[res].label + '.', res === 'REJECTED' ? 'bad' : 'ok');
  };
  A['inv-retry'] = function (el) {
    var id = el.getAttribute('data-id');
    App.confirm({ title: 'Corregir y emitir de nuevo', text: 'Se creará un documento nuevo con los mismos datos para que lo corrijas. Al firmarlo usará un folio nuevo; el folio rechazado no se reutiliza.', confirm: 'Crear documento corregido', onConfirm: function () { App.go('/invoices/new?from=' + id); } });
  };
  A['inv-delete'] = function (el) {
    var id = el.getAttribute('data-id');
    App.confirm({ title: 'Eliminar el borrador', text: 'El borrador se eliminará de forma permanente. No afecta folios ni la contabilidad.', confirm: 'Eliminar borrador', destructive: true, icon: 'trash', onConfirm: function () { var i = D.invoices.indexOf(D.invById(id)); D.invoices.splice(i, 1); App.go('/invoices'); App.toast('Borrador eliminado.'); } });
  };
  A['inv-email'] = function (el) {
    var inv = D.invById(el.getAttribute('data-id')), p = D.party(inv.party);
    App.dialog({ title: 'Enviar ' + docName(inv).toLowerCase() + ' por email',
      body: html`${C.field('Destinatario', C.input('em-to', p.email, { readonly: true }), { id: 'em-to', hint: 'Se envía al email registrado del cliente. Para cambiarlo, edita el cliente (RIESGOS R-08).' })}<div style="margin-top:12px">${C.field('Mensaje (opcional)', raw('<textarea class="textarea" id="em-msg">Adjuntamos ' + U.esc(docName(inv)) + '. Saludos cordiales.</textarea>'), { id: 'em-msg' })}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Enviar email', { variant: 'primary', icon: 'mail', action: 'em-send', attrs: 'data-to="' + p.email + '"' })}` });
  };
  A['em-send'] = function (el) { App.closeDialog(); App.toast('Email enviado a ' + el.getAttribute('data-to') + '.'); };

  // ======================= Cotizaciones =======================
  var QTONE = { Enviada: 'info', Aceptada: 'success', Rechazada: 'destructive', Vencida: 'neutral', Borrador: 'neutral' };
  V.quotes = function () {
    return {
      subtitle: 'Propuestas enviadas a clientes y su respuesta.', endpoint: '/crm/quotes',
      actions: C.btn('Nueva cotización', { variant: 'primary', icon: 'plus', action: 'quote-new' }),
      empty: { icon: 'fileText', title: 'Aún no hay cotizaciones', text: 'Crea una cotización para enviarla a un cliente.', action: C.btn('Nueva cotización', { variant: 'primary', icon: 'plus', action: 'quote-new' }) },
      body: C.dataTable({ id: 'qt', search: 'Buscar por número o cliente', searchKeys: ['n', function (r) { return D.party(r.party).name; }],
        filters: [{ key: 'status', label: 'Estado', options: ['Enviada', 'Aceptada', 'Rechazada', 'Vencida'] }],
        columns: [
          { key: 'n', label: 'N°', render: function (r) { return html`<span class="cell-main">Cotización ${r.n}</span>`; } },
          { key: 'party', label: 'Cliente', render: function (r) { return D.party(r.party).name; } },
          { key: 'date', label: 'Fecha', render: function (r) { return U.date(r.date); } },
          { key: 'valid', label: 'Válida hasta', render: function (r) { return U.date(r.valid); } },
          { key: 'total', label: 'Total', num: true, render: function (r) { return U.clp(r.total); } },
          { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, QTONE[r.status]); } },
          { key: 'act', label: '', sort: false, render: function (r) {
            if (r.status === 'Enviada') return html`<div class="row" style="flex-wrap:nowrap">${C.btn('Aceptar', { size: 'sm', action: 'quote-set', attrs: 'data-id="' + r.id + '" data-v="Aceptada"' })}${C.btn('Rechazar', { size: 'sm', variant: 'ghost', action: 'quote-set', attrs: 'data-id="' + r.id + '" data-v="Rechazada"' })}</div>`;
            if (r.status === 'Aceptada') return C.btn('Convertir en factura', { size: 'sm', icon: 'receipt', href: '#/invoices/new?from=' + r.id, attrs: 'title="Propuesta nueva: depende de corregir la emisión (B-01)"' });
            return '';
          } }
        ], rows: D.quotes }, S.tables.qt)
    };
  };
  A['quote-set'] = function (el) { var q = D.quotes.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; q.status = el.getAttribute('data-v'); App.render(); App.toast('Cotización ' + q.n + ' marcada como ' + q.status.toLowerCase() + '.'); };
  A['quote-new'] = function () {
    S.wiz.q = { party: 'p1', total: 0 };
    App.dialog({ title: 'Nueva cotización', body: function () {
      return html`<div class="stack">${C.field('Cliente', C.select('q-p', D.parties.filter(function (p) { return p.kind === 'Cliente'; }).map(function (p) { return [p.id, p.name]; }), S.wiz.q.party, { attrs: 'data-bind="wiz.q.party"' }), { id: 'q-p' })}
        ${C.moneyField('q-t', 'Monto total', S.wiz.q.total, { attrs: 'data-bind="wiz.q.total" data-type="money"', hint: 'IVA incluido.' })}
        ${C.field('Válida por', C.select('q-v', [['15', '15 días'], ['30', '30 días']], '15'), { id: 'q-v' })}</div>`;
    }, actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Crear y enviar', { variant: 'primary', action: 'quote-save' })}` });
  };
  A['quote-save'] = function () {
    var q = S.wiz.q; if (!q.total) { App.toast('Ingresa el monto total.', 'bad'); return; }
    var n = D.quotes[0].n + 1; D.quotes.unshift({ id: 'q' + n, n: n, party: q.party, date: D.TODAY, valid: '2026-11-30', total: q.total, status: 'Enviada' });
    App.closeDialog(); App.render(); App.toast('Cotización ' + n + ' creada y enviada.');
  };

  // ======================= Oportunidades =======================
  var STAGES = ['Nuevo', 'Contactado', 'Propuesta', 'Ganado'];
  V.leads = function () {
    return {
      subtitle: 'Seguimiento de posibles clientes por etapa.', endpoint: '/crm/leads',
      actions: C.btn('Nueva oportunidad', { variant: 'primary', icon: 'plus', action: 'lead-new' }),
      empty: { icon: 'target', title: 'Aún no hay oportunidades', text: 'Registra un posible cliente para darle seguimiento.' },
      body: html`<div class="kanban">${STAGES.map(function (s) {
        var items = D.leads.filter(function (l) { return l.stage === s; });
        return html`<section class="kcol" aria-label="${s}"><h3><span>${s}</span><span class="muted">${items.length}, ${U.clp(items.reduce(function (a, l) { return a + l.value; }, 0))}</span></h3>
          ${items.map(function (l) { return html`<article class="kcard"><strong>${l.name}</strong><div class="small muted">${l.contact}, ${l.src}</div><div class="row-between" style="margin-top:8px"><span class="money strong">${U.clp(l.value)}</span>
            <label class="sr-only" for="ls-${l.id}">Mover ${l.name} a</label><select class="select" style="height:28px;width:auto;font-size:12px" id="ls-${l.id}" data-action-change="lead-move" data-id="${l.id}">${STAGES.map(function (x) { return html`<option ${x === s ? raw('selected') : ''}>${x}</option>`; })}</select></div></article>`; })}
          ${items.length ? '' : html`<p class="small muted">Sin oportunidades en esta etapa.</p>`}
        </section>`;
      })}</div>`
    };
  };
  CH['lead-move'] = function (el) { var l = D.leads.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; l.stage = el.value; App.render(); App.toast(l.name + ' movido a ' + l.stage + '.'); };
  A['lead-new'] = function () {
    S.wiz.lead = { name: '', contact: '', value: 0 };
    App.dialog({ title: 'Nueva oportunidad', body: html`<div class="stack">${C.field('Empresa', C.input('ld-n', '', { attrs: 'data-bind="wiz.lead.name"' }), { id: 'ld-n', req: true })}${C.field('Contacto', C.input('ld-c', '', { attrs: 'data-bind="wiz.lead.contact"' }), { id: 'ld-c' })}${C.moneyField('ld-v', 'Valor estimado', 0, { attrs: 'data-bind="wiz.lead.value" data-type="money"' })}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Crear oportunidad', { variant: 'primary', action: 'lead-save' })}` });
  };
  A['lead-save'] = function () { var l = S.wiz.lead; if (!l.name) { App.toast('Ingresa el nombre de la empresa.', 'bad'); return; } D.leads.unshift({ id: 'l' + Date.now(), name: l.name, contact: l.contact || '—', value: l.value, stage: 'Nuevo', src: 'Manual' }); App.closeDialog(); App.render(); App.toast('Oportunidad creada.'); };

  // ======================= Órdenes de compra =======================
  var POTONE = { Borrador: 'neutral', Enviada: 'info', Recibida: 'success' };
  V.procurement = function () {
    return {
      subtitle: 'Compras a proveedores, desde el pedido hasta la recepción.', endpoint: '/procurement/orders',
      actions: C.btn('Nueva orden de compra', { variant: 'primary', icon: 'plus', action: 'po-new' }),
      empty: { icon: 'cart', title: 'Aún no hay órdenes de compra', text: 'Crea una orden para pedir productos a un proveedor.' },
      body: C.dataTable({ id: 'po', search: 'Buscar por número o proveedor', searchKeys: ['n', function (r) { return D.party(r.party).name; }],
        filters: [{ key: 'status', label: 'Estado', options: ['Borrador', 'Enviada', 'Recibida'] }],
        columns: [
          { key: 'n', label: 'Orden', render: function (r) { return html`<span class="cell-main">OC ${r.n}</span>`; } },
          { key: 'party', label: 'Proveedor', render: function (r) { var p = D.party(r.party); return html`<div>${p.name}</div><div class="cell-sub">${p.rut}</div>`; } },
          { key: 'date', label: 'Fecha', render: function (r) { return U.date(r.date); } },
          { key: 'eta', label: 'Llegada estimada', render: function (r) { return r.eta === '—' ? '—' : U.date(r.eta); } },
          { key: 'total', label: 'Total', num: true, render: function (r) { return U.clp(r.total); } },
          { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, POTONE[r.status]); } },
          { key: 'act', label: '', sort: false, render: function (r) { return r.status === 'Borrador' ? C.btn('Marcar enviada', { size: 'sm', action: 'po-set', attrs: 'data-id="' + r.id + '" data-v="Enviada"' }) : r.status === 'Enviada' ? C.btn('Marcar recibida', { size: 'sm', action: 'po-set', attrs: 'data-id="' + r.id + '" data-v="Recibida"' }) : ''; } }
        ], rows: D.purchaseOrders }, S.tables.po)
    };
  };
  A['po-set'] = function (el) { var o = D.purchaseOrders.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; o.status = el.getAttribute('data-v'); App.render(); App.toast('OC ' + o.n + ': ' + o.status.toLowerCase() + '.'); };
  A['po-new'] = function () {
    S.wiz.po = { party: 's1', total: 0 };
    App.dialog({ title: 'Nueva orden de compra', body: html`<div class="stack">${C.field('Proveedor', C.select('po-p', D.parties.filter(function (p) { return p.kind === 'Proveedor'; }).map(function (p) { return [p.id, p.name]; }), 's1', { attrs: 'data-bind="wiz.po.party"' }), { id: 'po-p' })}${C.moneyField('po-t', 'Monto total', 0, { attrs: 'data-bind="wiz.po.total" data-type="money"' })}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Guardar borrador', { variant: 'primary', action: 'po-save' })}` });
  };
  A['po-save'] = function () { var o = S.wiz.po; if (!o.total) { App.toast('Ingresa el monto.', 'bad'); return; } var n = D.purchaseOrders[0].n + 1; D.purchaseOrders.unshift({ id: 'oc' + n, n: n, party: o.party, date: D.TODAY, total: o.total, status: 'Borrador', eta: '—' }); App.closeDialog(); App.render(); App.toast('OC ' + n + ' guardada como borrador.'); };

  // ======================= Boletas de honorarios =======================
  V.fees = function () {
    var month = D.fees.filter(function (f) { return f.date >= '2026-11-01'; });
    return {
      subtitle: 'Boletas recibidas de personas naturales y la retención que debes declarar.', endpoint: '/fees',
      actions: html`${C.btn('Importar desde el SII', { icon: 'upload', action: 'fee-import' })}`,
      empty: { icon: 'receipt', title: 'Aún no hay boletas de honorarios', text: 'Importa las boletas recibidas desde el archivo que descargas del SII.', action: C.btn('Importar desde el SII', { variant: 'primary', icon: 'upload', action: 'fee-import' }) },
      body: html`<div class="stack">
        <div class="grid g3">${C.kpi('Boletas en noviembre', String(month.length))}${C.kpi('Monto bruto', U.clp(month.reduce(function (s, f) { return s + f.gross; }, 0)))}${C.kpi('Retención a declarar (F29 código 151)', U.clp(month.reduce(function (s, f) { return s + f.ret; }, 0)), 'Tasa 2026: 15,25%')}</div>
        ${C.alert('warning', 'El sistema actual calcula con la tasa de 2024', 'Los parámetros tributarios están fijos en 2024 (RIESGOS R-07). Esta pantalla usa la tasa vigente de 2026.')}
        ${C.dataTable({ id: 'fee', search: 'Buscar por persona o RUT', searchKeys: ['person', 'rut'],
          columns: [
            { key: 'n', label: 'Boleta', render: function (r) { return html`<span class="cell-main">N° ${r.n}</span>`; } },
            { key: 'person', label: 'Emisor', render: function (r) { return html`<div>${r.person}</div><div class="cell-sub">${r.rut}</div>`; } },
            { key: 'desc', label: 'Glosa' },
            { key: 'date', label: 'Fecha', render: function (r) { return U.date(r.date); } },
            { key: 'gross', label: 'Bruto', num: true, render: function (r) { return U.clp(r.gross); } },
            { key: 'ret', label: 'Retención', num: true, render: function (r) { return U.clp(r.ret); } },
            { key: 'net', label: 'Líquido', num: true, render: function (r) { return U.clp(r.net); } }
          ], rows: D.fees, totals: function (rows) { return { gross: U.clp(rows.reduce(function (s, r) { return s + r.gross; }, 0)), ret: U.clp(rows.reduce(function (s, r) { return s + r.ret; }, 0)), net: U.clp(rows.reduce(function (s, r) { return s + r.net; }, 0)) }; } }, S.tables.fee)}
      </div>`
    };
  };
  A['fee-import'] = function () {
    App.dialog({ title: 'Importar boletas de honorarios', body: html`<p>Descarga desde el SII el informe de boletas recibidas del mes y súbelo aquí.</p>
      <label class="card card-pad" style="display:flex;flex-direction:column;align-items:center;gap:8px;border-style:dashed;margin-top:12px;cursor:pointer">${icon('upload', 24)}<strong>Elegir archivo</strong><span class="small muted">CSV o Excel del SII</span><input type="file" class="sr-only" accept=".csv,.xlsx"></label>
      <div style="margin-top:12px">${C.alert('info', null, 'Hoy esta importación es simulada aunque existe POST /fees/import (MAPA-FUNCIONAL §5).')}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Usar archivo de ejemplo', { variant: 'primary', action: 'fee-imp-ok' })}` });
  };
  A['fee-imp-ok'] = function () { App.closeDialog(); App.toast('Se importaron 3 boletas de octubre.'); };
})();
