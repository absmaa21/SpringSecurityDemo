package htlkaindorf.springsecuritydemo.controller;

import htlkaindorf.springsecuritydemo.auth.*;
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
    public ResponseEntity<MfaResponse> login(
            @Valid @RequestBody AuthRequest authRequest
    ){
        return ResponseEntity.ok(authService.signin(authRequest));
    }

    @PostMapping("/signin")
    public ResponseEntity<MfaResponse> signin(
            @Valid @RequestBody AuthRequest authRequest
    ){
        return ResponseEntity.ok(authService.signin(authRequest));
    }

    @GetMapping("/otp-signin")
    public ResponseEntity<AuthResponse> otpSignin(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam String otp
    ){
        // Extract MFA token from Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }
        
        String mfaToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.verifyOtp(mfaToken, otp));
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

    @GetMapping("refresh-token")
    public ResponseEntity<AccessTokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest refreshRequest
            ) {
        return ResponseEntity.ok(authService.refresh(refreshRequest));
    }
}
