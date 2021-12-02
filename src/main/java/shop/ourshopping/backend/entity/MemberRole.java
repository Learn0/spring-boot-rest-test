package shop.ourshopping.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

//로그인한 관리자 및 사용자 체크를 위한 열거형
@Getter
@AllArgsConstructor
public enum MemberRole {

	ADMIN("ROLE_ADMIN"),
	USER("ROLE_USER");

	private String value;
}