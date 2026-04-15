package com.be.recheckbe.domain.college.service;

import com.be.recheckbe.domain.college.dto.CollegeResponse;
import com.be.recheckbe.domain.department.dto.DepartmentResponse;
import java.util.List;

public interface CollegeService {

  List<CollegeResponse> getColleges();

  List<DepartmentResponse> getDepartments(Long collegeId);
}
