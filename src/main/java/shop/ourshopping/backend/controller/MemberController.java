package shop.ourshopping.backend.controller;

import lombok.RequiredArgsConstructor;
import shop.ourshopping.backend.dto.MemberResponseDTO;
import shop.ourshopping.backend.service.MemberService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {
	private final MemberService memberService;

	@GetMapping("/me")
	public ResponseEntity<MemberResponseDTO> getMyMemberInfo() {
		return ResponseEntity.ok(memberService.getMyInfo());
	}

	@GetMapping("/{email}")
	public ResponseEntity<MemberResponseDTO> getMemberInfo(@PathVariable String email) {
		return ResponseEntity.ok(memberService.getMemberInfo(email));
	}
}