package htlkaindorf.springsecuritydemo.services.impl;

import htlkaindorf.springsecuritydemo.entity.MfaToken;
import htlkaindorf.springsecuritydemo.entity.User;
import htlkaindorf.springsecuritydemo.repositories.MfaRepository;
import htlkaindorf.springsecuritydemo.services.EmailService;
import htlkaindorf.springsecuritydemo.services.JwtService;
import htlkaindorf.springsecuritydemo.services.MfaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.random.RandomGenerator;


@Service
@RequiredArgsConstructor
public class MfaServiceImpl implements MfaService {

    private final MfaRepository mfaRepository;
    private final JwtService jwtService;
    private final EmailService emailService;

    @Override
    public String generateMfaToken(User user) {
        String token = jwtService.generateMfaToken(user);
        Integer otp = RandomGenerator.getDefault().nextInt(100000, 1000000);

        MfaToken mfaToken = new MfaToken(token, otp);
        mfaRepository.save(mfaToken);
        emailService.sendOtpEmail(user.getUsername(), mfaToken.getOneTimePassword());

        return mfaToken.getToken();
    }

}
