package htlkaindorf.springsecuritydemo.controller;

import htlkaindorf.springsecuritydemo.auth.AuthRequest;
import htlkaindorf.springsecuritydemo.auth.AuthResponse;
import htlkaindorf.springsecuritydemo.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody AuthRequest authRequest
    ){
        return ResponseEntity.ok(authService.login(authRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody AuthRequest authRequest
    ){
        authService.register(authRequest);
        return ResponseEntity.ok("Successfully registered! Check Email for verification.");
    }

    @GetMapping("verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String token
    ) {
        authService.verifyEmail(token);
        return ResponseEntity.ok("Successfully verified!");
    }

}
