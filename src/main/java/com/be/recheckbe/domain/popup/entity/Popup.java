package com.be.recheckbe.domain.popup.entity;

import com.be.recheckbe.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "popup_config")
public class Popup extends BaseTimeEntity {

  public static final Long CONFIG_ID = 1L;

  @Id private Long id;

  @Column private String content;

  @Column(nullable = false)
  private boolean active;

  public void update(String content) {
    this.content = content;
    this.active = true;
  }

  public void deactivate() {
    this.active = false;
  }
}
