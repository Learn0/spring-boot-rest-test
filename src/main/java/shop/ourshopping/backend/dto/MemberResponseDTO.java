package shop.ourshopping.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import shop.ourshopping.backend.entity.Member;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemberResponseDTO {
    private String email;

    public static MemberResponseDTO of(Member member) {
        return new MemberResponseDTO(member.getEmail());
    }
}