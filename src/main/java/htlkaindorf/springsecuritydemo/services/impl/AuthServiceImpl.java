package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.auth.*;
import htlkaindorf.springsecuritydemo.entity.Role;
import htlkaindorf.springsecuritydemo.entity.User;
import htlkaindorf.springsecuritydemo.entity.VerificationToken;
import htlkaindorf.springsecuritydemo.exceptions.*;
import htlkaindorf.springsecuritydemo.repositories.UserRepository;
import htlkaindorf.springsecuritydemo.repositories.VerificationTokenRepository;
import htlkaindorf.springsecuritydemo.services.AuthService;
import htlkaindorf.springsecuritydemo.services.EmailService;
import htlkaindorf.springsecuritydemo.services.EmailVerificationService;
import htlkaindorf.springsecuritydemo.services.JwtService;
import htlkaindorf.springsecuritydemo.services.LoginAttemptService;
import htlkaindorf.springsecuritydemo.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final EmailService emailService;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OtpService otpService;
    private final LoginAttemptService loginAttemptService;

    @Override
    public AuthResponse login(AuthRequest request) {

        Optional<User> foundUser = userRepository.findUserByUsername(request.getUsername());

        if (foundUser.isEmpty()) {
            throw new UsernameWrongException("User " + request.getUsername() + " not found.");
        }

        if (!passwordEncoder.matches(request.getPassword(), foundUser.get().getPassword())) {
            throw new PasswordWrongException("Invalid Password.");
        }

        String refresh = jwtService.generateRefreshToken(foundUser.get());
        String access = jwtService.generateAccessToken(foundUser.get());

        return new AuthResponse(refresh, access);
    }
    @Override
    public MfaResponse signin(AuthRequest request) {
        // Check if account is locked
        if (loginAttemptService.isBlocked(request.getUsername())) {
            throw new AccountLockedException("Account is temporarily locked due to multiple failed login attempts. Please try again later.");
        }

        Optional<User> foundUser = userRepository.findUserByUsername(request.getUsername());

        if (foundUser.isEmpty()) {
            loginAttemptService.loginFailed(request.getUsername());
            throw new UsernameWrongException("User " + request.getUsername() + " not found.");
        }

        if (!passwordEncoder.matches(request.getPassword(), foundUser.get().getPassword())) {
            loginAttemptService.loginFailed(request.getUsername());
            throw new PasswordWrongException("Invalid Password.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            ));

            UserDetails user = (UserDetails) authentication.getPrincipal();

            // Generate MFA token instead of regular JWT
            String mfaToken = jwtService.generateMfaToken((User) user);

            // Generate and send OTP
            String otp = otpService.generateOtp(user.getUsername());
            emailService.sendOtpEmail(user.getUsername(), otp);

            // Login succeeded, reset failed attempts
            loginAttemptService.loginSucceeded(request.getUsername());

            return new MfaResponse(mfaToken);
        } catch (Exception e) {
            loginAttemptService.loginFailed(request.getUsername());
            throw e;
        }
    }

    @Override
    public AuthResponse verifyOtp(String mfaToken, String otp) {
        // Extract username from MFA token
        String username = jwtService.extractUsername(mfaToken);
        
        // Validate that it's an MFA token
        if (!jwtService.isMfaToken(mfaToken)) {
            throw new htlkaindorf.springsecuritydemo.exceptions.InvalidMfaTokenException("Invalid MFA token");
        }

        // Validate OTP
        if (!otpService.validateOtp(username, otp)) {
            throw new htlkaindorf.springsecuritydemo.exceptions.InvalidOtpException("Invalid or expired OTP");
        }

        // Get user from database
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameWrongException("User not found"));

        // Clear the OTP after successful validation
        otpService.clearOtp(username);

        // Generate final JWT without MFA claim
        String refresh = jwtService.generateRefreshToken(user);
        String access = jwtService.generateAccessToken(user);

        return new AuthResponse(refresh, access);
    }

    public void register(AuthRequest request) {
        if (userRepository.findUserByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsAuthenticationException("A user with this email is already registered.");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(newUser);

        emailService.sendVerificationEmail(
                newUser.getUsername(),
                emailVerificationService.generateVerificationToken(newUser)
        );
    }


    public void verifyEmail(String token) {
        VerificationToken vToken = verificationTokenRepository.getVerificationTokenByToken(token);

        if (!vToken.getExpiryDate().isAfter(LocalDateTime.now())) {
            throw new EmailVerificationTokenExpired("The Verification Token is expired!");
        }

        vToken.getUser().setEnabled(true);
        userRepository.save(vToken.getUser());
        verificationTokenRepository.delete(vToken);
    }

    @Override
    public AccessTokenResponse refresh(RefreshTokenRequest request) {

        String username = jwtService.extractUsername(request.getRefreshToken());

        if (!jwtService.isRefreshToken(request.getRefreshToken()))
            throw new AuthorizationTokenExpiredException("Given token cannot be used for refresh.");

        Optional<User> foundUser = userRepository.findUserByUsername(username);

        if (foundUser.isEmpty())
            throw new UsernameNotFoundException("Username not found!");

        if (!jwtService.isTokenValid(request.getRefreshToken(), foundUser.get()))
            throw new AuthorizationTokenExpiredException("Refresh Token is expired!");

        String access = jwtService.generateAccessToken(foundUser.get());

        return new AccessTokenResponse(access);
    }

}
