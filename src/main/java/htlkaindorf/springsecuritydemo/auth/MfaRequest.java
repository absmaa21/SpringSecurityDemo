package htlkaindorf.springsecuritydemo.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MfaRequest {

    private String token;

    private Integer otp;

}
