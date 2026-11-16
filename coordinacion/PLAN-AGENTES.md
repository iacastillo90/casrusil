# Plan de trabajo multi-agente — SII-ERP AI (Casrusil)

Orquestador: Claude (sesión de rediseño). Rama base de todo el trabajo: **`integracion`**. Nadie toca `main`.

## Reglas comunes (aplican a TODOS los agentes)

1. **Workspace propio**: cada agente trabaja en su propio clon o `git worktree` (nunca en la carpeta de otro agente).
   ```bash
   git fetch origin && git worktree add ../casrusil-<agente> -b <rama> origin/integracion
   ```
2. **Rama propia** creada desde `origin/integracion`. Prohibido: `push` a `main` o a `integracion`, `push --force` a ramas ajenas, `rebase` de ramas ajenas.
3. **Propiedad de archivos**: solo modificas los paths listados en tu prompt. Si necesitas cambiar un archivo de otro agente, NO lo edites: anótalo en la sección "Necesito de otros" de tu informe.
4. **Commits** pequeños y en español con prefijo convencional (`feat(invoicing): …`, `fix(front): …`). Push al terminar cada bloque lógico (`git push -u origin <rama>`).
5. **Gates obligatorios antes de cada push**:
   - Back: `./gradlew compileJava` y `./gradlew bootJar` (y `./gradlew test --tests '<lo que tocaste>'` si existe test).
   - Front: `npx tsc --noEmit` y `npm run lint` (sin errores nuevos).
6. **Entorno local**: backend en puerto **8092** (8080/8081 los usa OTRO proyecto — **NO matarlos**). Postgres 15 en `55432`, DB `siierp_ddl`. Redis puede estar caído.
7. **Fuente de verdad**: `Doc/DESIGN.md`, `Doc/RIESGOS.md`, `Doc/UI-DESIGN.md`, `Doc/MAPA-FUNCIONAL.md` (locales) + `coordinacion/rediseno/*` y `prototipo/` (en esta rama). Si el código contradice la doc, repórtalo con `archivo:línea`; no adivines. Nada inventado: lo nuevo va marcado "Propuesta nueva".
8. **Nunca declares algo hecho sin verificarlo** (gate verde o prueba runtime con `curl`).
9. **Al terminar**: abre PR **hacia `integracion`** (nunca hacia `main`) y pega el informe (plantilla abajo) como descripción del PR y en tu respuesta final.
10. Si te quedas sin presupuesto: commit + push de lo que compile, y en el informe marca exactamente qué quedó a medias.

## Olas y orden de merge

| Ola | Agente | Rama | Depende de |
|---|---|---|---|
| 1 | **A0 — Backend contrato** (agente que ya implementó los 14 endpoints) | `feat/backend-contrato` | — |
| 1 | **B — Front fundaciones** | `feat/front-fundaciones` | — |
| 2 | **A1 — Backend folios, CAF y seguridad** | `feat/backend-folios-seguridad` | A0 mergeado |
| 2 | **C — Front pantallas P0** | `feat/front-pantallas-p0` | A0 + B mergeados |

Ola 1 corre en paralelo (no comparten archivos). El orquestador revisa y mergea A0 → B en `integracion`; recién ahí arranca la ola 2 (A1 y C en paralelo, tampoco comparten archivos).

## Mapa de propiedad de archivos

| Path | Dueño |
|---|---|
| `SII-ERP-AI-BACK/**/modules/invoicing/infrastructure/adapter/in/rest/**`, `modules/accounting/**/rest/**` (endpoints nuevos), `dashboard`, `energy` controllers nuevos, `application-dev.properties` | A0 |
| `SII-ERP-AI-BACK/**/modules/invoicing/{domain,application,infrastructure/persistence}/**`, `modules/integration_sii/**`, `shared/**/security/**`, `schema.sql`, `application.properties`, `application-prod.properties` | A1 (ola 2) |
| `SII-ERP-AI-FRONT/src/app/globals.css`, `tailwind.config.*`, `src/components/{ui,layout,shared}/**`, `src/lib/axios.ts`, `src/config/routes.ts`, `src/stores/**`, `.env.example` | B |
| `SII-ERP-AI-FRONT/src/app/(dashboard)/{invoices,sii}/**`, `src/features/{invoicing,sii}/**` | C (ola 2) |

A0 solo mantiene `schema.sql`/servicios de invoicing tal como los dejó en su trabajo previo; desde la ola 2 son de A1.

## Plantilla de informe (obligatoria)

```markdown
## Informe — <Agente> — rama <rama>
**Commits**: <hash corto — mensaje> (uno por línea)
**PR**: <url> → integracion
### Hecho (verificado)
- <cambio> — archivo(s) — cómo se verificó (gate / curl / screenshot)
### Contratos API expuestos o consumidos
| Método | Ruta | Request | Response | Rol(es) |
### Gates
- compileJava / bootJar / tsc / lint / tests: ✅/❌ (salida resumida)
### Contradicciones doc ↔ código
- archivo:línea — qué dice la doc — qué hace el código
### Pendiente / a medias
- <ítem> — motivo — siguiente paso concreto
### Necesito de otros agentes
- <agente> — <archivo> — <cambio requerido>
### Riesgos/IDs cerrados
- R-xx / B-xx / PEN-xx: cerrado | parcial | no tocado
```
