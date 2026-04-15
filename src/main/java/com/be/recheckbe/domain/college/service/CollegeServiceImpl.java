package com.be.recheckbe.domain.college.service;

import com.be.recheckbe.domain.college.dto.CollegeResponse;
import com.be.recheckbe.domain.college.repository.CollegeRepository;
import com.be.recheckbe.domain.department.dto.DepartmentResponse;
import com.be.recheckbe.domain.department.repository.DepartmentRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollegeServiceImpl implements CollegeService {

  private final CollegeRepository collegeRepository;
  private final DepartmentRepository departmentRepository;

  @Override
  public List<CollegeResponse> getColleges() {
    return collegeRepository.findAll().stream().map(CollegeResponse::from).toList();
  }

  @Override
  public List<DepartmentResponse> getDepartments(Long collegeId) {
    return departmentRepository.findByCollegeId(collegeId).stream()
        .map(DepartmentResponse::from)
        .toList();
  }
}
