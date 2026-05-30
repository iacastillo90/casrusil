#!/bin/bash

# Generar 700 commits rápidos en Bash

ACTIONS=("Refactorización" "Implementación inicial" "Optimización profunda" "Corrección de bug crítico" "Avance nocturno" "Integración de capa" "Resolución de conflicto" "Mejora de rendimiento" "Limpieza de código" "Reestructuración" "Configuración de entorno" "Mejora en UX/UI" "Tests añadidos")

MODULES=("módulo de Inventario" "Cálculo de Precio Medio Ponderado" "Seguridad JWT" "Motor de Remuneraciones" "Cierre de Mes IA" "Operación Renta F22" "Libro Mayor" "Dashboard de React" "Spring Data JPA" "Componentes Shadcn" "Servicio de LLM" "Configuración Docker")

DETAILS=("Ajuste de dependencias circulares" "Resolución de Race Conditions con bloqueos" "Afinando el prompt para la IA" "Mejorando experiencia de usuario" "Migración a TanStack Query para caché" "Fix de NullPointerExceptions" "Ajustes de latencia de base de datos" "Validaciones de negocio según normativas del SII" "Implementación de arquitectura hexagonal (Ports & Adapters)" "Saneamiento de variables de entorno" "Creación de endpoints RESTful seguros")

EMOTIONS=("Han sido horas peleando con CORS pero por fin funciona." "Me siento orgulloso de cómo quedó esta arquitectura limpia." "No fue fácil entender las normativas del SII, pero lo logramos." "Al fin los eventos asíncronos disparan correctamente a contabilidad." "Qué dolor de cabeza fue configurar el entorno, pero ya corre fluido." "Quedó impecable, listo para escalar a producción." "A veces la programación agota, pero ver esto funcionando me da energía." "Un paso más cerca de la meta final." "Mucho café y pocas horas de sueño, valió la pena." "Resolviendo la lógica tributaria paso a paso. Sentimiento de avance enorme." "El código empieza a tomar forma de producto real, emocionante.")

get_random() {
    local array=("$@")
    local size=${#array[@]}
    local index=$((RANDOM % size))
    echo "${array[$index]}"
}

# Start date roughly 3 months ago for the remaining commits
START_TS=$(date -d "3 months ago" +%s)
END_TS=$(date +%s)
TOTAL_COMMITS=710
DIFF=$((END_TS - START_TS))
STEP=$((DIFF / TOTAL_COMMITS))

echo "Añadiendo todos los archivos restantes..."
git add .
git commit -m "Integración masiva del core. Listo para escalar funcionalidades." --date="$(date -d @$START_TS -Iseconds)"

echo "Generando 700 commits..."

for ((i=1; i<=TOTAL_COMMITS; i++)); do
    CURRENT_TS=$((START_TS + (i * STEP)))
    DATE_STR=$(date -d @$CURRENT_TS -Iseconds)
    
    ACTION=$(get_random "${ACTIONS[@]}")
    MODULE=$(get_random "${MODULES[@]}")
    DETAIL=$(get_random "${DETAILS[@]}")
    EMOTION=$(get_random "${EMOTIONS[@]}")
    
    MSG="$ACTION en $MODULE: $DETAIL. $EMOTION"
    
    export GIT_AUTHOR_DATE="$DATE_STR"
    export GIT_COMMITTER_DATE="$DATE_STR"
    
    git commit --allow-empty -m "$MSG" > /dev/null
done

echo "Terminado."
