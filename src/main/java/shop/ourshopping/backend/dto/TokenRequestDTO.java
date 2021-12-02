package shop.ourshopping.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TokenRequestDTO {
	
    private String accessToken;
    private String refreshToken;
}
