package htlkaindorf.springsecuritydemo.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MfaResponse {

    private String token;

}
