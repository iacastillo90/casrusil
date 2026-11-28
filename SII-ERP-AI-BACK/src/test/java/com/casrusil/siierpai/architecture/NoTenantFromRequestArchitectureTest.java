package com.casrusil.siierpai.architecture;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regla de arquitectura de la P0 multiempresa: ningún controller puede recibir la
 * empresa desde la petición. Falla si algún handler declara un parámetro, header,
 * cookie, path variable o campo de body llamado companyId, tenantId, X-Company-Id o
 * X-Tenant-ID (en cualquier variante de mayúsculas, guiones o guiones bajos).
 * La empresa se obtiene siempre de CompanyContext (JWT).
 */
class NoTenantFromRequestArchitectureTest {

    private static final String BASE_PACKAGE = "com.casrusil.siierpai";
    private static final Set<String> FORBIDDEN = Set.of("companyid", "tenantid", "xcompanyid", "xtenantid");

    @Test
    void ningunControllerRecibeLaEmpresaDesdeLaPeticion() throws Exception {
        List<String> violations = new ArrayList<>();
        List<Class<?>> controllers = controllers();
        assertFalse(controllers.isEmpty(), "El escaneo no encontró controllers; revisa el paquete base");

        for (Class<?> controller : controllers) {
            for (Method method : controller.getDeclaredMethods()) {
                for (Parameter parameter : method.getParameters()) {
                    String where = controller.getSimpleName() + "#" + method.getName() + "(" + parameter.getName() + ")";
                    for (String name : declaredNames(parameter)) {
                        if (isForbidden(name)) {
                            violations.add(where + " recibe '" + name + "' desde la petición");
                        }
                    }
                    if (parameter.isAnnotationPresent(RequestBody.class) && parameter.getType().isRecord()) {
                        for (RecordComponent component : parameter.getType().getRecordComponents()) {
                            if (isForbidden(component.getName())) {
                                violations.add(where + " acepta el campo '" + component.getName() + "' en el body");
                            }
                        }
                    }
                }
            }
        }

        assertTrue(violations.isEmpty(),
                "La empresa debe salir de CompanyContext (JWT), nunca de la petición:\n" + String.join("\n", violations));
    }

    @Test
    void laReglaDetectaUnaViolacion() throws Exception {
        Method bad = BadController.class.getDeclaredMethod("leak", String.class, String.class);
        long found = 0;
        for (Parameter parameter : bad.getParameters()) {
            found += declaredNames(parameter).stream().filter(NoTenantFromRequestArchitectureTest::isForbidden).count();
        }
        assertTrue(found == 2, "La regla debe detectar header y parámetro prohibidos");
    }

    private static List<Class<?>> controllers() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
        List<Class<?>> result = new ArrayList<>();
        for (BeanDefinition candidate : scanner.findCandidateComponents(BASE_PACKAGE)) {
            result.add(Class.forName(candidate.getBeanClassName()));
        }
        return result;
    }

    /** Nombres con los que el parámetro se enlaza a la petición (anotación o nombre del parámetro). */
    private static List<String> declaredNames(Parameter parameter) {
        List<String> names = new ArrayList<>();
        RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
        if (requestParam != null) {
            names.add(first(requestParam.value(), requestParam.name(), parameter.getName()));
        }
        RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
        if (requestHeader != null) {
            names.add(first(requestHeader.value(), requestHeader.name(), parameter.getName()));
        }
        PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
        if (pathVariable != null) {
            names.add(first(pathVariable.value(), pathVariable.name(), parameter.getName()));
        }
        CookieValue cookieValue = parameter.getAnnotation(CookieValue.class);
        if (cookieValue != null) {
            names.add(first(cookieValue.value(), cookieValue.name(), parameter.getName()));
        }
        MatrixVariable matrixVariable = parameter.getAnnotation(MatrixVariable.class);
        if (matrixVariable != null) {
            names.add(first(matrixVariable.value(), matrixVariable.name(), parameter.getName()));
        }
        RequestPart requestPart = parameter.getAnnotation(RequestPart.class);
        if (requestPart != null) {
            names.add(first(requestPart.value(), requestPart.name(), parameter.getName()));
        }
        // Parámetros simples sin anotación también se enlazan por nombre (?companyId=...).
        if (names.isEmpty() && isSimple(parameter.getType())) {
            names.add(parameter.getName());
        }
        return names;
    }

    private static boolean isSimple(Class<?> type) {
        return type.isPrimitive() || type == String.class || type == java.util.UUID.class
                || Number.class.isAssignableFrom(type);
    }

    private static String first(String... candidates) {
        for (String candidate : candidates) {
            if (candidate != null && !candidate.isBlank()) {
                return candidate;
            }
        }
        return "";
    }

    static boolean isForbidden(String name) {
        String normalized = name.toLowerCase(Locale.ROOT).replace("-", "").replace("_", "");
        return FORBIDDEN.contains(normalized);
    }

    /** Controller de ejemplo (no escaneado: está fuera de un @Controller real) para probar la regla. */
    @SuppressWarnings("unused")
    static class BadController {
        void leak(@RequestHeader("X-Company-Id") String header, @RequestParam("tenantId") String param) {
        }
    }
}
