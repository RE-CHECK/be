package com.be.recheckbe.domain.week.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "week_config")
public class Week {

  @Id
  private Long id; // 항상 1 (단일 행 싱글턴)

  @Column
  private Integer weekNumber; // null = 테스트 기간, 1~3 = 활성화된 주차

  public void activate(int weekNumber) {
    this.weekNumber = weekNumber;
  }

  public void deactivate() {
    this.weekNumber = null;
  }
}