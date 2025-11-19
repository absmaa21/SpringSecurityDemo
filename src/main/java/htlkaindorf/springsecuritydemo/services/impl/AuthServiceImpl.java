package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.auth.AuthRequest;
import htlkaindorf.springsecuritydemo.auth.AuthResponse;
import htlkaindorf.springsecuritydemo.entity.Role;
import htlkaindorf.springsecuritydemo.entity.User;
import htlkaindorf.springsecuritydemo.exceptions.PasswordWrongException;
import htlkaindorf.springsecuritydemo.exceptions.UserAlreadyExistsAuthenticationException;
import htlkaindorf.springsecuritydemo.exceptions.UsernameWrongException;
import htlkaindorf.springsecuritydemo.repositories.UserRepository;
import htlkaindorf.springsecuritydemo.services.AuthService;
import htlkaindorf.springsecuritydemo.services.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse login(AuthRequest request) {

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

        String jwt = jwtService.generateToken((User) user);

        return new AuthResponse(jwt);
    }

    public AuthResponse register(AuthRequest request) {
        if (userRepository.findUserByUsername(request.getUsername()).isPresent()) {
            throw new UserAlreadyExistsAuthenticationException("A user with this email is already registered.");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(newUser);
        return login(request);
    }

}
