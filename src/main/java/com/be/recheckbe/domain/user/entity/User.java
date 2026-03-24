package com.be.recheckbe.domain.user.entity;

import com.be.recheckbe.global.common.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username; // 사용자 아아디

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    @Column(nullable = false)
    private String name; // 사용자 이름

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role; // user or admin

    @Column(nullable = false)
    private String phoneNumber; // 전화번호

    @Column(nullable = false)
    private int studentNumber; // 학번

    // 비밀번호 변경 시 사용하는 메서드
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }
}