package shop.ourshopping.backend.controller;

import shop.ourshopping.backend.dto.MemberRequestDTO;
import shop.ourshopping.backend.dto.MemberResponseDTO;
import shop.ourshopping.backend.dto.TokenDTO;
import shop.ourshopping.backend.dto.TokenRequestDTO;
import shop.ourshopping.backend.service.AuthService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

	@Autowired
	private AuthService authService;

	@PostMapping("/signup")
	public ResponseEntity<MemberResponseDTO> signup(@RequestBody MemberRequestDTO memberRequestDto) {
		return ResponseEntity.ok(authService.signup(memberRequestDto));
	}

	@PostMapping("/login")
	public ResponseEntity<TokenDTO> login(@RequestBody MemberRequestDTO memberRequestDto) {
		return ResponseEntity.ok(authService.login(memberRequestDto));
	}

	@PostMapping("/reissue")
	public ResponseEntity<TokenDTO> reissue(@RequestBody TokenRequestDTO tokenRequestDto) {
		return ResponseEntity.ok(authService.reissue(tokenRequestDto));
	}
}
