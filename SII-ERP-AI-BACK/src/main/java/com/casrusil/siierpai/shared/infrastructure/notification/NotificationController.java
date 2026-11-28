package com.casrusil.siierpai.shared.infrastructure.notification;

import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
@PreAuthorize("isAuthenticated()")
public class NotificationController {

    private final NotificationService notificationService;

    private final com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository userRepository;

    public NotificationController(NotificationService notificationService,
            com.casrusil.siierpai.modules.sso.domain.port.out.UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    @GetMapping("/subscribe")
    public SseEmitter subscribe(@RequestParam String userId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !(auth.getPrincipal() instanceof com.casrusil.siierpai.shared.domain.valueobject.UserId principal)
                || !principal.value().toString().equals(userId)) {
            throw new AccessDeniedException("Cannot subscribe to another user's notifications");
        }
        return notificationService.subscribe(userId);
    }

    @GetMapping("/preferences")
    public ResponseEntity<java.util.Map<String, String>> getPreferences() {
        // Get current user from context
        // Check if CompanyContext/UserContext is available or use SecurityContext
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !(auth.getPrincipal() instanceof com.casrusil.siierpai.shared.domain.valueobject.UserId userId)) {
            return ResponseEntity.status(401).build();
        }

        return userRepository.findById(userId)
                .map(u -> ResponseEntity.ok(u.getPreferences()))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza las preferencias de notificación del usuario autenticado.
     * Solo acepta las claves conocidas: email, push y marketing.
     */
    @PutMapping("/preferences")
    public ResponseEntity<Map<String, String>> updatePreferences(
            @RequestBody Map<String, String> preferences) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null
                || !(auth.getPrincipal() instanceof com.casrusil.siierpai.shared.domain.valueobject.UserId userId)) {
            return ResponseEntity.status(401).build();
        }

        return userRepository.findById(userId)
                .map(user -> {
                    Map<String, String> merged = new HashMap<>(user.getPreferences());
                    for (String key : List.of("email", "push", "marketing")) {
                        if (preferences.containsKey(key)) {
                            String value = String.valueOf(preferences.get(key)).toLowerCase();
                            if ("true".equals(value) || "false".equals(value)) {
                                merged.put(key, value);
                            }
                        }
                    }
                    user.setPreferences(merged);
                    userRepository.save(user);
                    return ResponseEntity.ok(merged);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
