import { writeFile, mkdir } from 'node:fs/promises';
import { randomUUID } from 'node:crypto';
import path from 'node:path';

const API = process.env.API_URL || 'http://localhost:8092/api/v1';
const ROOT = process.env.API_ROOT || 'http://localhost:8092';
const OUT_DIR = path.resolve('api');
const GEN_DIR = path.resolve('.generated');
const PASSWORD = 'E2ePass123!';
const stamp = Date.now();

let ipCounter = 10;
function forwardedFor() {
  ipCounter += 1;
  return `10.${(ipCounter >> 16) & 255}.${(ipCounter >> 8) & 255}.${ipCounter & 255}`;
}

async function http(method, pathOrUrl, { token, json, form, root = false } = {}) {
  const url = pathOrUrl.startsWith('http') ? pathOrUrl : `${root ? ROOT : API}${pathOrUrl}`;
  const headers = { 'X-Forwarded-For': forwardedFor() };
  let body;
  if (json !== undefined) {
    headers['Content-Type'] = 'application/json';
    body = JSON.stringify(json);
  } else if (form !== undefined) {
    body = form;
  }
  if (token) headers.Authorization = `Bearer ${token}`;

  try {
    const response = await fetch(url, { method, headers, body, signal: AbortSignal.timeout(20000) });
    const text = await response.text();
    let parsed;
    try { parsed = JSON.parse(text); } catch { parsed = undefined; }
    return { status: response.status, text, json: parsed };
  } catch (error) {
    return { status: 'TIMEOUT/ERROR', text: String(error), json: undefined };
  }
}

function fail(message) {
  console.error(`❌ ${message}`);
  process.exitCode = 1;
}

/**
 * RUT chileno válido y único por corrida (módulo 11). El backend valida el
 * formato de rutEmisor al emitir; un RUT inventado como "E2E-..." hacía que
 * POST /invoices respondiera 400 y el flujo de facturación nunca emitía.
 */
function rutValido(seed) {
  const cuerpo = 70_000_000 + (seed % 9_000_000);
  let suma = 0;
  let factor = 2;
  for (const digito of String(cuerpo).split('').reverse()) {
    suma += Number(digito) * factor;
    factor = factor === 7 ? 2 : factor + 1;
  }
  const resto = 11 - (suma % 11);
  const dv = resto === 11 ? '0' : resto === 10 ? 'K' : String(resto);
  return `${cuerpo}-${dv}`;
}

async function registerCompany() {
  const adminEmail = `qa-admin-${stamp}@example.com`;
  const rut = rutValido(stamp);
  const response = await http('POST', '/auth/register', {
    json: {
      rut,
      razonSocial: `QA E2E ${stamp}`,
      adminEmail,
      adminPassword: PASSWORD,
    },
  });
  if (response.status !== 200 || !response.json?.token) {
    throw new Error(`Registro falló (${response.status}): ${response.text}`);
  }
  return {
    token: response.json.token,
    companyId: response.json.companyId,
    userId: response.json.userId,
    email: adminEmail,
    role: 'ADMIN',
    rut,
  };
}

async function login(email) {
  const response = await http('POST', '/auth/login', { json: { email, password: PASSWORD } });
  if (response.status !== 200 || !response.json?.token) {
    return { ok: false, status: response.status, text: response.text };
  }
  return { ok: true, token: response.json.token, userId: response.json.userId };
}

async function createRoleUsers(admin) {
  const created = {};
  const failures = [];
  const roles = ['ACCOUNTANT', 'TREASURER', 'SALES', 'OPERATIONS', 'HR', 'AUDITOR', 'USER', 'B2B_CLIENT'];
  for (const role of roles) {
    const email = `qa-${role.toLowerCase()}-${stamp}@example.com`;
    const response = await http('POST', '/sso/users', {
      token: admin.token,
      json: { email, fullName: `QA ${role}`, password: PASSWORD, role },
    });
    if (response.status !== 200) {
      failures.push({ role, status: response.status, text: response.text });
      continue;
    }
    const auth = await login(email);
    if (!auth.ok) {
      failures.push({ role, status: auth.status, text: `login: ${auth.text}`, email });
      continue;
    }
    created[role] = { email, token: auth.token, userId: auth.userId };
  }

  return { created, failures };
}

const ALL_ROLES = ['ADMIN', 'ACCOUNTANT', 'TREASURER', 'SALES', 'OPERATIONS', 'HR', 'AUDITOR', 'USER', 'B2B_CLIENT'];

const endpoints = [
  {
    key: 'invoices',
    label: 'GET /invoices/{id} (UUID aleatorio)',
    method: 'GET',
    path: `/invoices/${randomUUID()}`,
    allow: ['ADMIN', 'SALES', 'ACCOUNTANT'],
    allowMeansNot403: true,
  },
  { key: 'sii/caf', label: 'GET /sii/caf', method: 'GET', path: '/sii/caf', allow: ['ADMIN', 'ACCOUNTANT'] },
  { key: 'sii/status', label: 'GET /sii/status', method: 'GET', path: '/sii/status', allow: ['ADMIN', 'ACCOUNTANT'] },
  { key: 'accounting', label: 'GET /accounting/entries', method: 'GET', path: '/accounting/entries', allow: ['ADMIN', 'ACCOUNTANT'] },
  {
    key: 'payroll',
    label: 'GET /payroll/employee/payslips',
    method: 'GET',
    path: '/payroll/employee/payslips',
    allow: ['ADMIN', 'HR', 'USER'],
    allowMeansNot403: true,
  },
  { key: 'actuator', label: 'GET /actuator/metrics', method: 'GET', path: '/actuator/metrics', root: true, allow: ['ADMIN'] },
  {
    key: 'ai-assistant',
    label: 'POST /ai/conversations',
    method: 'POST',
    path: '/ai/conversations',
    allow: ['ADMIN', 'ACCOUNTANT', 'SALES', 'TREASURER', 'AUDITOR'],
  },
  {
    key: 'notifications',
    label: 'GET /notifications/preferences',
    method: 'GET',
    path: '/notifications/preferences',
    allow: ALL_ROLES,
    allowMeansNot403: true,
  },
  {
    key: 'b2b',
    label: 'GET /b2b/orders/my-orders',
    method: 'GET',
    path: '/b2b/orders/my-orders',
    allow: ['ADMIN', 'B2B_CLIENT'],
    allowMeansNot403: true,
  },
];

function isAllowedResponse(endpoint, status) {
  if (endpoint.allowMeansNot403) return status !== 401 && status !== 403;
  return status === 200;
}

function cell(endpoint, role, result) {
  if (result === undefined) return '—';
  if (result.status === 'N/A') return 'N/A';
  const expected = endpoint.allow.includes(role) ? 'ALLOW' : 'DENY';
  const ok = expected === 'ALLOW' ? isAllowedResponse(endpoint, result.status) : result.status === 403;
  const icon = ok ? '✅' : '❌';
  return `${icon} ${result.status}`;
}


async function checkB2bIsolation(admin, created) {
  const first = created.B2B_CLIENT;
  const email2 = `qa-b2b2-${stamp}@example.com`;
  if (!first) return { ok: false, detail: 'No se creó el usuario B2B_CLIENT' };

  const create2 = await http('POST', '/sso/users', {
    token: admin.token,
    json: { email: email2, fullName: 'QA B2B 2', password: PASSWORD, role: 'B2B_CLIENT' },
  });
  if (create2.status !== 200) return { ok: false, detail: `alta B2B 2 → ${create2.status}` };
  const login2 = await login(email2);
  if (!login2.ok) return { ok: false, detail: `login B2B 2 → ${login2.status}` };

  const item = { productName: 'Producto QA', quantity: 1, unitPrice: 1000 };
  const order1 = await http('POST', '/b2b/orders', { token: first.token, json: { items: [item] } });
  const order2 = await http('POST', '/b2b/orders', { token: login2.token, json: { items: [item] } });
  if (order1.status !== 201 || order2.status !== 201) {
    return { ok: false, detail: `crear órdenes → ${order1.status}/${order2.status}` };
  }
  const list1 = await http('GET', '/b2b/orders/my-orders', { token: first.token });
  const list2 = await http('GET', '/b2b/orders/my-orders', { token: login2.token });
  const ids1 = (list1.json ?? []).map((o) => o.id);
  const ids2 = (list2.json ?? []).map((o) => o.id);
  const ok = ids1.length === 1 && ids1[0] === order1.json.id
    && ids2.length === 1 && ids2[0] === order2.json.id;
  return {
    ok,
    orderA: order1.json.id,
    orderB: order2.json.id,
    seenByA: ids1,
    seenByB: ids2,
  };
}

async function main() {
  await mkdir(OUT_DIR, { recursive: true });
  await mkdir(GEN_DIR, { recursive: true });

  console.log(`➡️  Backend: ${ROOT}`);
  const admin = await registerCompany();
  console.log(`✅ Empresa/admin creados: ${admin.email} (${admin.companyId})`);

  const { created, failures } = await createRoleUsers(admin);
  const tokens = { ADMIN: admin.token, ...Object.fromEntries(Object.entries(created).map(([r, v]) => [r, v.token])) };

  console.log(`✅ Roles creados: ${Object.keys(created).join(', ')}`);
  if (failures.length) console.log(`⚠️ Fallos creando roles: ${JSON.stringify(failures)}`);

  const b2bIsolation = await checkB2bIsolation(admin, created);

  const endpointResults = [];
  for (const endpoint of endpoints) {
    const results = {};
    for (const role of ALL_ROLES) {
      const token = tokens[role];
      if (!token) {
        results[role] = undefined;
        continue;
      }
      const response = await http(endpoint.method, endpoint.path, { token, root: endpoint.root });
      results[role] = { status: response.status, body: response.text.slice(0, 180) };
    }
    endpointResults.push({ endpoint, results });
  }

  const roles = ALL_ROLES;
  const rows = [];
  const discrepancies = [];
  for (const entry of endpointResults) {
    const cells = [];
    for (const role of roles) {
      const result = entry.results[role];
      cells.push(cell(entry.endpoint, role, result));
      if (result && result.status !== 'N/A') {
        const expectedAllow = entry.endpoint.allow.includes(role);
        const ok = expectedAllow ? isAllowedResponse(entry.endpoint, result.status) : result.status === 403;
        if (!ok) {
          discrepancies.push({
            endpoint: entry.endpoint.key,
            role,
            expected: expectedAllow ? 'ALLOW' : 'DENY',
            obtained: result.status,
            body: result.body,
          });
        }
      }
    }
    rows.push(`| ${entry.endpoint.label} | ${cells.join(' | ')} |`);
  }

  const setupLines = [
    `- Empresa E2E: \`QA E2E ${stamp}\` / RUT \`E2E-${stamp}\``,
    `- ADMIN: \`${admin.email}\``,
    ...Object.entries(created).map(([role, v]) => `- ${role}: \`${v.email}\``),
    `- B2B_CLIENT: \`${created.B2B_CLIENT?.email ?? 'no creado'}\``,
  ];

  const report = `# Matriz de permisos por API — SII-ERP

Generado: ${new Date().toISOString()}
Backend: ${ROOT}
Empresa: ${admin.companyId}

## Setup

${setupLines.join('\n')}

## Matriz rol × endpoint

Celdas: ✅ status = coincide con lo esperado; ❌ status = discrepancia; — = rol no creado.

| Endpoint | ${roles.join(' | ')} |
| --- | ${roles.map(() => '---').join(' | ')} |
${rows.join('\n')}

## Resultado

${discrepancies.length === 0 ? 'Sin discrepancias en los endpoints probados.' : discrepancies.map((d) => `- **${d.endpoint}** / ${d.role}: esperado ${d.expected}, obtenido ${d.obtained} — ${d.body ?? ''}`).join('\n')}

## Notas de la matriz

- **B2B_CLIENT** ya existe en UserRole y se crea por POST /sso/users; el endpoint B2B se prueba con su token.
- **payroll / USER**: el endpoint permite USER y ahora resuelve el empleado desde el usuario autenticado; solo devuelve sus liquidaciones (test backend PayrollPayslipIsolationTest). La fila USER de MAPA-FUNCIONAL.md estaba desactualizada.
- **actuator / ADMIN**: metrics está expuesto en application.properties y protegido por ADMIN; responde 200.
- **invoices / ALLOW (400)**: UUID inexistente; lo relevante es que no sea 403.
- **B2B aislamiento**: ${b2bIsolation.ok ? 'OK: cada cliente ve solo su orden' : 'FALLO: ' + JSON.stringify(b2bIsolation)} (A=${b2bIsolation.orderA ?? '-'}, B=${b2bIsolation.orderB ?? '-'}).

## B2B_CLIENT

UserRole incluye B2B_CLIENT. CustomerOrderService filtra por customerId y el test E2E crea dos clientes con una orden cada uno; cada GET /b2b/orders/my-orders devuelve solo la propia.
`;

  await writeFile(path.join(OUT_DIR, 'report.md'), report);
  await writeFile(path.join(OUT_DIR, 'results.json'), JSON.stringify({ admin, created, failures, b2bIsolation, endpointResults, discrepancies }, null, 2));
  await writeFile(path.join(GEN_DIR, 'users.json'), JSON.stringify({
    admin: { email: admin.email, password: PASSWORD, role: 'ADMIN' },
    sales: { email: created.SALES?.email, password: PASSWORD, role: 'SALES' },
    accountant: { email: created.ACCOUNTANT?.email, password: PASSWORD, role: 'ACCOUNTANT' },
    companyId: admin.companyId,
    companyRut: admin.rut,
  }, null, 2));

  console.log(`\n${report}`);
  if (discrepancies.length || !b2bIsolation.ok) {
    fail(`${discrepancies.length} discrepancia(s), aislamiento B2B=${b2bIsolation.ok}; ver api/report.md`);
  } else {
    console.log('✅ Matriz sin discrepancias');
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
