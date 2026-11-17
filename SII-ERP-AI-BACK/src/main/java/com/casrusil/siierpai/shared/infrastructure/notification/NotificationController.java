package com.casrusil.siierpai.shared.infrastructure.notification;

import com.casrusil.siierpai.shared.infrastructure.context.CompanyContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
}
