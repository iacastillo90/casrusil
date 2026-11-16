/* Datos de demostración. Todo es ficticio: empresas, personas y montos inventados.
   Los RUT se calculan con dígito verificador válido (U.rut). Fecha de referencia: 15-11-2026. */
(function () {
  'use strict';
  var U = window.U;
  var TODAY = '2026-11-15';

  var company = {
    name: 'Comercial Quillay Verde SpA', rutNum: 76481205, giro: 'Venta al por mayor de alimentos e insumos',
    address: 'Av. Los Carrera 1450, Concepción', industry: 'Comercio mayorista', email: 'contacto@quillayverde.cl',
    cert: { holder: 'Valentina Rojas Fuentes', holderRut: 13874502, validUntil: '2027-01-08' }
  };
  company.rut = U.rut(company.rutNum);

  var parties = [
    { id: 'p1', name: 'Ferretería Cordillera Norte Ltda.', n: 77214889, kind: 'Cliente', city: 'Antofagasta', email: 'pagos@cordilleranorte.cl' },
    { id: 'p2', name: 'Panadería La Espiga Dorada EIRL', n: 76903112, kind: 'Cliente', city: 'Talcahuano', email: 'compras@espigadorada.cl' },
    { id: 'p3', name: 'Transportes Austral Sur SpA', n: 76552031, kind: 'Cliente', city: 'Puerto Montt', email: 'facturas@australsur.cl' },
    { id: 'p4', name: 'Constructora Mar de Fondo S.A.', n: 96874220, kind: 'Cliente', city: 'Viña del Mar', email: 'dte@mardefondo.cl' },
    { id: 'p5', name: 'Café Tostado Valdivia SpA', n: 77630458, kind: 'Cliente', city: 'Valdivia', email: 'admin@tostadovaldivia.cl' },
    { id: 'p6', name: 'Librería El Copihue Ltda.', n: 78115902, kind: 'Cliente', city: 'Chillán', email: 'contabilidad@elcopihue.cl' },
    { id: 'p7', name: 'Agrícola Valle Escondido SpA', n: 76299674, kind: 'Cliente', city: 'Rancagua', email: 'finanzas@valleescondido.cl' },
    { id: 'p8', name: 'Veterinaria Huellas del Sur SpA', n: 77008341, kind: 'Cliente', city: 'Temuco', email: 'pagos@huellasdelsur.cl' },
    { id: 'p9', name: 'Imprenta Puerto Claro Ltda.', n: 76744517, kind: 'Cliente', city: 'Valparaíso', email: 'dte@puertoclaro.cl' },
    { id: 's1', name: 'Distribuidora Andes Mayorista SpA', n: 76120553, kind: 'Proveedor', city: 'Santiago', email: 'ventas@andesmayorista.cl' },
    { id: 's2', name: 'Energía Limpia Atacama SpA', n: 77402198, kind: 'Proveedor', city: 'Copiapó', email: 'clientes@limpiaatacama.cl' },
    { id: 's3', name: 'Plásticos Biobío Ltda.', n: 79551306, kind: 'Proveedor', city: 'Los Ángeles', email: 'ventas@plasticosbiobio.cl' },
    { id: 's4', name: 'Molino Ñuble Alto S.A.', n: 96512870, kind: 'Proveedor', city: 'Chillán', email: 'facturacion@nublealto.cl' }
  ];
  parties.forEach(function (p) { p.rut = U.rut(p.n); });
  function party(id) { return parties.filter(function (p) { return p.id === id; })[0]; }

  var products = [
    { sku: 'HAR-025', name: 'Harina especial 25 kg', unit: 'saco', price: 18900, cost: 14200, stock: 340, min: 120, loc: 'A-01-02' },
    { sku: 'ACE-005', name: 'Aceite maravilla 5 L', unit: 'bidón', price: 12490, cost: 9300, stock: 86, min: 100, loc: 'A-02-01' },
    { sku: 'CAF-001', name: 'Café en grano 1 kg', unit: 'bolsa', price: 15990, cost: 10800, stock: 210, min: 60, loc: 'B-01-04' },
    { sku: 'AZU-025', name: 'Azúcar granulada 25 kg', unit: 'saco', price: 21500, cost: 17100, stock: 155, min: 80, loc: 'A-01-05' },
    { sku: 'TE-100', name: 'Té negro caja 100 u', unit: 'caja', price: 3990, cost: 2450, stock: 620, min: 200, loc: 'B-02-01' },
    { sku: 'SER-500', name: 'Servilletas pack 500 u', unit: 'pack', price: 2790, cost: 1650, stock: 44, min: 150, loc: 'C-03-02' },
    { sku: 'LEV-500', name: 'Levadura seca 500 g', unit: 'paquete', price: 4590, cost: 2980, stock: 300, min: 100, loc: 'B-03-03' },
    { sku: 'VAS-100', name: 'Vasos compostables 100 u', unit: 'pack', price: 6990, cost: 4100, stock: 0, min: 50, loc: 'C-01-01' },
    { sku: 'SAL-001', name: 'Sal de mar 1 kg', unit: 'bolsa', price: 1290, cost: 690, stock: 980, min: 300, loc: 'A-03-01' },
    { sku: 'MAN-010', name: 'Manjar repostero 10 kg', unit: 'balde', price: 32900, cost: 24800, stock: 38, min: 20, loc: 'D-01-01' }
  ];

  var TIPOS = {
    33: 'Factura electrónica', 34: 'Factura exenta', 39: 'Boleta electrónica', 43: 'Liquidación factura',
    46: 'Factura de compra', 52: 'Guía de despacho', 56: 'Nota de débito', 61: 'Nota de crédito',
    110: 'Factura de exportación', 111: 'Nota de débito de exportación', 112: 'Nota de crédito de exportación'
  };

  function mkLines(spec) {
    return spec.map(function (s, i) {
      var p = products[s[0]];
      return { n: i + 1, sku: p.sku, desc: p.name, qty: s[1], unit: p.unit, price: s[2] || p.price, exempt: false };
    });
  }
  function totals(lines, tipo) {
    var neto = 0, exento = 0;
    lines.forEach(function (l) { var t = l.qty * l.price; if (l.exempt || tipo === 34) exento += t; else neto += t; });
    var iva = Math.round(neto * 0.19);
    return { neto: neto, exento: exento, iva: iva, total: neto + exento + iva };
  }

  var invoices = [];
  function addInv(o) {
    o.lines = mkLines(o.spec); delete o.spec;
    var t = totals(o.lines, o.tipo); o.neto = t.neto; o.exento = t.exento; o.iva = t.iva; o.total = t.total;
    o.history = o.history || [];
    invoices.push(o);
  }
  addInv({ id: 'f1024', tipo: 33, folio: 1024, party: 'p4', date: '2026-11-14', due: '2026-12-14', status: 'SENT', trackId: '0248115932', spec: [[0, 40], [3, 20]], payment: 'Crédito 30 días',
    history: [['2026-11-14T10:02', 'DRAFT', 'Matías Fuentes creó el borrador'], ['2026-11-14T10:20', 'SIGNED', 'Valentina Rojas firmó el documento'], ['2026-11-14T10:21', 'SENT', 'Enviado al SII. TrackID 0248115932']] });
  addInv({ id: 'f1023', tipo: 33, folio: 1023, party: 'p2', date: '2026-11-13', due: '2026-12-13', status: 'ACCEPTED', trackId: '0248110457', spec: [[0, 25], [6, 30], [3, 10]], payment: 'Crédito 30 días',
    history: [['2026-11-13T09:10', 'DRAFT', 'Creado'], ['2026-11-13T09:15', 'SIGNED', 'Firmado'], ['2026-11-13T09:16', 'SENT', 'Enviado al SII'], ['2026-11-13T11:40', 'ACCEPTED', 'El SII aceptó el documento']] });
  addInv({ id: 'f1022', tipo: 33, folio: 1022, party: 'p5', date: '2026-11-12', due: '2026-11-27', status: 'ACCEPTED_WITH_OBJECTIONS', trackId: '0248102296', spec: [[2, 60], [4, 40]], payment: 'Crédito 15 días',
    objections: ['Giro del receptor no coincide con el registrado en el SII.', 'Dirección del receptor incompleta (falta comuna).'],
    history: [['2026-11-12T16:01', 'DRAFT', 'Creado'], ['2026-11-12T16:05', 'SIGNED', 'Firmado'], ['2026-11-12T16:06', 'SENT', 'Enviado al SII'], ['2026-11-12T18:30', 'ACCEPTED_WITH_OBJECTIONS', 'Aceptado con 2 reparos']] });
  addInv({ id: 'f1021', tipo: 33, folio: 1021, party: 'p1', date: '2026-11-11', due: '2026-12-11', status: 'REJECTED', trackId: '0248097810', spec: [[1, 30], [5, 50]], payment: 'Crédito 30 días',
    reason: 'Error en firma del documento: el RUT del certificado no corresponde al emisor autorizado (código RCH-05).',
    history: [['2026-11-11T12:00', 'DRAFT', 'Creado'], ['2026-11-11T12:04', 'SIGNED', 'Firmado'], ['2026-11-11T12:05', 'SENT', 'Enviado al SII'], ['2026-11-11T14:12', 'REJECTED', 'Rechazado por el SII']] });
  addInv({ id: 'f1020', tipo: 33, folio: 1020, party: 'p7', date: '2026-11-10', due: '2026-12-10', status: 'ACCEPTED', trackId: '0248090011', spec: [[3, 80], [8, 200]], payment: 'Crédito 30 días',
    history: [['2026-11-10T10:00', 'ACCEPTED', 'El SII aceptó el documento']] });
  addInv({ id: 'f1019', tipo: 33, folio: 1019, party: 'p9', date: '2026-11-07', due: '2026-12-07', status: 'CANCELLED', trackId: '0248061174', spec: [[5, 120]], payment: 'Contado',
    history: [['2026-11-07T10:00', 'ACCEPTED', 'Aceptado'], ['2026-11-09T15:30', 'CANCELLED', 'Anulado por la nota de crédito N° 18']] });
  addInv({ id: 'f1018', tipo: 33, folio: 1018, party: 'p3', date: '2026-11-05', due: '2026-12-05', status: 'ACCEPTED', trackId: '0248040322', spec: [[0, 60], [1, 40], [9, 6]], payment: 'Crédito 30 días', history: [] });
  addInv({ id: 'b3120', tipo: 39, folio: 3120, party: 'p6', date: '2026-11-14', due: '2026-11-14', status: 'ACCEPTED', trackId: '0248116003', spec: [[4, 4], [8, 6]], payment: 'Contado', history: [] });
  addInv({ id: 'e41', tipo: 34, folio: 41, party: 'p8', date: '2026-11-09', due: '2026-12-09', status: 'ACCEPTED', trackId: '0248071290', spec: [[8, 100]], payment: 'Crédito 30 días', history: [] });
  addInv({ id: 'nc18', tipo: 61, folio: 18, party: 'p9', date: '2026-11-09', due: '2026-11-09', status: 'ACCEPTED', trackId: '0248079905', ref: 'f1019', spec: [[5, 120]], payment: '—', history: [] });
  addInv({ id: 'd-new1', tipo: 33, folio: null, party: 'p6', date: '2026-11-15', due: '2026-12-15', status: 'DRAFT', trackId: null, spec: [[4, 30], [6, 12]], payment: 'Crédito 30 días',
    history: [['2026-11-15T08:41', 'DRAFT', 'Matías Fuentes creó el borrador']] });
  addInv({ id: 'f-sig', tipo: 33, folio: 1017, party: 'p2', date: '2026-11-04', due: '2026-12-04', status: 'SIGNED', trackId: null, spec: [[0, 15]], payment: 'Crédito 30 días',
    history: [['2026-11-04T17:20', 'SIGNED', 'Firmado, pendiente de envío']] });
  addInv({ id: 'f1016', tipo: 33, folio: 1016, party: 'p4', date: '2026-10-29', due: '2026-11-28', status: 'ACCEPTED', trackId: '0247992210', spec: [[3, 100], [0, 100]], payment: 'Crédito 30 días', history: [] });
  addInv({ id: 'f1015', tipo: 33, folio: 1015, party: 'p5', date: '2026-10-24', due: '2026-11-23', status: 'ACCEPTED', trackId: '0247950018', spec: [[2, 40]], payment: 'Crédito 30 días', history: [] });

  function invById(id) { return invoices.filter(function (i) { return i.id === id; })[0]; }
  function tot(id) { return invById(id).total; }

  var cafs = [
    { id: 'c1', tipo: 33, from: 1, to: 500, used: 500, issued: '2025-03-02' },
    { id: 'c2', tipo: 33, from: 501, to: 1100, used: 524, issued: '2026-04-18' },
    { id: 'c3', tipo: 34, from: 1, to: 200, used: 41, issued: '2025-09-10' },
    { id: 'c4', tipo: 39, from: 1, to: 5000, used: 3120, issued: '2026-01-05' },
    { id: 'c5', tipo: 61, from: 1, to: 100, used: 18, issued: '2025-09-10' },
    { id: 'c6', tipo: 56, from: 1, to: 50, used: 3, issued: '2025-09-10' }
  ];

  var leads = [
    { id: 'l1', name: 'Pastelería Nube de Azúcar', contact: 'Rocío Paredes', value: 2400000, stage: 'Nuevo', src: 'Sitio web' },
    { id: 'l2', name: 'Hostal Brisa del Lago', contact: 'Tomás Riquelme', value: 1150000, stage: 'Nuevo', src: 'Referido' },
    { id: 'l3', name: 'Casino Escolar Alerce', contact: 'Paula Gutiérrez', value: 6800000, stage: 'Contactado', src: 'Feria' },
    { id: 'l4', name: 'Restaurante Mar y Leña', contact: 'Ignacio Sepúlveda', value: 3900000, stage: 'Propuesta', src: 'Llamada' },
    { id: 'l5', name: 'Cafetería Grano Austral', contact: 'Josefa Navarro', value: 1750000, stage: 'Propuesta', src: 'Sitio web' },
    { id: 'l6', name: 'Banquetería Los Maitenes', contact: 'Cristóbal Vera', value: 5200000, stage: 'Ganado', src: 'Referido' }
  ];
  var quotes = [
    { id: 'q207', n: 207, party: 'p3', date: '2026-11-12', valid: '2026-11-27', total: 3570000, status: 'Enviada' },
    { id: 'q206', n: 206, party: 'p5', date: '2026-11-08', valid: '2026-11-23', total: 1904000, status: 'Aceptada' },
    { id: 'q205', n: 205, party: 'p8', date: '2026-11-03', valid: '2026-11-18', total: 892500, status: 'Rechazada' },
    { id: 'q204', n: 204, party: 'p2', date: '2026-10-30', valid: '2026-11-14', total: 2142000, status: 'Vencida' },
    { id: 'q203', n: 203, party: 'p4', date: '2026-10-28', valid: '2026-11-28', total: 11305000, status: 'Aceptada' }
  ];
  var purchaseOrders = [
    { id: 'oc88', n: 88, party: 's1', date: '2026-11-13', total: 4760000, status: 'Enviada', eta: '2026-11-20' },
    { id: 'oc87', n: 87, party: 's4', date: '2026-11-10', total: 7140000, status: 'Recibida', eta: '2026-11-14' },
    { id: 'oc86', n: 86, party: 's3', date: '2026-11-06', total: 1309000, status: 'Borrador', eta: '—' },
    { id: 'oc85', n: 85, party: 's2', date: '2026-10-31', total: 892500, status: 'Recibida', eta: '2026-11-04' }
  ];
  var fees = [
    { id: 'h1', n: 412, person: 'Camila Soto Pérez', rn: 15482336, date: '2026-11-08', gross: 850000, desc: 'Diseño de etiquetas línea café' },
    { id: 'h2', n: 77, person: 'Diego Muñoz Lagos', rn: 17903221, date: '2026-11-04', gross: 420000, desc: 'Mantención sitio web' },
    { id: 'h3', n: 1290, person: 'Fernanda Araya Vidal', rn: 16220874, date: '2026-10-30', gross: 1200000, desc: 'Asesoría tributaria octubre' },
    { id: 'h4', n: 413, person: 'Camila Soto Pérez', rn: 15482336, date: '2026-10-15', gross: 650000, desc: 'Fotografía de catálogo' }
  ];
  var RET = 0.1525; // retención honorarios 2026 (Ley 21.133). En el sistema actual se usa la tasa de 2024 (R-07).
  fees.forEach(function (f) { f.rut = U.rut(f.rn); f.ret = Math.round(f.gross * RET); f.net = f.gross - f.ret; });

  var employees = [
    { id: 'e1', name: 'Matías Fuentes Ortiz', rn: 15903447, role: 'Jefe de administración', base: 1850000, hire: '2021-03-01', health: 'Isapre' },
    { id: 'e2', name: 'Valentina Rojas Fuentes', rn: 13874502, role: 'Contadora', base: 1720000, hire: '2020-07-15', health: 'Fonasa' },
    { id: 'e3', name: 'Joaquín Herrera Díaz', rn: 18447120, role: 'Vendedor', base: 780000, hire: '2023-01-09', health: 'Fonasa' },
    { id: 'e4', name: 'Antonia Castro Pino', rn: 19230558, role: 'Vendedora', base: 780000, hire: '2024-05-02', health: 'Fonasa' },
    { id: 'e5', name: 'Benjamín Salinas Rey', rn: 17655093, role: 'Bodeguero', base: 690000, hire: '2022-10-17', health: 'Fonasa' },
    { id: 'e6', name: 'Isidora Morales Lazo', rn: 16884231, role: 'Tesorera', base: 1390000, hire: '2021-11-22', health: 'Isapre' },
    { id: 'e7', name: 'Vicente Tapia Lillo', rn: 20118746, role: 'Conductor', base: 720000, hire: '2025-02-03', health: 'Fonasa' }
  ];
  employees.forEach(function (e) {
    e.rut = U.rut(e.rn);
    e.grat = Math.min(Math.round(e.base * 0.25), 209396);
    var imp = e.base + e.grat;
    e.afp = Math.round(imp * 0.1144); e.salud = Math.round(imp * 0.07); e.ces = Math.round(imp * 0.006);
    e.tax = imp > 1100000 ? Math.round((imp - 1100000) * 0.04) : 0;
    e.liquido = imp - e.afp - e.salud - e.ces - e.tax;
  });

  var banks = [
    { id: 'bk1', bank: 'Banco Cumbre', acc: 'Cuenta corriente ****4821', balance: 48215300, sync: '2026-11-15T07:30', status: 'Conectada' },
    { id: 'bk2', bank: 'Banco Litoral', acc: 'Cuenta corriente ****0937', balance: 12890450, sync: '2026-11-14T22:10', status: 'Requiere reautorización' }
  ];
  var bankMoves = [
    { id: 'm1', date: '2026-11-14', desc: 'Transferencia de Constructora Mar de Fondo', amount: tot('f1016'), match: 'r1' },
    { id: 'm2', date: '2026-11-13', desc: 'Transferencia de Panadería La Espiga', amount: tot('f1023'), match: 'r2' },
    { id: 'm3', date: '2026-11-12', desc: 'Pago proveedor Molino Ñuble Alto', amount: -7140000, match: 'r3' },
    { id: 'm4', date: '2026-11-12', desc: 'Comisión mantención cuenta', amount: -12900, match: null },
    { id: 'm5', date: '2026-11-11', desc: 'Depósito en efectivo sucursal centro', amount: 380000, match: null },
    { id: 'm6', date: '2026-11-10', desc: 'Transferencia Agrícola Valle Escondido', amount: tot('f1020'), match: 'r4' }
  ];
  var ledgerRecords = [
    { id: 'r1', date: '2026-10-29', desc: 'Cobro factura N° 1016, Constructora Mar de Fondo', amount: tot('f1016') },
    { id: 'r2', date: '2026-11-13', desc: 'Cobro factura N° 1023, Panadería La Espiga', amount: tot('f1023') },
    { id: 'r3', date: '2026-11-10', desc: 'Pago OC 87, Molino Ñuble Alto', amount: -7140000 },
    { id: 'r4', date: '2026-11-10', desc: 'Cobro factura N° 1020, Agrícola Valle Escondido', amount: tot('f1020') },
    { id: 'r5', date: '2026-11-09', desc: 'Cobro factura N° 1015, Café Tostado Valdivia', amount: tot('f1015') }
  ];

  var accounts = [
    ['1', 'Activo', null], ['1.1', 'Activo corriente', null], ['1.1.01', 'Caja', 1420000], ['1.1.02', 'Bancos', 61105750], ['1.1.03', 'Clientes', 38940120],
    ['1.1.04', 'IVA crédito fiscal', 4211830], ['1.1.05', 'Existencias', 21388400], ['1.2', 'Activo no corriente', null], ['1.2.01', 'Vehículos', 32900000],
    ['1.2.02', 'Depreciación acumulada vehículos', -9870000], ['1.2.03', 'Equipos computacionales', 6480000],
    ['2', 'Pasivo', null], ['2.1.01', 'Proveedores', -22410300], ['2.1.02', 'IVA débito fiscal', -9842023], ['2.1.03', 'Retenciones por pagar', -1083440], ['2.1.04', 'Remuneraciones por pagar', -7020600],
    ['3', 'Patrimonio', null], ['3.1.01', 'Capital', -80000000], ['3.1.02', 'Resultados acumulados', -30852017],
    ['4', 'Ingresos', null], ['4.1.01', 'Ventas', -51800120], ['5', 'Costos y gastos', null], ['5.1.01', 'Costo de ventas', 36120400], ['5.2.01', 'Remuneraciones', 7914000], ['5.2.02', 'Arriendos', 1850000], ['5.2.03', 'Depreciación', 548000]
  ];
  function saleEntry(n, id, warn) {
    var i = invById(id);
    return { id: 'a' + n, n: n, date: i.date, desc: 'Venta según factura N° ' + i.folio + (warn ? ' (rechazada por el SII)' : ''), ref: id, warn: !!warn,
      lines: [['1.1.03', 'Clientes', i.total, 0], ['4.1.01', 'Ventas', 0, i.neto], ['2.1.02', 'IVA débito fiscal', 0, i.iva]] };
  }
  var entries = [
    saleEntry(812, 'f1024'),
    { id: 'a811', n: 811, date: '2026-11-13', desc: 'Cobro factura N° 1023', ref: 'f1023', lines: [['1.1.02', 'Bancos', tot('f1023'), 0], ['1.1.03', 'Clientes', 0, tot('f1023')]] },
    saleEntry(810, 'f1023'),
    saleEntry(809, 'f1021', true),
    { id: 'a808', n: 808, date: '2026-11-10', desc: 'Pago OC 87, Molino Ñuble Alto', ref: 'oc87', lines: [['2.1.01', 'Proveedores', 7140000, 0], ['1.1.02', 'Bancos', 0, 7140000]] },
    { id: 'a807', n: 807, date: '2026-11-08', desc: 'Boleta de honorarios N° 412', ref: 'h1', lines: [['5.2.04', 'Honorarios', 850000, 0], ['2.1.03', 'Retenciones por pagar', 0, 129625], ['2.1.05', 'Honorarios por pagar', 0, 720375]] }
  ];
  entries.forEach(function (e) { e.total = e.lines.reduce(function (s, l) { return s + l[2]; }, 0); });

  var assets = [
    { id: 'af1', name: 'Camión 3/4 reparto', cat: 'Vehículos', date: '2023-03-15', cost: 24900000, life: 7, dep: 7820000 },
    { id: 'af2', name: 'Furgón utilitario', cat: 'Vehículos', date: '2025-06-01', cost: 8000000, life: 7, dep: 2050000 },
    { id: 'af3', name: 'Grúa horquilla eléctrica', cat: 'Maquinaria', date: '2024-02-10', cost: 11600000, life: 10, dep: 3190000 },
    { id: 'af4', name: 'Notebooks administración (6)', cat: 'Equipos computacionales', date: '2025-01-20', cost: 6480000, life: 6, dep: 1980000 }
  ];
  var projects = [
    { id: 'cc1', code: 'CC-01', name: 'Sucursal Concepción', owner: 'Matías Fuentes', budget: 42000000, spent: 31220000, status: 'Activo' },
    { id: 'cc2', code: 'CC-02', name: 'Bodega Talcahuano', owner: 'Benjamín Salinas', budget: 18500000, spent: 17940000, status: 'Activo' },
    { id: 'cc3', code: 'CC-03', name: 'Línea café de especialidad', owner: 'Joaquín Herrera', budget: 9000000, spent: 3150000, status: 'En pausa' },
    { id: 'cc4', code: 'CC-04', name: 'Flota de reparto', owner: 'Vicente Tapia', budget: 26000000, spent: 12480000, status: 'Activo' }
  ];
  var documents = [
    { id: 'd1', name: 'Factura proveedor Andes Mayorista N° 55120.pdf', type: 'Factura de compra', date: '2026-11-13', status: 'Analizado', conf: 0.96 },
    { id: 'd2', name: 'Contrato arriendo bodega Talcahuano.pdf', type: 'Contrato', date: '2026-11-02', status: 'Analizado', conf: 0.91 },
    { id: 'd3', name: 'Cartola Banco Cumbre octubre.pdf', type: 'Cartola bancaria', date: '2026-11-01', status: 'Pendiente', conf: null },
    { id: 'd4', name: 'Boleta honorarios Camila Soto 412.pdf', type: 'Boleta de honorarios', date: '2026-11-08', status: 'Analizado', conf: 0.88 }
  ];
  var users = [
    { id: 'u1', name: 'Matías Fuentes Ortiz', email: 'mfuentes@quillayverde.cl', role: 'ADMIN', status: 'Activo', last: '2026-11-15T08:40' },
    { id: 'u2', name: 'Valentina Rojas Fuentes', email: 'vrojas@quillayverde.cl', role: 'ACCOUNTANT', status: 'Activo', last: '2026-11-15T08:12' },
    { id: 'u3', name: 'Isidora Morales Lazo', email: 'imorales@quillayverde.cl', role: 'TREASURER', status: 'Activo', last: '2026-11-14T18:03' },
    { id: 'u4', name: 'Joaquín Herrera Díaz', email: 'jherrera@quillayverde.cl', role: 'SALES', status: 'Activo', last: '2026-11-14T16:47' },
    { id: 'u5', name: 'Benjamín Salinas Rey', email: 'bsalinas@quillayverde.cl', role: 'OPERATIONS', status: 'Activo', last: '2026-11-13T09:15' },
    { id: 'u6', name: 'Rosa Vergara Cid', email: 'rvergara@auditorasur.cl', role: 'AUDITOR', status: 'Invitación pendiente', last: null },
    { id: 'u7', name: 'Antonia Castro Pino', email: 'acastro@quillayverde.cl', role: 'USER', status: 'Suspendido', last: '2026-10-02T10:00' }
  ];
  var auditAlerts = [
    { id: 'aa1', kind: 'Duplicado', sev: 'Alta', desc: 'Factura de compra N° 55120 de Distribuidora Andes Mayorista registrada dos veces', date: '2026-11-13', amount: 4760000 },
    { id: 'aa2', kind: 'Monto inusual', sev: 'Media', desc: 'Pago a Plásticos Biobío 3,4 veces mayor que su promedio mensual', date: '2026-11-06', amount: 1309000 },
    { id: 'aa3', kind: 'Espejo SII', sev: 'Alta', desc: '3 facturas en el RCV de octubre que no están en el ERP', date: '2026-11-03', amount: 2190400 },
    { id: 'aa4', kind: 'Espejo SII', sev: 'Baja', desc: 'Diferencia de $1 por redondeo en factura N° 1015', date: '2026-10-25', amount: 1 }
  ];
  var rules = [
    { id: 'ru1', match: 'RUT 76.120.553-?', account: '5.1.01 Costo de ventas', hits: 42, by: 'IA' },
    { id: 'ru2', match: 'Glosa contiene "arriendo"', account: '5.2.02 Arriendos', hits: 11, by: 'Valentina Rojas' },
    { id: 'ru3', match: 'RUT 77.402.198-?', account: '5.2.05 Servicios básicos', hits: 9, by: 'IA' }
  ];
  rules[0].match = 'RUT ' + U.rut(76120553); rules[2].match = 'RUT ' + U.rut(77402198);

  var notifications = [
    { id: 'n1', tone: 'warning', icon: 'layers', title: 'Quedan 76 folios de factura electrónica', body: 'Al ritmo actual se agotan en unos 9 días. Solicita un nuevo CAF al SII.', when: '2026-11-15T07:00', link: '#/sii/caf' },
    { id: 'n2', tone: 'info', icon: 'calendar', title: 'El F29 de octubre vence el 20-11-2026', body: 'La declaración está lista para revisar.', when: '2026-11-14T09:00', link: '#/accounting/f29' },
    { id: 'n3', tone: 'destructive', icon: 'xCircle', title: 'El SII rechazó la factura N° 1021', body: 'Motivo: error en la firma del documento.', when: '2026-11-11T14:12', link: '#/invoices/f1021' },
    { id: 'n4', tone: 'warning', icon: 'key', title: 'El certificado digital vence el 08-01-2027', body: 'Renuévalo para seguir firmando documentos.', when: '2026-11-10T09:00', link: '#/settings?tab=cert' }
  ];
  var chargers = [
    { id: 'ch1', name: 'Cargador bodega 1', model: 'Voltara DC-60', power: 60, status: 'Disponible', sessions: 128, kwh: 4180 },
    { id: 'ch2', name: 'Cargador bodega 2', model: 'Voltara DC-60', power: 60, status: 'Cargando', sessions: 141, kwh: 4620 },
    { id: 'ch3', name: 'Cargador estacionamiento', model: 'Lumen AC-22', power: 22, status: 'Falla', sessions: 37, kwh: 690 },
    { id: 'ch4', name: 'Cargador sucursal', model: 'Lumen AC-22', power: 22, status: 'Fuera de línea', sessions: 52, kwh: 910 }
  ];
  var vehicles = [
    { id: 'v1', plate: 'RT-KZ-41', model: 'Camión 3/4 eléctrico', km: 18240, eff: 0.92, status: 'En ruta' },
    { id: 'v2', plate: 'PL-TS-08', model: 'Furgón utilitario', km: 41380, eff: 0.78, status: 'Disponible' },
    { id: 'v3', plate: 'SB-HR-22', model: 'Furgón eléctrico', km: 9120, eff: 0.95, status: 'Cargando' }
  ];
  var locations = [
    { id: 'w1', code: 'A-01-02', zone: 'Secos', cap: 400, used: 340 }, { id: 'w2', code: 'A-02-01', zone: 'Líquidos', cap: 200, used: 86 },
    { id: 'w3', code: 'B-01-04', zone: 'Café y té', cap: 300, used: 210 }, { id: 'w4', code: 'C-03-02', zone: 'Desechables', cap: 250, used: 44 },
    { id: 'w5', code: 'D-01-01', zone: 'Refrigerado', cap: 60, used: 38 }
  ];
  var pickings = [
    { id: 'pk1', n: 'PK-311', order: 'Factura N° 1024', items: 2, status: 'Pendiente' },
    { id: 'pk2', n: 'PK-310', order: 'Factura N° 1023', items: 3, status: 'Completado' },
    { id: 'pk3', n: 'PK-309', order: 'Pedido B2B 5521', items: 4, status: 'En curso' }
  ];
  var boms = [
    { id: 'bm1', name: 'Kit cafetería básico', comps: 'Café 1 kg ×2, Té caja ×1, Vasos 100 u ×1', cost: 46720 },
    { id: 'bm2', name: 'Kit panadería', comps: 'Harina 25 kg ×1, Levadura ×2, Sal 1 kg ×2', cost: 23740 }
  ];
  var workOrders = [
    { id: 'wo1', n: 'OT-64', bom: 'Kit cafetería básico', qty: 40, due: '2026-11-18', status: 'En proceso' },
    { id: 'wo2', n: 'OT-63', bom: 'Kit panadería', qty: 25, due: '2026-11-12', status: 'Completada' }
  ];
  var b2bOrders = [
    { id: 'o5523', n: 5523, party: 'p2', date: '2026-11-14', items: 3, total: 1427450, status: 'Pendiente de pago' },
    { id: 'o5522', n: 5522, party: 'p5', date: '2026-11-12', items: 2, total: 2094400, status: 'Pagado' },
    { id: 'o5521', n: 5521, party: 'p2', date: '2026-11-07', items: 4, total: 987600, status: 'Despachado' }
  ];
  var payslips = [
    { id: 'ps10', period: [2026, 10], liquido: null, paid: '2026-10-31' },
    { id: 'ps9', period: [2026, 9], liquido: null, paid: '2026-09-30' },
    { id: 'ps8', period: [2026, 8], liquido: null, paid: '2026-08-31' }
  ];

  // Series para gráficos (ventas netas mensuales, millones)
  var salesSeries = [
    { m: 'Jun', v: 38.2, c: 29.1 }, { m: 'Jul', v: 41.5, c: 31.0 }, { m: 'Ago', v: 39.8, c: 30.2 },
    { m: 'Sep', v: 44.1, c: 33.9 }, { m: 'Oct', v: 51.8, c: 36.1 }, { m: 'Nov', v: 23.4, c: 16.8 }
  ];

  window.D = {
    TODAY: TODAY, company: company, parties: parties, party: party, products: products, TIPOS: TIPOS, invoices: invoices, totals: totals,
    cafs: cafs, leads: leads, quotes: quotes, purchaseOrders: purchaseOrders, fees: fees, RET: RET, employees: employees, banks: banks,
    bankMoves: bankMoves, ledgerRecords: ledgerRecords, accounts: accounts, entries: entries, assets: assets, projects: projects,
    documents: documents, users: users, auditAlerts: auditAlerts, rules: rules, notifications: notifications, chargers: chargers,
    vehicles: vehicles, locations: locations, pickings: pickings, boms: boms, workOrders: workOrders, b2bOrders: b2bOrders,
    payslips: payslips, salesSeries: salesSeries, invById: invById
  };
})();
