package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.auth.AuthRequest;
import htlkaindorf.springsecuritydemo.auth.AuthResponse;
import htlkaindorf.springsecuritydemo.auth.MfaRequest;
import htlkaindorf.springsecuritydemo.auth.MfaResponse;
import htlkaindorf.springsecuritydemo.entity.MfaToken;
import htlkaindorf.springsecuritydemo.entity.Role;
import htlkaindorf.springsecuritydemo.entity.User;
import htlkaindorf.springsecuritydemo.entity.VerificationToken;
import htlkaindorf.springsecuritydemo.exceptions.*;
import htlkaindorf.springsecuritydemo.repositories.MfaRepository;
import htlkaindorf.springsecuritydemo.repositories.UserRepository;
import htlkaindorf.springsecuritydemo.repositories.VerificationTokenRepository;
import htlkaindorf.springsecuritydemo.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
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
    private final MfaRepository mfaRepository;
    private final MfaService mfaService;

    @Override
    public MfaResponse login(AuthRequest request) {

        Optional<User> foundUser = userRepository.findUserByUsername(request.getUsername());

        if (foundUser.isEmpty()) {
            throw new UsernameWrongException("User " + request.getUsername() + " not found.");
        }

        if (!passwordEncoder.matches(request.getPassword(), foundUser.get().getPassword())) {
            throw new PasswordWrongException("Invalid Password.");
        }

        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
        ));

        UserDetails user = (UserDetails) authentication.getPrincipal();

        String jwt = mfaService.generateMfaToken((User) user);

        return new MfaResponse(jwt);
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
    public AuthResponse otpLogin(MfaRequest request) {

        Optional<MfaToken> mfaToken = mfaRepository.findById(request.getToken());
        Optional<User> user = userRepository.findUserByUsername(jwtService.extractUsername(request.getToken()));

        if (mfaToken.isEmpty() || user.isEmpty() || jwtService.isTokenValid(mfaToken.get().getToken(), user.get())) {
            throw new MfaTokenInvalid("The given token for MFA is not valid or expired!");
        }

        if (mfaToken.get().getOneTimePassword().equals(request.getOtp())) {
            throw new MfaOtpInvalid("The given OTP is not matching!");
        }

        String token = jwtService.generateToken(user.get());
        return new AuthResponse(token);
    }

}
