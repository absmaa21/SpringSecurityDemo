package htlkaindorf.springsecuritydemo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class DemoController {

    // -- PUBLIC --
    @GetMapping("/public/hello")
    public String publicHello() {
        return "Hello from PUBLIC endpoint! No token needed.";
    }

    // -- PRIVATE --
    @GetMapping("/private/hello")
    public String privateHello() {
        return "Hello from PRIVATE endpoint! Valid token needed.";
    }

    // -- MANAGER only --
    @GetMapping("/maintenance/hello")
    public String managerHello() {
        return "Hello from MANAGER endpoint! Role Manager needed.";
    }

    @GetMapping("/users/{username}")
    @PreAuthorize("#username == authentication.name OR hasRole('ADMIN')")
    public String getUser(
            @PathVariable String username,
            Authentication auth // Wird über die Filter Chain gesetzt
    ) {
        return "Access granted to user profile: " + username + " (logged in as: " + auth.getName() + ")";
    }

}
