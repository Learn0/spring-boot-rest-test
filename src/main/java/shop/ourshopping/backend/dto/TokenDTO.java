package shop.ourshopping.backend.dto;

import lombok.*;

@Getter
@Setter
@Builder
public class TokenDTO {

    private String grantType;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresIn;
}
