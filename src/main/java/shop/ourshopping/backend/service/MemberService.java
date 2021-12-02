package shop.ourshopping.backend.service;

import shop.ourshopping.backend.utils.SecurityUtil;
import shop.ourshopping.backend.dto.MemberResponseDTO;
import shop.ourshopping.backend.repository.MemberRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {

	private final MemberRepository memberRepository;

	@Transactional(readOnly = true)
	public MemberResponseDTO getMemberInfo(String email) {
		return memberRepository.findByEmail(email).map(MemberResponseDTO::of)
				.orElseThrow(() -> new RuntimeException("유저 정보가 없습니다."));
	}

	// 현재 SecurityContext 에 있는 유저 정보 가져오기
	@Transactional(readOnly = true)
	public MemberResponseDTO getMyInfo() {
		return memberRepository.findById(SecurityUtil.getCurrentMemberId()).map(MemberResponseDTO::of)
				.orElseThrow(() -> new RuntimeException("로그인 유저 정보가 없습니다."));
	}
}