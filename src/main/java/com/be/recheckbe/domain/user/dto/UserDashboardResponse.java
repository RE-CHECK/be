package com.be.recheckbe.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserDashboardResponse {

  private String name;
  private String collegeName;
  private int totalPaymentAmount;
}