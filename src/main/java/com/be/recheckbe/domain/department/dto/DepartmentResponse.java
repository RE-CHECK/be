package com.be.recheckbe.domain.department.dto;

import com.be.recheckbe.domain.department.entity.Department;
import lombok.Getter;

@Getter
public class DepartmentResponse {

  private final Long id;
  private final String name;

  private DepartmentResponse(Department department) {
    this.id = department.getId();
    this.name = department.getName();
  }

  public static DepartmentResponse from(Department department) {
    return new DepartmentResponse(department);
  }
}
