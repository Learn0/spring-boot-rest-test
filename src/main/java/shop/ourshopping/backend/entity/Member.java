package shop.ourshopping.backend.entity;

import lombok.*;

import javax.persistence.*;

@Getter
@NoArgsConstructor
@Table(name = "member")
@Entity
public class Member {

    @Id
    @Column(name = "member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private MemberRole authority;

    @Builder
    public Member(String email, String password, MemberRole authority) {
        this.email = email;
        this.password = password;
        this.authority = authority;
    }
}
