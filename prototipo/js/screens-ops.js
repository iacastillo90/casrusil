/* Operaciones, documentos, reportes, módulos por rubro, configuración y notificaciones. */
(function () {
  'use strict';
  var U = window.U, C = window.C, D = window.D, REG = window.REG, html = U.html, icon = U.icon, raw = U.raw, V = window.V, A = window.A, CH = window.CH, S = window.S;

  function stockBadge(p) { return p.stock === 0 ? C.badge('Sin stock', 'destructive', 'xCircle') : p.stock < p.min ? C.badge('Bajo el mínimo', 'warning', 'alert') : C.badge('Disponible', 'success', 'check'); }

  // ======================= Inventario =======================
  V.inventory = function () {
    var low = D.products.filter(function (p) { return p.stock < p.min; });
    return {
      subtitle: 'Productos, existencias y movimientos de bodega. Costo por precio medio ponderado.', endpoint: '/inventory/products',
      actions: html`${C.btn('Exportar a Excel', { icon: 'download', action: 'toast', attrs: 'data-msg="Hoy esta exportación falla: GET /inventory/export/excel no existe." data-tone="bad"' })}${C.btn('Nuevo producto', { variant: 'primary', icon: 'plus', action: 'prod-new' })}`,
      empty: { icon: 'package', title: 'Aún no hay productos', text: 'Crea tu catálogo para controlar existencias y usarlo al emitir documentos.', action: C.btn('Nuevo producto', { variant: 'primary', icon: 'plus', action: 'prod-new' }) },
      body: html`<div class="stack">
        <div class="grid g4">${C.kpi('Productos', String(D.products.length))}${C.kpi('Valor del inventario', U.clp(D.products.reduce(function (s, p) { return s + p.stock * p.cost; }, 0)), 'A costo PMP')}${C.kpi('Bajo el mínimo', String(low.length), 'Requieren reposición', { tone: low.length ? 'down' : 'up', ic: 'alert' })}${C.kpi('Sin stock', String(D.products.filter(function (p) { return !p.stock; }).length))}</div>
        ${C.dataTable({ id: 'prd', search: 'Buscar por nombre o código', searchKeys: ['name', 'sku'], filters: [{ key: 'lvl', label: 'Existencias', options: [['ok', 'Disponible'], ['low', 'Bajo el mínimo'], ['out', 'Sin stock']], get: function (p) { return p.stock === 0 ? 'out' : p.stock < p.min ? 'low' : 'ok'; } }],
          columns: [
            { key: 'sku', label: 'Código', render: function (r) { return html`<span class="mono">${r.sku}</span>`; } },
            { key: 'name', label: 'Producto', render: function (r) { return html`<span class="cell-main">${r.name}</span>`; } },
            { key: 'loc', label: 'Ubicación' },
            { key: 'stock', label: 'Existencias', num: true, render: function (r) { return U.int(r.stock) + ' ' + r.unit; } },
            { key: 'min', label: 'Mínimo', num: true, render: function (r) { return U.int(r.min); } },
            { key: 'cost', label: 'Costo PMP', num: true, render: function (r) { return U.clp(r.cost); } },
            { key: 'price', label: 'Precio venta', num: true, render: function (r) { return U.clp(r.price); } },
            { key: 'st', label: 'Estado', sort: false, render: stockBadge },
            { key: 'a', label: '', sort: false, render: function (r) { return C.btn('Movimiento', { size: 'sm', variant: 'ghost', action: 'prod-move', attrs: 'data-id="' + r.sku + '"' }); } }
          ], rows: D.products }, S.tables.prd)}
      </div>`
    };
  };
  A['prod-new'] = function () {
    App.dialog({ title: 'Nuevo producto', body: html`<div class="stack"><div class="grid g2">${C.field('Código', C.input('pr-s', '', { placeholder: 'ACE-001' }), { id: 'pr-s', req: true })}${C.field('Unidad', C.select('pr-u', ['unidad', 'caja', 'saco', 'kg', 'litro']), { id: 'pr-u' })}</div>${C.field('Nombre', C.input('pr-n', ''), { id: 'pr-n', req: true })}<div class="grid g2">${C.moneyField('pr-p', 'Precio de venta neto', 0)}${C.field('Stock mínimo', C.input('pr-m', '', { attrs: 'inputmode="numeric"' }), { id: 'pr-m' })}</div>
      ${C.alert('info', null, 'Hoy no se pueden crear productos desde la pantalla, aunque el backend lo permite (MAPA-FUNCIONAL §6).')}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Crear producto', { variant: 'primary', action: 'prod-save' })}` });
  };
  A['prod-save'] = function () { App.closeDialog(); App.toast('Producto creado.'); };
  A['prod-move'] = function (el) {
    var p = D.products.filter(function (x) { return x.sku === el.getAttribute('data-id'); })[0];
    S.wiz.mv = { sku: p.sku, kind: 'Entrada', qty: 1 };
    App.dialog({ title: 'Registrar movimiento: ' + p.name, body: html`<div class="stack"><p class="muted">Existencias actuales: ${U.int(p.stock)} ${p.unit}.</p>${C.field('Tipo', C.select('mv-k', ['Entrada', 'Salida', 'Ajuste'], 'Entrada', { attrs: 'data-bind="wiz.mv.kind"' }), { id: 'mv-k' })}${C.field('Cantidad', C.input('mv-q', 1, { attrs: 'data-bind="wiz.mv.qty" data-type="int" inputmode="numeric"' }), { id: 'mv-q', req: true })}</div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Registrar', { variant: 'primary', action: 'mv-save' })}` });
  };
  A['mv-save'] = function () { var m = S.wiz.mv, p = D.products.filter(function (x) { return x.sku === m.sku; })[0]; p.stock = Math.max(0, p.stock + (m.kind === 'Salida' ? -m.qty : m.qty)); App.closeDialog(); App.render(); App.toast('Movimiento registrado. Nuevo stock: ' + U.int(p.stock) + '.'); };

  // ======================= Centros de costo =======================
  V.projects = function () {
    return {
      subtitle: 'Presupuesto y gasto por centro de costo o proyecto.', endpoint: '/projects',
      actions: C.btn('Nuevo centro de costo', { variant: 'primary', icon: 'plus', action: 'proj-new' }),
      empty: { icon: 'target', title: 'Aún no hay centros de costo', text: 'Crea centros de costo para separar gastos por sucursal, proyecto o área.' },
      body: html`<div class="stack">${C.alert('info', null, 'En el rediseño esta pantalla pasa de Contabilidad a Operaciones: el backend solo la permite a Administración y Operaciones (decisión pendiente D-06).')}
        ${C.dataTable({ id: 'prj', search: 'Buscar centro de costo', searchKeys: ['name', 'code'], filters: [{ key: 'status', label: 'Estado', options: ['Activo', 'En pausa'] }], columns: [
          { key: 'code', label: 'Código', render: function (r) { return html`<span class="mono">${r.code}</span>`; } }, { key: 'name', label: 'Nombre', render: function (r) { return html`<span class="cell-main">${r.name}</span>`; } }, { key: 'owner', label: 'Responsable' },
          { key: 'budget', label: 'Presupuesto', num: true, render: function (r) { return U.clp(r.budget); } }, { key: 'spent', label: 'Gastado', num: true, render: function (r) { return U.clp(r.spent); } },
          { key: 'pct', label: 'Uso', sortGet: function (r) { return r.spent / r.budget; }, render: function (r) { var pc = Math.round(r.spent / r.budget * 100); return html`<div class="row" style="flex-wrap:nowrap;gap:8px;min-width:140px"><div class="meter ${pc > 90 ? 'low' : ''}" style="flex:1" role="img" aria-label="${pc}% usado"><span class="avail" style="width:${pc}%"></span></div><span class="small">${pc}%</span></div>`; } },
          { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, r.status === 'Activo' ? 'success' : 'neutral'); } }
        ], rows: D.projects }, S.tables.prj)}</div>`
    };
  };
  A['proj-new'] = function () { App.dialog({ title: 'Nuevo centro de costo', body: html`<div class="stack">${C.field('Nombre', C.input('pj-n', ''), { id: 'pj-n', req: true })}${C.moneyField('pj-b', 'Presupuesto anual', 0)}</div>`, actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Crear', { variant: 'primary', action: 'toast', attrs: 'data-msg="Centro de costo creado."' })}` }); };

  // ======================= Gestor documental =======================
  V.documents = function () {
    return {
      subtitle: 'Documentos de respaldo, con extracción de datos asistida por IA.', endpoint: '/dms',
      actions: C.btn('Subir documento', { variant: 'primary', icon: 'upload', action: 'doc-up' }),
      empty: { icon: 'folder', title: 'Aún no hay documentos', text: 'Sube facturas de proveedores, contratos o cartolas para tenerlos ordenados y extraer sus datos.', action: C.btn('Subir documento', { variant: 'primary', icon: 'upload', action: 'doc-up' }) },
      body: C.dataTable({ id: 'dms', search: 'Buscar documento', searchKeys: ['name', 'type'], filters: [{ key: 'type', label: 'Tipo', options: ['Factura de compra', 'Contrato', 'Cartola bancaria', 'Boleta de honorarios'] }], columns: [
        { key: 'name', label: 'Documento', render: function (r) { return html`<span class="cell-main">${r.name}</span>`; } }, { key: 'type', label: 'Tipo' }, { key: 'date', label: 'Subido', render: function (r) { return U.date(r.date); } },
        { key: 'status', label: 'Análisis', render: function (r) { return r.status === 'Analizado' ? html`${C.badge('Analizado', 'success', 'check')} <span class="small muted">confianza ${Math.round(r.conf * 100)}%</span>` : C.badge('Pendiente', 'neutral', 'clock'); } },
        { key: 'a', label: '', sort: false, render: function (r) { return r.type === 'Factura de compra' ? C.btn('Extraer y registrar', { size: 'sm', action: 'doc-extract', attrs: 'data-id="' + r.id + '"' }) : C.btn(r.status === 'Pendiente' ? 'Analizar' : 'Ver', { size: 'sm', variant: 'ghost', action: 'toast', attrs: 'data-msg="Documento analizado."' }); } }
      ], rows: D.documents }, S.tables.dms)
    };
  };
  A['doc-up'] = function () { App.dialog({ title: 'Subir documento', body: html`<label class="card card-pad" style="display:flex;flex-direction:column;align-items:center;gap:8px;border-style:dashed;cursor:pointer">${icon('upload', 24)}<strong>Elegir archivos</strong><span class="small muted">PDF, imagen o Excel, hasta 20 MB</span><input type="file" class="sr-only" multiple></label><div style="margin-top:12px">${C.alert('info', null, 'Hoy la pantalla no tiene subida; el endpoint POST /dms está por confirmar (MAPA-FUNCIONAL §2.7).')}</div>`, actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Subir', { variant: 'primary', action: 'toast', attrs: 'data-msg="Documento subido."' })}` }); };
  A['doc-extract'] = function () {
    App.dialog({ title: 'Datos extraídos por la IA', wide: true, body: html`<div class="stack">${C.alert('brand', 'Revisa antes de registrar', 'La IA propone estos datos a partir del PDF. Corrige lo que no coincida; nada se registra sin tu confirmación. Usa /dms/ai-invoicing/process, que existe sin pantalla.')}
      <div class="grid g2">${C.field('Proveedor', C.input('ex-p', 'Distribuidora Andes Mayorista SpA'), { id: 'ex-p', ok: 'Coincide con un proveedor registrado' })}${C.field('RUT', C.input('ex-r', U.rut(76120553)), { id: 'ex-r' })}${C.field('Folio', C.input('ex-f', '55120'), { id: 'ex-f', err: 'Este folio ya está registrado: posible duplicado.' })}${C.field('Fecha', C.input('ex-d', '13-11-2026'), { id: 'ex-d' })}${C.moneyField('ex-n', 'Neto', 4000000)}${C.moneyField('ex-i', 'IVA', 760000)}</div></div>`,
      actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Registrar compra', { variant: 'primary', action: 'toast', attrs: 'data-msg="No se registró: el folio ya existe." data-tone="bad"' })}` });
  };

  // ======================= Reportes =======================
  V.reports = function () {
    var cat = [
      { t: 'Ventas por período', d: 'Detalle de documentos emitidos, en Excel.', st: 'disponible' },
      { t: 'Estado de resultados', d: 'Ingresos, costos y resultado.', st: 'disponible', href: '#/accounting/income' },
      { t: 'Balance general', d: 'Situación financiera a una fecha. Endpoint /reports/balance-sheet sin uso.', st: 'planificado' },
      { t: 'Reporte semanal', d: 'Resumen automático de la semana. Endpoint /reports/weekly/generate sin uso.', st: 'planificado' },
      { t: 'Libro de compras y ventas', d: 'A partir del RCV descargado del SII.', st: 'parcial' },
      { t: 'Retenciones de honorarios', d: 'Para el F29 y el certificado anual.', st: 'parcial' }
    ];
    var LBL = { disponible: ['Disponible', 'success'], parcial: ['Hoy devuelve vacío', 'warning'], planificado: ['Planificado', 'info'] };
    return {
      subtitle: 'Informes listos para descargar o compartir.', endpoint: '/reports',
      body: html`<div class="stack"><div class="row">${C.periodPicker('rep-per', 2026, 10, { change: 'noop' })}${C.field('Formato', C.select('rep-f', ['Excel', 'PDF']), { id: 'rep-f' })}</div>
        <div class="grid g3">${cat.map(function (c) { return html`<div class="card card-pad stack-sm"><div class="row-between"><h3>${c.t}</h3>${C.badge(LBL[c.st][0], LBL[c.st][1])}</div><p class="muted">${c.d}</p><div>${c.href ? C.btn('Abrir', { size: 'sm', href: c.href }) : C.btn('Generar', { size: 'sm', icon: 'download', action: 'toast', attrs: 'data-msg="Reporte "' + c.t + '" generado."' })}</div></div>`; })}</div></div>`
    };
  };

  // ======================= Base de conocimiento IA =======================
  V.knowledge = function () {
    var kb = [{ n: 'Política de crédito a clientes.pdf', d: '2026-09-02', c: 14 }, { n: 'Manual de procedimientos contables.docx', d: '2026-06-18', c: 48 }, { n: 'Circular SII sobre boleta electrónica.pdf', d: '2026-03-11', c: 9 }];
    return {
      subtitle: 'Documentos que el asistente usa como referencia al responder.', endpoint: '/ai/knowledge',
      actions: C.btn('Agregar documento', { variant: 'primary', icon: 'upload', action: 'toast', attrs: 'data-msg="Documento agregado a la base de conocimiento."' }),
      empty: { icon: 'book', title: 'La base de conocimiento está vacía', text: 'Agrega manuales o políticas internas para que el asistente responda con ellos.' },
      body: html`<div class="stack">${C.alert('warning', 'Riesgo de mezcla entre empresas', 'Hoy el índice del asistente se guarda en memoria y es compartido por todas las empresas (RIESGOS R-12). Debe separarse antes de habilitar esta pantalla.')}
        <div class="card card-pad"><div class="dt-search" style="max-width:480px">${icon('search', 15)}<label class="sr-only" for="kb-q">Buscar en la base</label><input class="input" id="kb-q" type="search" placeholder="Prueba: plazo de pago a clientes nuevos"></div></div>
        ${C.dataTable({ id: 'kb', columns: [{ key: 'n', label: 'Documento', render: function (r) { return html`<span class="cell-main">${r.n}</span>`; } }, { key: 'd', label: 'Agregado', render: function (r) { return U.date(r.d); } }, { key: 'c', label: 'Fragmentos indexados', num: true }], rows: kb }, S.tables.kb)}</div>`
    };
  };

  // ======================= Punto de venta =======================
  V.pos = function () {
    var cart = S.wiz.pos = S.wiz.pos || [];
    var total = cart.reduce(function (s, l) { return s + l.qty * l.price; }, 0);
    return {
      subtitle: 'Venta en mesón con boleta electrónica.', endpoint: '/pos/transactions', loadingKind: 'table',
      actions: C.btn('Sincronizar ventas', { icon: 'refresh', action: 'toast', attrs: 'data-msg="Ventas sincronizadas."' }),
      body: html`<div class="pos">
        <div class="stack">
          <div class="card card-pad row" style="flex-wrap:nowrap"><div class="dt-search" style="flex:1">${icon('search', 15)}<label class="sr-only" for="pos-q">Buscar o escanear producto</label><input class="input" id="pos-q" placeholder="Escanea o escribe el código" style="width:100%"></div>${C.btn('Buscar', { action: 'toast', attrs: 'data-msg="Hoy la búsqueda por código falla: GET /pos/products/search no existe." data-tone="bad"' })}</div>
          <div class="pos-grid">${D.products.filter(function (p) { return p.stock > 0; }).map(function (p) { return html`<button type="button" class="pos-tile" data-action="pos-add" data-id="${p.sku}"><strong>${p.name}</strong><span class="row-between"><span class="money">${U.clp(Math.round(p.price * 1.19))}</span><span class="small muted">${U.int(p.stock)} disp.</span></span></button>`; })}</div>
        </div>
        <div class="card" style="align-self:start"><div class="card-head"><h2>Venta actual</h2><span class="spacer"></span>${cart.length ? C.btn('Vaciar', { size: 'sm', variant: 'ghost', action: 'pos-clear' }) : ''}</div>
          <div class="card-body stack-sm">${cart.length ? cart.map(function (l, i) { return html`<div class="row-between" style="flex-wrap:nowrap"><div><strong>${l.name}</strong><div class="small muted">${l.qty} × ${U.clp(l.price)}</div></div><div class="row" style="flex-wrap:nowrap">${C.iconBtn('minus', 'Quitar uno de ' + l.name, 'pos-dec', 'data-i="' + i + '"')}<span class="money">${U.clp(l.qty * l.price)}</span></div></div>`; }) : html`<p class="muted">Agrega productos tocando el catálogo.</p>`}
            <div class="totals" style="margin-top:8px"><div class="grand"><span>Total (IVA incluido)</span><span>${U.clp(total)}</span></div></div>
            <div class="grid g2">${C.btn('Efectivo', { variant: 'outline', action: 'pos-pay', disabled: !cart.length, attrs: 'data-m="efectivo"' })}${C.btn('Tarjeta', { variant: 'primary', action: 'pos-pay', disabled: !cart.length, attrs: 'data-m="tarjeta"' })}</div>
            <p class="small muted">Las boletas se envían hoy por el mismo canal que las facturas; el SII podría exigir un canal propio (RIESGOS R-06).</p>
          </div></div>
      </div>`
    };
  };
  A['pos-add'] = function (el) { var p = D.products.filter(function (x) { return x.sku === el.getAttribute('data-id'); })[0], c = S.wiz.pos, l = c.filter(function (x) { return x.sku === p.sku; })[0]; if (l) l.qty++; else c.push({ sku: p.sku, name: p.name, qty: 1, price: Math.round(p.price * 1.19) }); App.render(); };
  A['pos-dec'] = function (el) { var c = S.wiz.pos, i = Number(el.getAttribute('data-i')); c[i].qty--; if (!c[i].qty) c.splice(i, 1); App.render(); };
  A['pos-clear'] = function () { S.wiz.pos = []; App.render(); };
  A['pos-pay'] = function (el) { var c = window.VENTAS.cafFor(39); var n = c ? c.next : 0; if (c) c.caf.used++; S.wiz.pos = []; App.render(); App.toast('Venta pagada con ' + el.getAttribute('data-m') + '. Boleta N° ' + U.int(n) + ' emitida.'); };

  // ======================= Pedidos B2B (vista interna, propuesta) =======================
  var OTONE = { 'Pendiente de pago': 'warning', Pagado: 'info', Despachado: 'success' };
  V['b2b-orders'] = function () {
    return {
      subtitle: 'Pedidos que tus clientes hacen en el portal B2B.', endpoint: '/b2b/orders',
      actions: C.btn('Ver el portal como cliente', { icon: 'external', href: '#/portal' }),
      body: html`<div class="stack">${C.alert('info', 'Propuesta nueva', 'Hoy el personal interno ve el portal del cliente en su propio menú. El rediseño separa la vista interna de pedidos (PLAN §3.3).')}
        ${C.dataTable({ id: 'b2bi', search: 'Buscar pedido o cliente', searchKeys: ['n', function (r) { return D.party(r.party).name; }], filters: [{ key: 'status', label: 'Estado', options: ['Pendiente de pago', 'Pagado', 'Despachado'] }], columns: [
          { key: 'n', label: 'Pedido', render: function (r) { return html`<span class="cell-main">N° ${r.n}</span>`; } }, { key: 'party', label: 'Cliente', render: function (r) { return D.party(r.party).name; } }, { key: 'date', label: 'Fecha', render: function (r) { return U.date(r.date); } },
          { key: 'items', label: 'Productos', num: true }, { key: 'total', label: 'Total', num: true, render: function (r) { return U.clp(r.total); } }, { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, OTONE[r.status]); } },
          { key: 'a', label: '', sort: false, render: function (r) { return r.status === 'Pagado' ? C.btn('Despachar', { size: 'sm', action: 'b2b-ship', attrs: 'data-id="' + r.id + '"' }) : ''; } }
        ], rows: D.b2bOrders }, S.tables.b2bi)}</div>`
    };
  };
  A['b2b-ship'] = function (el) { var o = D.b2bOrders.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; o.status = 'Despachado'; App.render(); App.toast('Pedido N° ' + o.n + ' despachado.'); };

  // ======================= Portal B2B (cliente) =======================
  V.portal = function () {
    var tab = S.tabs['portal-portal'] || 'orders';
    var mine = D.b2bOrders.filter(function (o) { return o.party === 'p2'; });
    if (tab === 'orders') return { title: 'Mis pedidos', endpoint: '/b2b/orders/my-orders', body: html`<div class="stack">
      <div class="grid g3">${C.kpi('Pedidos este mes', String(mine.length))}${C.kpi('Por pagar', U.clp(mine.filter(function (o) { return o.status === 'Pendiente de pago'; }).reduce(function (s, o) { return s + o.total; }, 0)))}${C.kpi('Crédito disponible', U.clp(3500000))}</div>
      ${C.dataTable({ id: 'myo', columns: [{ key: 'n', label: 'Pedido', render: function (r) { return html`<span class="cell-main">N° ${r.n}</span>`; } }, { key: 'date', label: 'Fecha', render: function (r) { return U.date(r.date); } }, { key: 'total', label: 'Total', num: true, render: function (r) { return U.clp(r.total); } }, { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, OTONE[r.status]); } },
        { key: 'a', label: '', sort: false, render: function (r) { return r.status === 'Pendiente de pago' ? C.btn('Pagar', { size: 'sm', variant: 'primary', action: 'b2b-pay', attrs: 'data-id="' + r.id + '"' }) : ''; } }], rows: mine, empty: { icon: 'bag', title: 'Aún no tienes pedidos', text: 'Haz tu primer pedido desde el catálogo.' } }, S.tables.myo)}</div>` };
    var cartQ = S.wiz.b2b = S.wiz.b2b || {};
    var total = D.products.reduce(function (s, p) { return s + (cartQ[p.sku] || 0) * p.price; }, 0);
    return { title: 'Nuevo pedido', endpoint: '/b2b/orders', body: html`<div class="grid g-main"><div class="card"><table class="dt"><thead><tr><th scope="col">Producto</th><th scope="col" class="num">Precio neto</th><th scope="col" class="num" style="width:120px">Cantidad</th></tr></thead><tbody>
      ${D.products.map(function (p) { return html`<tr><td class="cell-main">${p.name}</td><td class="num">${U.clp(p.price)}</td><td class="num"><label class="sr-only" for="b2-${p.sku}">Cantidad de ${p.name}</label><input class="input money" id="b2-${p.sku}" inputmode="numeric" value="${cartQ[p.sku] || ''}" placeholder="0" data-bind="wiz.b2b.${p.sku}" data-type="int" data-live="1" data-rerender></td></tr>`; })}
      </tbody></table></div><div class="card card-pad stack" style="align-self:start"><h3>Resumen</h3><div class="totals"><div><span>Neto</span><span>${U.clp(total)}</span></div><div><span>IVA 19%</span><span>${U.clp(Math.round(total * 0.19))}</span></div><div class="grand"><span>Total</span><span>${U.clp(Math.round(total * 1.19))}</span></div></div>${C.btn('Enviar pedido', { variant: 'primary', action: 'b2b-order', disabled: !total })}</div></div>` };
  };
  A['b2b-pay'] = function (el) { var o = D.b2bOrders.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; App.confirm({ title: 'Pagar el pedido N° ' + o.n, text: 'Se cargará ' + U.clp(o.total) + ' a tu medio de pago registrado.', confirm: 'Pagar ' + U.clp(o.total), onConfirm: function () { o.status = 'Pagado'; App.render(); App.toast('Pedido pagado.'); } }); };
  A['b2b-order'] = function () { var q = S.wiz.b2b, items = Object.keys(q).filter(function (k) { return q[k] > 0; }), total = D.products.reduce(function (s, p) { return s + (q[p.sku] || 0) * p.price; }, 0); var n = D.b2bOrders[0].n + 1; D.b2bOrders.unshift({ id: 'o' + n, n: n, party: 'p2', date: D.TODAY, items: items.length, total: Math.round(total * 1.19), status: 'Pendiente de pago' }); S.wiz.b2b = {}; S.tabs['portal-portal'] = 'orders'; App.render(); App.toast('Pedido N° ' + n + ' enviado.'); };

  // ======================= Bodega (WMS) =======================
  V.wms = function () {
    var tab = S.tabs.wms || 'loc';
    var body = tab === 'loc' ? C.dataTable({ id: 'loc', columns: [{ key: 'code', label: 'Ubicación', render: function (r) { return html`<span class="mono">${r.code}</span>`; } }, { key: 'zone', label: 'Zona' }, { key: 'used', label: 'Ocupación', render: function (r) { var p = Math.round(r.used / r.cap * 100); return html`<div class="row" style="flex-wrap:nowrap;gap:8px;min-width:160px"><div class="meter ${p > 85 ? 'low' : ''}" style="flex:1"><span class="avail" style="width:${p}%"></span></div><span class="small">${r.used}/${r.cap}</span></div>`; } }], rows: D.locations }, S.tables.loc)
      : C.dataTable({ id: 'pk', columns: [{ key: 'n', label: 'Picking', render: function (r) { return html`<span class="cell-main">${r.n}</span>`; } }, { key: 'order', label: 'Origen' }, { key: 'items', label: 'Líneas', num: true }, { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, r.status === 'Completado' ? 'success' : r.status === 'En curso' ? 'info' : 'neutral'); } }, { key: 'a', label: '', sort: false, render: function (r) { return r.status !== 'Completado' ? C.btn('Completar', { size: 'sm', action: 'pk-done', attrs: 'data-id="' + r.id + '"' }) : ''; } }], rows: D.pickings }, S.tables.pk);
    return { subtitle: 'Ubicaciones de bodega y preparación de pedidos.', endpoint: '/wms/locations',
      actions: C.btn(tab === 'loc' ? 'Nueva ubicación' : 'Nuevo picking', { variant: 'primary', icon: 'plus', action: 'toast', attrs: 'data-msg="Creado. (Hoy el alta no está en la pantalla; el backend la permite.)"' }),
      body: html`${C.tabs('wms', [{ id: 'loc', label: 'Ubicaciones' }, { id: 'pick', label: 'Picking' }], tab)}${body}` };
  };
  A['pk-done'] = function (el) { D.pickings.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0].status = 'Completado'; App.render(); App.toast('Picking completado.'); };

  // ======================= Producción (MRP) =======================
  V.mrp = function () {
    var tab = S.tabs.mrp || 'wo';
    var body = tab === 'wo' ? C.dataTable({ id: 'wo', columns: [{ key: 'n', label: 'Orden', render: function (r) { return html`<span class="cell-main">${r.n}</span>`; } }, { key: 'bom', label: 'Producto' }, { key: 'qty', label: 'Cantidad', num: true }, { key: 'due', label: 'Entrega', render: function (r) { return U.date(r.due); } }, { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, r.status === 'Completada' ? 'success' : 'info'); } }, { key: 'a', label: '', sort: false, render: function (r) { return r.status !== 'Completada' ? C.btn('Completar', { size: 'sm', action: 'wo-done', attrs: 'data-id="' + r.id + '"' }) : ''; } }], rows: D.workOrders }, S.tables.wo)
      : C.dataTable({ id: 'bom', columns: [{ key: 'name', label: 'Lista de materiales', render: function (r) { return html`<span class="cell-main">${r.name}</span>`; } }, { key: 'comps', label: 'Componentes' }, { key: 'cost', label: 'Costo unitario', num: true, render: function (r) { return U.clp(r.cost); } }], rows: D.boms }, S.tables.bom);
    return { subtitle: 'Listas de materiales y órdenes de trabajo.', endpoint: '/mrp/work-orders',
      actions: C.btn(tab === 'wo' ? 'Nueva orden de trabajo' : 'Nueva lista de materiales', { variant: 'primary', icon: 'plus', action: 'toast', attrs: 'data-msg="Creada. (Hoy el alta no está en la pantalla; el backend la permite.)"' }),
      body: html`${C.tabs('mrp', [{ id: 'wo', label: 'Órdenes de trabajo' }, { id: 'bom', label: 'Listas de materiales' }], tab)}${body}` };
  };
  A['wo-done'] = function (el) { D.workOrders.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0].status = 'Completada'; App.render(); App.toast('Orden completada.'); };

  // ======================= Flota =======================
  V.fleet = function () {
    return { subtitle: 'Vehículos de reparto, uso y eficiencia.', endpoint: '/fleet/metrics',
      body: html`<div class="stack"><div class="grid g3">${C.kpi('Vehículos', String(D.vehicles.length))}${C.kpi('Kilómetros del mes', U.int(4820))}${C.kpi('Eficiencia promedio', Math.round(D.vehicles.reduce(function (s, v) { return s + v.eff; }, 0) / D.vehicles.length * 100) + '%')}</div>
        ${C.dataTable({ id: 'veh', columns: [{ key: 'plate', label: 'Patente', render: function (r) { return html`<span class="mono">${r.plate}</span>`; } }, { key: 'model', label: 'Modelo' }, { key: 'km', label: 'Kilometraje', num: true, render: function (r) { return U.int(r.km) + ' km'; } }, { key: 'eff', label: 'Eficiencia', num: true, render: function (r) { return Math.round(r.eff * 100) + '%'; } }, { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, r.status === 'En ruta' ? 'info' : r.status === 'Cargando' ? 'brand' : 'success'); } }], rows: D.vehicles }, S.tables.veh)}</div>` };
  };

  // ======================= Energía =======================
  V.energy = function () {
    var role = REG.effectiveRole(S.role);
    var tabsAll = [{ id: 'chargers', label: 'Cargadores' }, { id: 'remote', label: 'Control remoto' }, { id: 'pricing', label: 'Precios y campañas' }, { id: 'maint', label: 'Mantenimiento' }, { id: 'alerts', label: 'Alertas y métricas' }, { id: 'reports', label: 'Reportes y pronósticos' }, { id: 'models', label: 'Fabricantes y modelos' }];
    var tabs = role === 'AUDITOR' ? tabsAll.filter(function (t) { return t.id === 'reports'; }) : tabsAll;
    var tab = S.tabs.energy || tabs[0].id; if (!tabs.some(function (t) { return t.id === tab; })) tab = tabs[0].id;
    var ST = { Disponible: 'success', Cargando: 'brand', Falla: 'destructive', 'Fuera de línea': 'neutral' };
    var counts = { chargers: 0, remote: 15, pricing: 12, maint: 9, alerts: 10, reports: 13, models: 11 };
    var body;
    if (tab === 'chargers') body = html`<div class="stack"><div class="grid g4">${C.kpi('Cargadores', String(D.chargers.length))}${C.kpi('Sesiones del mes', String(D.chargers.reduce(function (s, c) { return s + c.sessions; }, 0)))}${C.kpi('Energía entregada', U.int(D.chargers.reduce(function (s, c) { return s + c.kwh; }, 0)) + ' kWh')}${C.kpi('Con falla', '1', 'Cargador estacionamiento', { tone: 'down', ic: 'alert' })}</div>
      ${C.dataTable({ id: 'chg', columns: [{ key: 'name', label: 'Cargador', render: function (r) { return html`<span class="cell-main">${r.name}</span>`; } }, { key: 'model', label: 'Modelo' }, { key: 'power', label: 'Potencia', num: true, render: function (r) { return r.power + ' kW'; } }, { key: 'sessions', label: 'Sesiones', num: true }, { key: 'kwh', label: 'kWh', num: true, render: function (r) { return U.int(r.kwh); } }, { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, ST[r.status]); } }], rows: D.chargers }, S.tables.chg)}</div>`;
    else if (tab === 'remote') body = html`<div class="card card-pad stack" style="max-width:640px"><h2>Control remoto OCPP</h2><p class="muted">Envía órdenes a un cargador: reiniciar, desbloquear el conector, iniciar o detener una carga.</p>
      ${C.field('Cargador', C.select('oc-c', D.chargers.map(function (c) { return [c.id, c.name + ' (' + c.status + ')']; })), { id: 'oc-c' })}
      <div class="row">${C.btn('Reiniciar', { icon: 'refresh', action: 'ocpp', attrs: 'data-cmd="Reiniciar"' })}${C.btn('Desbloquear conector', { action: 'ocpp', attrs: 'data-cmd="Desbloquear conector"' })}${C.btn('Detener carga', { variant: 'destructive', action: 'ocpp', attrs: 'data-cmd="Detener carga"' })}</div></div>`;
    else body = html`<div class="card">${C.empty({ icon: { pricing: 'tag', maint: 'wrench', alerts: 'bell', reports: 'chart', models: 'plug' }[tab], title: tabs.filter(function (t) { return t.id === tab; })[0].label, text: 'El backend tiene ' + counts[tab] + ' endpoints para esta sección y ninguno se usa en el front (MAPA-FUNCIONAL §6). Se diseña si el módulo sigue en el producto (decisión D-05).' })}</div>`;
    return { subtitle: 'Red de cargadores eléctricos: estado, control y tarifas.', endpoint: '/api/v1/ocpp',
      body: html`<div class="stack">${C.alert('warning', 'Decisión pendiente', 'No está claro si Energía es parte de este producto, un producto aparte o código sin uso (DESIGN §13, decisión D-05). Tiene 70 endpoints; la pantalla actual llama a 4 que no existen.')}${C.tabs('energy', tabs, tab)}${body}</div>` };
  };
  A.ocpp = function (el) { var cmd = el.getAttribute('data-cmd'); App.confirm({ title: cmd, text: 'La orden se enviará al cargador por OCPP. Si hay una carga en curso, puede interrumpirse.', confirm: cmd, destructive: cmd === 'Detener carga', onConfirm: function () { App.toast('Orden "' + cmd + '" enviada.'); } }); };

  // ======================= Sostenibilidad =======================
  V.sustainability = function () {
    return { subtitle: 'Puntaje ambiental, compras sostenibles y reporte ESG.', endpoint: '/sustainability/score',
      actions: html`${C.btn('Simulador Corfo', { icon: 'calculator', action: 'toast', attrs: 'data-msg="Simulación: podrías postular a un cofinanciamiento de hasta $8.000.000."' })}${C.btn('Generar reporte ESG', { variant: 'primary', icon: 'download', action: 'toast', attrs: 'data-msg="Reporte ESG generado."' })}`,
      body: html`<div class="stack"><div class="grid g4">${C.kpi('Puntaje ESG', '72 / 100', '+6 desde septiembre', { tone: 'up', ic: 'trending' })}${C.kpi('Compras sostenibles', '38%', 'Del total de compras')}${C.kpi('Huella estimada', '14,2 t CO₂e', 'Último trimestre')}${C.kpi('Flota eléctrica', '2 de 3', 'Vehículos')}</div>
        <div class="card"><div class="card-head"><h2>Compras sostenibles por mes</h2></div><div class="card-body">${C.barChart([{ m: 'Jun', v: 24 }, { m: 'Jul', v: 27 }, { m: 'Ago', v: 31 }, { m: 'Sep', v: 33 }, { m: 'Oct', v: 38 }], [{ key: 'v', label: '% de compras sostenibles', color: '--chart-2' }], { unit: '%', title: 'Porcentaje de compras sostenibles por mes' })}</div></div></div>` };
  };

  // ======================= Configuración =======================
  function settingsView(ctx, forceTab) {
    var tab = forceTab || S.tabs.settings || ctx.query.tab || 'company';
    var c = D.company, body;
    if (tab === 'company') body = html`<div class="card card-pad stack" style="max-width:760px"><div class="grid g2">
        ${C.field('Razón social', C.input('co-n', c.name), { id: 'co-n', req: true })}${C.rutField('co-r', 'RUT de la empresa', c.rut, { req: true })}
        ${C.field('Giro', C.input('co-g', c.giro), { id: 'co-g' })}${C.field('Industria', C.select('co-i', ['Comercio mayorista', 'Comercio minorista', 'Manufactura', 'Servicios', 'Transporte', 'Energía'], c.industry), { id: 'co-i' })}
        ${C.field('Dirección', C.input('co-a', c.address), { id: 'co-a' })}${C.field('Email de contacto', C.input('co-e', c.email, { type: 'email' }), { id: 'co-e' })}</div>
        <div class="field"><span class="label">Logo</span><div class="row"><span class="brand-mark" style="width:48px;height:48px">${icon('landmark', 22)}</span>${C.btn('Cambiar logo', { size: 'sm', icon: 'upload', action: 'toast', attrs: 'data-msg="Logo actualizado."' })}</div></div>
        <div>${C.btn('Guardar cambios', { variant: 'primary', action: 'toast', attrs: 'data-msg="Perfil de la empresa guardado."' })}</div></div>`;
    else if (tab === 'users') body = html`<div class="stack">${C.alert('info', 'Gestión de usuarios y roles', 'Se diseña sobre /users y /roles, el sistema más completo del backend (recomendación de la decisión D-04). Eliminar usuario hoy falla: DELETE /sso/users/{id} no existe.')}
      ${C.dataTable({ id: 'usr', search: 'Buscar por nombre o email', searchKeys: ['name', 'email'], filters: [{ key: 'role', label: 'Rol', options: REG.ROLES.map(function (r) { return r; }) }, { key: 'status', label: 'Estado', options: ['Activo', 'Invitación pendiente', 'Suspendido'] }],
        toolbar: C.btn('Invitar usuario', { size: 'sm', variant: 'primary', icon: 'plus', action: 'user-invite' }),
        columns: [{ key: 'name', label: 'Usuario', render: function (r) { return html`<span class="cell-main">${r.name}</span><div class="cell-sub">${r.email}</div>`; } },
          { key: 'role', label: 'Rol', render: function (r) { return html`<label class="sr-only" for="ur-${r.id}">Rol de ${r.name}</label>${C.select('ur-' + r.id, REG.ROLES, r.role, { attrs: 'data-action-change="user-role" data-id="' + r.id + '" style="height:30px;width:auto"' })}`; } },
          { key: 'status', label: 'Estado', render: function (r) { return C.badge(r.status, r.status === 'Activo' ? 'success' : r.status === 'Suspendido' ? 'neutral' : 'warning'); } },
          { key: 'last', label: 'Último acceso', render: function (r) { return r.last ? U.dateTime(r.last) : '—'; } },
          { key: 'a', label: '', sort: false, render: function (r) { return html`<div class="row" style="flex-wrap:nowrap">${C.btn(r.status === 'Suspendido' ? 'Activar' : 'Suspender', { size: 'sm', variant: 'ghost', action: 'user-susp', attrs: 'data-id="' + r.id + '"' })}${C.btn('Eliminar', { size: 'sm', variant: 'ghost', action: 'user-del', attrs: 'data-id="' + r.id + '"' })}</div>`; } }], rows: D.users }, S.tables.usr)}
      <div class="card card-pad"><h3>Permisos por rol</h3><p class="muted small" style="margin:4px 0 8px">Resumen de la matriz rol × área (PLAN §3.2). El menú de cada usuario se construye con ella.</p>
        <div class="table-wrap"><table class="dt compact"><thead><tr><th scope="col">Área</th>${REG.ROLES.slice(0, 7).map(function (r) { return html`<th scope="col" style="text-align:center">${r[1]}</th>`; })}</tr></thead><tbody>
        ${REG.NAV.map(function (g) { return html`<tr><td>${g.label}</td>${REG.ROLES.slice(0, 7).map(function (r) { var n = g.items.filter(function (id) { return REG.canAccess(REG.BY_ID[id], r[0]); }).length; return html`<td style="text-align:center">${n === g.items.length ? html`<span aria-label="Acceso completo" title="Acceso completo">${icon('checkCircle', 16, 'up')}</span>` : n ? html`<span aria-label="Acceso parcial" title="Acceso parcial" class="small strong">${n}/${g.items.length}</span>` : html`<span aria-label="Sin acceso" class="muted">—</span>`}</td>`; })}</tr>`; })}
        </tbody></table></div></div></div>`;
    else if (tab === 'orgs') body = html`<div class="card card-pad stack" style="max-width:640px">${C.devTag({ dev: 'planificado', real: 'Solo backend', missing: 'El backend tiene /organizations/* (9 endpoints) para organizaciones jerárquicas, sin pantalla.', src: 'MAPA-FUNCIONAL.md §6' })}<h2>Organizaciones</h2><p class="muted">Agrupa empresas relacionadas (holding y filiales) para administrar usuarios en común.</p>
      <ul class="timeline"><li><span class="tl-ico badge t-brand" style="padding:0;justify-content:center">${icon('building', 14)}</span><div><strong>Grupo Quillay</strong><div class="muted">2 empresas</div></div></li><li><span class="tl-ico badge t-neutral" style="padding:0;justify-content:center">${icon('building', 14)}</span><div><strong>${c.name}</strong><div class="muted">${c.rut}</div></div></li></ul></div>`;
    else if (tab === 'cert') body = html`<div class="grid g2" style="max-width:960px"><div class="card card-pad stack"><h2>Certificado digital</h2>
        <dl style="display:grid;grid-template-columns:auto 1fr;gap:6px 16px;margin:0"><dt class="muted">Titular</dt><dd style="margin:0">${c.cert.holder}</dd><dt class="muted">RUT del titular</dt><dd style="margin:0">${U.rut(c.cert.holderRut)}</dd><dt class="muted">Vence</dt><dd style="margin:0">${U.date(c.cert.validUntil)} ${C.badge('En 54 días', 'warning')}</dd></dl>
        ${C.alert('warning', null, 'Hoy el sistema no extrae el RUT del titular (devuelve "UNKNOWN-RUT", RIESGOS R-10) y guarda el certificado y su contraseña sin cifrar (R-01).')}
        <div>${C.btn('Reemplazar certificado', { icon: 'upload', action: 'toast', attrs: 'data-msg="Certificado reemplazado."' })}</div></div>
      <div class="card card-pad stack"><h2>Ambiente del SII</h2><p class="muted">Define a qué servicio del SII se envían los documentos.</p>
        <label class="check"><input type="radio" name="env" value="cert" ${S.env === 'cert' ? raw('checked') : ''} data-action-change="set-env"><span><strong>Certificación</strong><br><span class="small muted">Pruebas. Los documentos no tienen validez tributaria.</span></span></label>
        <label class="check"><input type="radio" name="env" value="prod" ${S.env === 'prod' ? raw('checked') : ''} data-action-change="set-env"><span><strong>Producción</strong><br><span class="small muted">Documentos con validez tributaria; consumen folios reales.</span></span></label>
        ${C.alert('info', null, 'Hoy el sistema envía a producción por defecto y no hay configuración de certificación (RIESGOS R-05).')}</div></div>`;
    else if (tab === 'modules') body = html`<div class="card card-pad stack" style="max-width:640px"><h2>Módulos por rubro</h2><p class="muted">Activa solo lo que usa tu empresa. Los módulos desactivados no aparecen en el menú.</p>
      ${REG.MODULES.map(function (m) { return html`<div class="row-between" style="padding:6px 0;border-bottom:1px solid var(--border)"><label for="mod-${m[0]}">${m[1]}</label><span class="switch"><input type="checkbox" role="switch" id="mod-${m[0]}" value="${m[0]}" ${S.modules[m[0]] ? raw('checked') : ''} data-action-change="toggle-module"><span></span></span></div>`; })}
      ${C.alert('info', 'Propuesta nueva', 'No existe hoy un campo de módulos por empresa en el backend (PLAN B-10).')}</div>`;
    else body = html`<div class="card card-pad stack" style="max-width:640px"><h2>Notificaciones</h2>
      ${[['CAF por agotarse', true], ['Documento rechazado por el SII', true], ['Vencimientos tributarios', true], ['Certificado por vencer', true], ['Resumen semanal por email', false]].map(function (n, i) { return html`<div class="row-between" style="padding:6px 0;border-bottom:1px solid var(--border)"><label for="nt-${i}">${n[0]}</label><span class="switch"><input type="checkbox" role="switch" id="nt-${i}" ${n[1] ? raw('checked') : ''}><span></span></span></div>`; })}
      <div>${C.btn('Guardar preferencias', { variant: 'primary', action: 'toast', attrs: 'data-msg="Hoy esto falla: PUT /notifications/preferences no existe." data-tone="bad"' })}</div></div>`;
    return { title: 'Configuración', crumbs: [], subtitle: 'Empresa, usuarios, certificado, módulos y notificaciones.', endpoint: '/company/profile',
      body: html`${forceTab ? html`<div style="margin-bottom:12px">${C.alert('info', 'Esta dirección se unificó', 'El perfil de empresa era una pantalla duplicada. En el rediseño es la pestaña Empresa de Configuración.', C.btn('Ir a Configuración', { size: 'sm', href: '#/settings' }))}</div>` : ''}
        ${C.tabs('settings', [{ id: 'company', label: 'Empresa' }, { id: 'users', label: 'Usuarios y roles' }, { id: 'orgs', label: 'Organizaciones' }, { id: 'cert', label: 'Certificado y SII' }, { id: 'modules', label: 'Módulos' }, { id: 'notif', label: 'Notificaciones' }], tab)}${body}` };
  }
  V.settings = function (ctx) { return settingsView(ctx); };
  V['settings-company'] = function (ctx) { return settingsView(ctx, 'company'); };
  CH['set-env'] = function (el) { S.env = el.value; App.render(); App.toast(el.value === 'prod' ? 'Ambiente: producción. Los documentos tendrán validez tributaria.' : 'Ambiente: certificación.', el.value === 'prod' ? 'info' : 'ok'); };
  CH['user-role'] = function (el) { var u = D.users.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; u.role = el.value; App.toast('Rol de ' + u.name + ' cambiado a ' + REG.ROLE_LABEL[u.role] + '.'); };
  A['user-susp'] = function (el) { var u = D.users.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; u.status = u.status === 'Suspendido' ? 'Activo' : 'Suspendido'; App.render(); App.toast(u.name + ': ' + u.status.toLowerCase() + '.'); };
  A['user-del'] = function (el) { var u = D.users.filter(function (x) { return x.id === el.getAttribute('data-id'); })[0]; App.confirm({ title: 'Eliminar a ' + u.name, text: 'Perderá el acceso de inmediato. Sus documentos y registros se conservan.', confirm: 'Eliminar usuario', destructive: true, icon: 'trash', onConfirm: function () { D.users.splice(D.users.indexOf(u), 1); App.render(); App.toast('Usuario eliminado.'); } }); };
  A['user-invite'] = function () { App.dialog({ title: 'Invitar usuario', body: html`<div class="stack">${C.field('Email', C.input('iv-e', '', { type: 'email', placeholder: 'nombre@empresa.cl' }), { id: 'iv-e', req: true })}${C.field('Rol', C.select('iv-r', REG.ROLES, 'ACCOUNTANT'), { id: 'iv-r', hint: 'Define qué áreas verá en el menú.' })}</div>`, actions: html`${C.btn('Cancelar', { action: 'dialog-close' })}${C.btn('Enviar invitación', { variant: 'primary', action: 'iv-send' })}` }); };
  A['iv-send'] = function () { App.closeDialog(); App.toast('Invitación enviada. Vence en 7 días.'); };

  // ======================= Notificaciones =======================
  V.notifications = function () {
    return { subtitle: 'Avisos del sistema sobre plazos, folios y documentos.', endpoint: '/notifications',
      actions: C.btn('Preferencias', { icon: 'settings', href: '#/settings?tab=notif' }),
      empty: { icon: 'bell', title: 'No tienes notificaciones', text: 'Te avisaremos aquí cuando un plazo se acerque o el SII responda.' },
      body: html`<div class="card">${D.notifications.map(function (n, i) { return html`<a href="${n.link}" class="row" style="padding:14px 16px;gap:12px;flex-wrap:nowrap;text-decoration:none;color:inherit;${i ? 'border-top:1px solid var(--border)' : ''}"><span class="badge t-${n.tone}" style="height:32px;width:32px;padding:0;justify-content:center;flex-shrink:0">${icon(n.icon, 16)}</span><span style="flex:1"><strong style="display:block">${n.title}</strong><span class="muted">${n.body}</span></span><span class="small muted nowrap">${U.dateTime(n.when)}</span></a>`; })}</div>` };
  };
})();
