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

async function registerCompany() {
  const adminEmail = `qa-admin-${stamp}@example.com`;
  const response = await http('POST', '/auth/register', {
    json: {
      rut: `E2E-${stamp}`,
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
  const roles = ['ACCOUNTANT', 'TREASURER', 'SALES', 'OPERATIONS', 'HR', 'AUDITOR', 'USER'];
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

  // B2B_CLIENT no existe en UserRole (backend): se intenta y se documenta.
  const b2bEmail = `qa-b2b-${stamp}@example.com`;
  const b2bAttempt = await http('POST', '/sso/users', {
    token: admin.token,
    json: { email: b2bEmail, fullName: 'QA B2B', password: PASSWORD, role: 'B2B_CLIENT' },
  });

  return { created, failures, b2bAttempt, b2bEmail };
}

const ALL_ROLES = ['ADMIN', 'ACCOUNTANT', 'TREASURER', 'SALES', 'OPERATIONS', 'HR', 'AUDITOR', 'USER'];

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
    allow: ['ADMIN', 'HR'],
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

async function main() {
  await mkdir(OUT_DIR, { recursive: true });
  await mkdir(GEN_DIR, { recursive: true });

  console.log(`➡️  Backend: ${ROOT}`);
  const admin = await registerCompany();
  console.log(`✅ Empresa/admin creados: ${admin.email} (${admin.companyId})`);

  const { created, failures, b2bAttempt, b2bEmail } = await createRoleUsers(admin);
  const tokens = { ADMIN: admin.token, ...Object.fromEntries(Object.entries(created).map(([r, v]) => [r, v.token])) };

  console.log(`✅ Roles creados: ${Object.keys(created).join(', ')}`);
  if (failures.length) console.log(`⚠️ Fallos creando roles: ${JSON.stringify(failures)}`);
  console.log(`ℹ️ B2B_CLIENT: intento de alta → HTTP ${b2bAttempt.status}`);

  const endpointResults = [];
  for (const endpoint of endpoints) {
    const results = {};
    for (const role of [...ALL_ROLES, 'B2B_CLIENT']) {
      const token = tokens[role];
      if (role === 'B2B_CLIENT' && !token) {
        results[role] = { status: 'N/A', note: 'rol no representable en UserRole' };
        continue;
      }
      if (!token) {
        results[role] = undefined;
        continue;
      }
      const response = await http(endpoint.method, endpoint.path, { token, root: endpoint.root });
      results[role] = { status: response.status, body: response.text.slice(0, 180) };
    }
    endpointResults.push({ endpoint, results });
  }

  const roles = [...ALL_ROLES, 'B2B_CLIENT'];
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
    `- B2B_CLIENT: \`${b2bEmail}\` — alta vía \`POST /sso/users\` → HTTP ${b2bAttempt.status} (rol inexistente en el enum)`,
  ];

  const report = `# Matriz de permisos por API — SII-ERP

Generado: ${new Date().toISOString()}
Backend: ${ROOT}
Empresa: ${admin.companyId}

## Setup

${setupLines.join('\n')}

## Matriz rol × endpoint

Celdas: \`✅ status\` = coincide con lo esperado; \`❌ status\` = discrepancia; \`—\` = rol no creado; \`N/A\` = no representable.

| Endpoint | ${roles.join(' | ')} |
| --- | ${roles.map(() => '---').join(' | ')} |
${rows.join('\n')}

## Discrepancias

${discrepancies.length === 0 ? 'Sin discrepancias en los endpoints probados.' : discrepancies.map((d) => `- **${d.endpoint}** / ${d.role}: esperado ${d.expected}, obtenido ${d.obtained} — \`${d.body ?? ''}\``).join('\n')}

## Detalle de discrepancias

- **payroll / USER** — el método permite explícitamente al rol USER: \`SII-ERP-AI-BACK/src/main/java/com/casrusil/siierpai/modules/payroll/infrastructure/web/PayrollController.java:59\` (\`@PreAuthorize("hasAnyRole('ADMIN','HR','USER')")\`). La matriz de \`MAPA-FUNCIONAL.md\` dice que USER no tiene áreas propias. No se corrige: es código de E/C.
- **actuator / ADMIN (500)** — \`SecurityConfig.java:66\` protege \`/actuator/**\` con ADMIN, pero \`application.properties:120\` solo expone \`health,info\`; \`/actuator/metrics\` no está mapeado y el \`GlobalExceptionHandler\` lo convierte en 500 (\`No static resource actuator/metrics\`). No se tocó el backend.
- **invoices / ALLOW (400)** — el UUID aleatorio no existe; lo relevante es que no sea 403 (permiso concedido).

## B2B_CLIENT

\`UserRole\` no define \`B2B_CLIENT\` (\`SII-ERP-AI-BACK/src/main/java/com/casrusil/siierpai/modules/sso/domain/model/UserRole.java:30\`), aunque \`CustomerOrderController\` lo exige (\`.../b2b/infrastructure/web/CustomerOrderController.java:30\`). No es posible crear un usuario con ese rol por la API; la fila B2B_CLIENT queda sin token y el endpoint solo se valida con ADMIN.
`;

  await writeFile(path.join(OUT_DIR, 'report.md'), report);
  await writeFile(path.join(OUT_DIR, 'results.json'), JSON.stringify({ admin, created, failures, b2bAttempt, endpointResults, discrepancies }, null, 2));
  await writeFile(path.join(GEN_DIR, 'users.json'), JSON.stringify({
    admin: { email: admin.email, password: PASSWORD, role: 'ADMIN' },
    sales: { email: created.SALES?.email, password: PASSWORD, role: 'SALES' },
    accountant: { email: created.ACCOUNTANT?.email, password: PASSWORD, role: 'ACCOUNTANT' },
    companyId: admin.companyId,
  }, null, 2));

  console.log(`\n${report}`);
  if (discrepancies.length) {
    fail(`${discrepancies.length} discrepancia(s); ver api/report.md`);
  } else {
    console.log('✅ Matriz sin discrepancias');
  }
}

main().catch((error) => {
  console.error(error);
  process.exit(1);
});
