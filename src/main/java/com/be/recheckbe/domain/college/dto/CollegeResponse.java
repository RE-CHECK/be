package com.be.recheckbe.domain.college.dto;

import com.be.recheckbe.domain.college.entity.College;
import lombok.Getter;

@Getter
public class CollegeResponse {

    private final Long id;
    private final String name;

    private CollegeResponse(College college) {
        this.id = college.getId();
        this.name = college.getName();
    }

    public static CollegeResponse from(College college) {
        return new CollegeResponse(college);
    }
}