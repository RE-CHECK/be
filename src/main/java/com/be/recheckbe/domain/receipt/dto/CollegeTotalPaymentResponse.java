package com.be.recheckbe.domain.receipt.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CollegeTotalPaymentResponse {

    private Long collegeId;
    private String collegeName;
    private int totalPaymentAmount;
}