package com.casrusil.siierpai.modules.sso.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final com.casrusil.siierpai.modules.sso.infrastructure.security.SecurityFilter securityFilter;

    /**
     * Orígenes CORS permitidos, inyectados desde la variable de entorno {@code app.cors.allowed-origins}.
     * Valor por defecto: solo localhost:3000 (desarrollo).
     * En producción, definir el valor en application-prod.properties o variables de entorno.
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOriginsRaw;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SecurityConfig.class);

    public SecurityConfig(com.casrusil.siierpai.modules.sso.infrastructure.security.SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
        log.info("🔥 CARGANDO CONFIGURACIÓN DE SEGURIDAD PERSONALIZADA 🔥");
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Deshabilitar CSRF: No es necesario para APIs REST stateless con JWT
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configurar CORS explícitamente usando el Bean definido abajo
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Gestión de Sesiones: STATELESS (Crucial para evitar jsessionid y
                // redirecciones)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 4. Reglas de Autorización
                .authorizeHttpRequests(auth -> auth
                        // Permitir OPTIONS (pre-flight requests de CORS)
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        // /me requiere sesión aunque cuelgue de /auth
                        .requestMatchers("/api/v1/auth/me", "/auth/me").authenticated()
                        // Endpoints públicos de autenticación
                        .requestMatchers("/api/v1/auth/**", "/error").permitAll()
                        // Actuator: solo health/info son públicos (probes de LB/k8s).
                        // El resto (metrics, prometheus, env, loggers...) requiere rol ADMIN.
                        .requestMatchers("/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        // Todo lo demás requiere autenticación
                        .anyRequest().authenticated())

                // 4.b Sin sesión, responder 401 (no 403) en endpoints autenticados.
                .exceptionHandling(ex -> ex.authenticationEntryPoint(
                        (request, response, authException) -> response.sendError(
                                HttpStatus.UNAUTHORIZED.value(), "No autenticado")))

                // 5. Agregar filtro JWT antes del filtro de autenticación de usuario/password
                .addFilterBefore(securityFilter,
                        org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        // 6. IMPORTANTE: No activar formLogin() ni httpBasic() para evitar
        // redirecciones a /login

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos: se leen de la variable de entorno app.cors.allowed-origins
        // Soporta múltiples valores separados por coma: "https://app.dominio.cl,https://admin.dominio.cl"
        List<String> origins = List.of(allowedOriginsRaw.split(","));
        configuration.setAllowedOrigins(origins);
        log.info("CORS configurado para los siguientes orígenes: {}", origins);

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Headers permitidos (JWT, tenant y cabeceras estándar del navegador).
        // Lista explícita: con allowCredentials=true NO se puede usar "*".
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "X-Tenant-ID",
                "X-Company-Id",
                "X-Requested-With"));

        // El front descarga archivos (Excel/Previred/PDF) y puede leer el nombre
        // sugerido por el backend.
        configuration.setExposedHeaders(List.of("Content-Disposition"));

        // Permitir credenciales (cookies, headers de auth)
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
