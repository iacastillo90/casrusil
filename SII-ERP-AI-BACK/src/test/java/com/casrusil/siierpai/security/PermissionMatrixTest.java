package com.casrusil.siierpai.security;

import com.casrusil.siierpai.modules.sso.domain.model.UserRole;
import com.casrusil.siierpai.modules.sso.infrastructure.security.JwtTokenProvider;
import com.casrusil.siierpai.shared.domain.valueobject.CompanyId;
import com.casrusil.siierpai.shared.domain.valueobject.UserId;
import com.casrusil.siierpai.shared.test.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;


import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;

/**
 * Matriz de permisos rol × endpoint.
 *
 * <p>Recorre todos los handlers REST del contexto real, deriva los roles del
 * {@code @PreAuthorize} efectivo (método sobre clase) y ejecuta una petición
 * MockMvc por rol. También verifica que ningún endpoint quede público fuera de
 * login, register, logout, health e info.</p>
 *
 * <p>Los endpoints con cuerpo pueden responder 400/415 antes de que Spring
 * evalúe el método; para esos casos la prueba exige que un rol denegado nunca
 * reciba 2xx y la matriz estática exige que exista una regla de autorización.
 * Los endpoints sin cuerpo se verifican con 403 estricto.</p>
 */
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class PermissionMatrixTest extends BaseIntegrationTest {

    private static final List<String> ROLES = List.of(
            "ADMIN", "ACCOUNTANT", "TREASURER", "HR", "SALES",
            "OPERATIONS", "AUDITOR", "USER");

    private static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/logout",
            // R11: el invitado acepta su invitación sin sesión (token de un solo uso).
            "/api/v1/auth/invitations/validate",
            "/api/v1/auth/invitations/accept");

    private static final String AUTHENTICATED = "__AUTHENTICATED__";
    private static final String PUBLIC = "__PUBLIC__";
    private static final String USER_ID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";
    private static final String COMPANY_ID = "00000000-0000-0000-0000-000000000001";

    private static final Pattern ANY_ROLE = Pattern.compile("hasAnyRole\\(([^)]*)\\)");
    private static final Pattern ONE_ROLE = Pattern.compile("hasRole\\('([^']+)'\\)");
    private static final Pattern QUOTED = Pattern.compile("'([^']+)'");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    @Qualifier("requestMappingHandlerMapping")
    private RequestMappingHandlerMapping handlerMapping;

    private List<Endpoint> endpoints;

    static Stream<String> roles() {
        return ROLES.stream();
    }

    @Test
    void inventoryHasAuthorizationRuleForEveryEndpoint() {
        List<String> withoutRule = new ArrayList<>();
        List<String> unknownPublic = new ArrayList<>();

        for (Endpoint endpoint : endpoints()) {
            if (endpoint.isPublic()) {
                if (!PUBLIC_PATHS.contains(endpoint.path())) {
                    unknownPublic.add(endpoint.method() + " " + endpoint.path());
                }
                continue;
            }
            if (endpoint.path().equals("/api/v1/auth/me")) {
                continue; // SecurityConfig exige autenticación sin rol; se verifica en unauthenticated.
            }
            String expression = endpoint.preAuthorize();
            if (expression == null || expression.isBlank()) {
                withoutRule.add(endpoint.method() + " " + endpoint.path() + " (" + endpoint.location() + ")");
            }
        }

        assertTrue(unknownPublic.isEmpty(), "Endpoints públicos no justificados: " + unknownPublic);
        assertTrue(withoutRule.isEmpty(), "Endpoints sin @PreAuthorize ni regla global: " + withoutRule);
    }

    @Test
    void unauthenticatedRequestsAreRejectedOutsideWhitelist() throws Exception {
        List<String> failures = new ArrayList<>();

        for (Endpoint endpoint : endpoints()) {
            MvcResult result = mockMvc.perform(builder(endpoint, false)).andReturn();
            int status = result.getResponse().getStatus();
            if (endpoint.isPublic()) {
                if (status == 401 || status == 403) {
                    failures.add("público rechazado: " + endpoint.method() + " " + endpoint.path() + " -> " + status);
                }
            } else if (status != 401 && status != 403) {
                failures.add("sin auth no rechazado: " + endpoint.method() + " " + endpoint.path() + " -> " + status);
            }
        }

        assertTrue(failures.isEmpty(), String.join("\n", failures));
    }

    @Test
    void actuatorWhitelistExposesOnlyHealthAndInfo() throws Exception {
        for (String path : List.of("/actuator/health", "/actuator/info")) {
            int status = mockMvc.perform(request(HttpMethod.GET, path)).andReturn().getResponse().getStatus();
            assertTrue(status != 401 && status != 403, path + " debe ser público, fue " + status);
        }
        int metrics = mockMvc.perform(request(HttpMethod.GET, "/actuator/metrics")).andReturn()
                .getResponse().getStatus();
        assertTrue(metrics == 401 || metrics == 403, "/actuator/metrics debe requerir autenticación, fue " + metrics);
    }

    @ParameterizedTest(name = "rol {0}")
    @MethodSource("roles")
    void roleMatrixMatchesEffectivePreAuthorize(String role) throws Exception {
        List<String> failures = new ArrayList<>();

        for (Endpoint endpoint : endpoints()) {
            boolean allowed = endpoint.allows(role);
            MvcResult result = mockMvc.perform(builder(endpoint, true)
                    .header("Authorization", "Bearer " + token(role))).andReturn();
            int status = result.getResponse().getStatus();

            if (endpoint.path().startsWith("/api/v1/auth/")
                    || endpoint.path().endsWith("/notifications/subscribe")) {
                // /auth/me y SSE subscribe validan identidad en el controller; no son reglas de rol.
                continue;
            }

            if (allowed) {
                if (status == 403) {
                    failures.add("permitido rechazado: " + role + " " + endpoint.method() + " "
                            + endpoint.path() + " -> " + status + " (" + endpoint.location() + ")");
                }
            } else {
                if (status >= 200 && status < 300) {
                    failures.add("denegado aceptado: " + role + " " + endpoint.method() + " "
                            + endpoint.path() + " -> " + status + " (" + endpoint.location() + ")");
                } else if (!endpoint.hasPreSecurityInput() && status != 403) {
                    failures.add("denegado no devuelve 403: " + role + " " + endpoint.method() + " "
                            + endpoint.path() + " -> " + status + " (" + endpoint.location() + ")");
                }
            }
        }

        assertFalse(!failures.isEmpty(), String.join("\n", failures));
    }

    private List<Endpoint> endpoints() {
        if (endpoints != null) {
            return endpoints;
        }
        List<Endpoint> found = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo info = entry.getKey();
            HandlerMethod handler = entry.getValue();
            if (!handler.getBeanType().getName().startsWith("com.casrusil.siierpai")) {
                continue;
            }
            Set<RequestMethod> methods = info.getMethodsCondition().getMethods();
            List<RequestMethod> httpMethods = methods.isEmpty()
                    ? List.of(RequestMethod.GET)
                    : new ArrayList<>(methods);

            for (String pattern : info.getPatternValues()) {
                if (!pattern.startsWith("/api/") || pattern.equals("/error")) {
                    continue;
                }
                for (RequestMethod method : httpMethods) {
                    String key = method + " " + pattern;
                    if (!seen.add(key)) {
                        continue;
                    }
                    found.add(new Endpoint(method, replaceVariables(pattern, handler), pattern, handler));
                }
            }
        }

        found.sort(Comparator.comparing(Endpoint::path).thenComparing(e -> e.method().name()));
        endpoints = found;
        return endpoints;
    }

    private MockHttpServletRequestBuilder builder(Endpoint endpoint, boolean authenticated) {
        MockHttpServletRequestBuilder builder = request(
                HttpMethod.valueOf(endpoint.method().name()), endpoint.requestPath());
        if (endpoint.hasRequestBody() && endpoint.method() != RequestMethod.GET
                && endpoint.method() != RequestMethod.DELETE) {
            builder.contentType(MediaType.APPLICATION_JSON).content("{}");
        }
        endpoint.queryParams().forEach(builder::param);
        endpoint.requestHeaders().forEach(builder::header);
        return builder;
    }

    private String token(String role) {
        return jwtTokenProvider.generateToken(
                new UserId(java.util.UUID.fromString(USER_ID)),
                new CompanyId(java.util.UUID.fromString(COMPANY_ID)),
                UserRole.valueOf(role));
    }

    private static String replaceVariables(String path, HandlerMethod handler) {
        String result = path;
        for (Parameter parameter : handler.getMethod().getParameters()) {
            PathVariable pathVariable = parameter.getAnnotation(PathVariable.class);
            if (pathVariable == null) {
                continue;
            }
            String name = pathVariable.value().isBlank() ? pathVariable.name() : pathVariable.value();
            if (name.isBlank()) {
                name = parameter.getName();
            }
            result = result.replace("{" + name + "}", pathValue(name, parameter.getType()));
        }
        return result;
    }

    private static String pathValue(String name, Class<?> type) {
        if (type.isEnum()) {
            return type.getEnumConstants()[0].toString();
        }
        if (type == UUID.class) {
            return UUID.randomUUID().toString();
        }
        if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
            return "1";
        }
        return switch (name) {
            case "period" -> "2026-11";
            case "fiscalYear" -> "2026";
            case "date" -> "2026-11-30";
            case "tipo" -> "39";
            case "folio" -> "1";
            case "rut" -> "76123456-7";
            case "userId" -> USER_ID;
            case "accountCode", "code" -> "110101";
            case "metricType" -> "ENERGY";
            case "severity" -> "HIGH";
            default -> "1";
        };
    }

    private record Endpoint(RequestMethod method, String requestPath, String path, HandlerMethod handler) {

        boolean isPublic() {
            return PUBLIC_PATHS.contains(path);
        }

        boolean hasRequestBody() {
            for (Parameter parameter : handler.getMethod().getParameters()) {
                if (parameter.isAnnotationPresent(RequestBody.class)
                        || parameter.isAnnotationPresent(RequestPart.class)
                        || MultipartFile.class.isAssignableFrom(parameter.getType())) {
                    return true;
                }
            }
            return false;
        }

        boolean hasPreSecurityInput() {
            for (Parameter parameter : handler.getMethod().getParameters()) {
                if (parameter.isAnnotationPresent(RequestBody.class)
                        || parameter.isAnnotationPresent(RequestPart.class)
                        || MultipartFile.class.isAssignableFrom(parameter.getType())) {
                    return true;
                }
                if (parameter.isAnnotationPresent(RequestParam.class)
                        || parameter.isAnnotationPresent(RequestHeader.class)) {
                    return true;
                }
                if (parameter.isAnnotationPresent(PathVariable.class)
                        && parameter.getType().isEnum()) {
                    return true;
                }
            }
            return false;
        }

        Map<String, String> queryParams() {
            Map<String, String> params = new java.util.LinkedHashMap<>();
            if (path.endsWith("/notifications/subscribe")) {
                params.put("userId", USER_ID);
            }
            for (Parameter parameter : handler.getMethod().getParameters()) {
                RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
                if (requestParam == null) {
                    continue;
                }
                String name = requestParam.value().isBlank() ? requestParam.name() : requestParam.value();
                if (name.isBlank()) {
                    name = parameter.getName();
                }
                String value = requestParam.defaultValue().isEmpty()
                        ? pathValue(name, parameter.getType())
                        : requestParam.defaultValue();
                params.put(name, value);
            }
            return params;
        }

        Map<String, String> requestHeaders() {
            Map<String, String> headers = new java.util.LinkedHashMap<>();
            for (Parameter parameter : handler.getMethod().getParameters()) {
                RequestHeader requestHeader = parameter.getAnnotation(RequestHeader.class);
                if (requestHeader == null) {
                    continue;
                }
                String name = requestHeader.value().isBlank() ? requestHeader.name() : requestHeader.value();
                if (name.isBlank()) {
                    name = parameter.getName();
                }
                headers.put(name, "test");
            }
            return headers;
        }

        String preAuthorize() {
            PreAuthorize methodRule = AnnotatedElementUtils.findMergedAnnotation(handler.getMethod(), PreAuthorize.class);
            if (methodRule != null) {
                return methodRule.value();
            }
            PreAuthorize classRule = AnnotatedElementUtils.findMergedAnnotation(
                    handler.getMethod().getDeclaringClass(), PreAuthorize.class);
            return classRule == null ? null : classRule.value();
        }

        Set<String> allowedRoles() {
            if (isPublic()) {
                return Set.of(PUBLIC);
            }
            String expression = preAuthorize();
            if (expression == null || expression.isBlank() || expression.contains("isAuthenticated()")) {
                return Set.of(AUTHENTICATED);
            }

            Set<String> roles = new TreeSet<>();
            Matcher any = ANY_ROLE.matcher(expression);
            while (any.find()) {
                roles.addAll(QUOTED.matcher(any.group(1)).results().map(m -> m.group(1)).toList());
            }
            Matcher one = ONE_ROLE.matcher(expression);
            while (one.find()) {
                roles.add(one.group(1));
            }
            if (roles.isEmpty()) {
                return Set.of(AUTHENTICATED);
            }
            return roles;
        }

        boolean allows(String role) {
            Set<String> roles = allowedRoles();
            return roles.contains(PUBLIC) || roles.contains(AUTHENTICATED) || roles.contains(role);
        }

        String location() {
            return handler.getBeanType().getSimpleName() + "#" + handler.getMethod().getName();
        }
    }
}
