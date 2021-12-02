package shop.ourshopping.backend.component;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import shop.ourshopping.backend.dto.TokenDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class TokenProvider {

	private static final String AUTHORITIES_KEY = "auth";
	private static final String BEARER_TYPE = "bearer";
	private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 30; // 30분
	private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

	private final Key key;

	public TokenProvider(@Value("${jwt.secret}") String secretKey) {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		this.key = Keys.hmacShaKeyFor(keyBytes);
	}

	public TokenDTO generateTokenDto(Authentication authentication) {
		// 권한들 가져오기
		String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
				.collect(Collectors.joining(","));

		long now = (new Date()).getTime();

		// Access Token 생성
		Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
		String accessToken = Jwts.builder().setSubject(authentication.getName()) // payload "sub": "name"
				.claim(AUTHORITIES_KEY, authorities) // payload "auth": "ROLE_USER"
				.setExpiration(accessTokenExpiresIn) // payload "exp": 1516239022 (예시)
				.signWith(SignatureAlgorithm.HS512, key) // header "alg": "HS512"
				.compact();

		// Refresh Token 생성
		String refreshToken = Jwts.builder().setExpiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
				.signWith(SignatureAlgorithm.HS512, key).compact();

		return TokenDTO.builder().grantType(BEARER_TYPE).accessToken(accessToken)
				.accessTokenExpiresIn(accessTokenExpiresIn.getTime()).refreshToken(refreshToken).build();
	}

	public Authentication getAuthentication(String accessToken) {
		// 토큰 복호화
		Claims claims = parseClaims(accessToken);

		if (claims.get(AUTHORITIES_KEY) == null) {
			throw new RuntimeException("권한 정보가 없는 토큰입니다.");
		}

		// 클레임에서 권한 정보 가져오기
		Collection<? extends GrantedAuthority> authorities = Arrays
				.stream(claims.get(AUTHORITIES_KEY).toString().split(",")).map(SimpleGrantedAuthority::new)
				.collect(Collectors.toList());

		// UserDetails 객체를 만들어서 Authentication 리턴
		UserDetails principal = new User(claims.getSubject(), "", authorities);

		return new UsernamePasswordAuthenticationToken(principal, "", authorities);
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parser().setSigningKey(key).parseClaimsJws(token);
			return true;
		} catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
			System.out.println("잘못된 JWT토큰");
		} catch (ExpiredJwtException e) {
			System.out.println("만료된 JWT토큰");
		} catch (Exception e) {
			System.out.println("JWT토큰 에러");
		}
		return false;
	}

	private Claims parseClaims(String accessToken) {
		try {
			return Jwts.parser().setSigningKey(key).parseClaimsJws(accessToken).getBody();
		} catch (ExpiredJwtException e) {
			return e.getClaims();
		}
	}
}