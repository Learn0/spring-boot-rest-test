package shop.ourshopping.backend.service;

import lombok.RequiredArgsConstructor;
import shop.ourshopping.backend.component.TokenProvider;
import shop.ourshopping.backend.dto.MemberRequestDTO;
import shop.ourshopping.backend.dto.MemberResponseDTO;
import shop.ourshopping.backend.dto.TokenDTO;
import shop.ourshopping.backend.dto.TokenRequestDTO;
import shop.ourshopping.backend.entity.Member;
import shop.ourshopping.backend.entity.RefreshToken;
import shop.ourshopping.backend.repository.MemberRepository;
import shop.ourshopping.backend.repository.RefreshTokenRepository;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

	private final AuthenticationManagerBuilder authenticationManagerBuilder;
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenProvider tokenProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public MemberResponseDTO signup(MemberRequestDTO memberRequestDto) {
		if (memberRepository.existsByEmail(memberRequestDto.getEmail())) {
			throw new RuntimeException("이미 가입되어 있는 유저입니다");
		}

		Member member = memberRequestDto.toMember(passwordEncoder);
		return MemberResponseDTO.of(memberRepository.save(member));
	}

	@Transactional
	public TokenDTO login(MemberRequestDTO memberRequestDto) {
		// MemberDTO로 AuthenticationToken 생성
		UsernamePasswordAuthenticationToken authenticationToken = memberRequestDto.toAuthentication();

		// 실제로 검증 (사용자 비밀번호 체크) 이 이루어지는 부분
		// authenticate 메서드가 실행이 될 때 CustomUserDetailsService 에서 만들었던 loadUserByUsername
		// 메서드가 실행됨
		Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

		// 인증 정보를 기반으로 JWT 토큰 생성
		TokenDTO tokenDto = tokenProvider.generateTokenDto(authentication);

		// RefreshToken 저장
		RefreshToken refreshToken = RefreshToken.builder().key(authentication.getName())
				.value(tokenDto.getRefreshToken()).build();

		refreshTokenRepository.save(refreshToken);

		return tokenDto;
	}

	@Transactional
	public TokenDTO reissue(TokenRequestDTO tokenRequestDto) {
		// Refresh Token 검증
		if (!tokenProvider.validateToken(tokenRequestDto.getRefreshToken())) {
			throw new RuntimeException("Refresh Token 이 유효하지 않습니다.");
		}

		// Access Token 에서 Member ID 가져오기
		Authentication authentication = tokenProvider.getAuthentication(tokenRequestDto.getAccessToken());

		// 저장소에서 Member ID 를 기반으로 Refresh Token 값 가져옴
		RefreshToken refreshToken = refreshTokenRepository.findByKey(authentication.getName())
				.orElseThrow(() -> new RuntimeException("로그아웃 된 사용자입니다."));

		// Refresh Token 일치하는지 검사
		if (!refreshToken.getValue().equals(tokenRequestDto.getRefreshToken())) {
			throw new RuntimeException("토큰의 유저 정보가 일치하지 않습니다.");
		}

		// 새로운 토큰 생성
		TokenDTO tokenDto = tokenProvider.generateTokenDto(authentication);

		// 저장소 정보 업데이트
		RefreshToken newRefreshToken = refreshToken.updateValue(tokenDto.getRefreshToken());
		refreshTokenRepository.save(newRefreshToken);

		return tokenDto;
	}
}