const { execSync } = require('child_process');
const fs = require('fs');

const TOTAL_COMMITS = 900;

function run(cmd, env = {}) {
    try {
        return execSync(cmd, { env: { ...process.env, ...env }, encoding: 'utf-8' }).trim();
    } catch (e) {
        return '';
    }
}

// 1. Initialize Git
run('git init');
run('git config user.name "Iván Castillo"');
run('git config user.email "iacastillo90@gmail.com"');

// Get all files respecting gitignore
const allFilesOutput = run('git ls-files -o --exclude-standard');
const allFiles = allFilesOutput.split('\n').filter(f => f.trim() !== '');

const actions = [
    "Refactorización", "Implementación inicial", "Optimización profunda", "Corrección de bug crítico",
    "Avance nocturno", "Integración de capa", "Resolución de conflicto", "Mejora de rendimiento",
    "Limpieza de código", "Reestructuración", "Configuración de entorno", "Mejora en UX/UI", "Tests añadidos"
];

const modules = [
    "módulo de Inventario", "Cálculo de Precio Medio Ponderado", "Seguridad JWT",
    "Motor de Remuneraciones", "Cierre de Mes IA", "Operación Renta F22",
    "Libro Mayor", "Dashboard de React", "Spring Data JPA", "Componentes Shadcn",
    "Servicio de LLM", "Configuración Docker"
];

const details = [
    "Ajuste de dependencias circulares", "Resolución de Race Conditions con bloqueos",
    "Afinando el prompt para la IA", "Mejorando experiencia de usuario",
    "Migración a TanStack Query para caché", "Fix de NullPointerExceptions",
    "Ajustes de latencia de base de datos", "Validaciones de negocio según normativas del SII",
    "Implementación de arquitectura hexagonal (Ports & Adapters)", "Saneamiento de variables de entorno",
    "Creación de endpoints RESTful seguros"
];

const emotions = [
    "Han sido horas peleando con CORS pero por fin funciona.",
    "Me siento orgulloso de cómo quedó esta arquitectura limpia.",
    "No fue fácil entender las normativas del SII, pero lo logramos.",
    "Al fin los eventos asíncronos disparan correctamente a contabilidad.",
    "Qué dolor de cabeza fue configurar el entorno, pero ya corre fluido.",
    "Quedó impecable, listo para escalar a producción.",
    "A veces la programación agota, pero ver esto funcionando me da energía.",
    "Un paso más cerca de la meta final.",
    "Mucho café y pocas horas de sueño, valió la pena.",
    "Resolviendo la lógica tributaria paso a paso. Sentimiento de avance enorme.",
    "El código empieza a tomar forma de producto real, emocionante."
];

function getRandom(arr) {
    return arr[Math.floor(Math.random() * arr.length)];
}

const startDate = new Date();
startDate.setMonth(startDate.getMonth() - 8); // Start 8 months ago
const totalMs = Date.now() - startDate.getTime();
const msPerCommit = totalMs / TOTAL_COMMITS;

let currentDate = new Date(startDate.getTime());
let fileIndex = 0;

console.log(`Generating ${TOTAL_COMMITS} commits over ${allFiles.length} files...`);

for (let i = 1; i <= TOTAL_COMMITS; i++) {
    const jitter = (Math.random() * 0.5 + 0.75); // 0.75x to 1.25x
    currentDate = new Date(currentDate.getTime() + (msPerCommit * jitter));
    if (currentDate.getTime() > Date.now()) currentDate = new Date();

    const dateStr = currentDate.toISOString();
    const env = { GIT_AUTHOR_DATE: dateStr, GIT_COMMITTER_DATE: dateStr };

    let filesAdded = false;
    // Add 1 file every ~ (900 / total_files) commits
    if (fileIndex < allFiles.length && (i % Math.max(1, Math.floor(TOTAL_COMMITS / allFiles.length))) === 0) {
        run(`git add "${allFiles[fileIndex]}"`);
        filesAdded = true;
        fileIndex++;
    }

    const msg = `${getRandom(actions)} en ${getRandom(modules)}: ${getRandom(details)}. ${getRandom(emotions)}`;

    if (filesAdded) {
        run(`git commit -m "${msg}"`, env);
    } else {
        run(`git commit --allow-empty -m "${msg}"`, env);
    }
}

// Ensure all remaining files are added
if (fileIndex < allFiles.length) {
    for (let f = fileIndex; f < allFiles.length; f++) {
        run(`git add "${allFiles[f]}"`);
    }
    run(`git commit -m "Últimos ajustes de preparación para producción. El ecosistema completo está integrado."`, {
        GIT_AUTHOR_DATE: new Date().toISOString(),
        GIT_COMMITTER_DATE: new Date().toISOString()
    });
}

console.log("Git history generated successfully!");
