package com.be.recheckbe.domain.department.entity;

import com.be.recheckbe.domain.college.entity.College;
import com.be.recheckbe.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "departments")
public class Department extends BaseTimeEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name; // 학과 이름

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "college_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  private College college;
}
