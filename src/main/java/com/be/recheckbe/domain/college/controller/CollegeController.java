package com.be.recheckbe.domain.college.controller;

import com.be.recheckbe.domain.college.dto.CollegeResponse;
import com.be.recheckbe.domain.college.service.CollegeService;
import com.be.recheckbe.domain.department.dto.DepartmentResponse;
import com.be.recheckbe.global.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/colleges")
@Tag(name = "College", description = "단과대/학과 API")
public class CollegeController {

  private final CollegeService collegeService;

  @GetMapping
  @Operation(summary = "단과대 목록 조회")
  public BaseResponse<List<CollegeResponse>> getColleges() {
    return BaseResponse.success(collegeService.getColleges());
  }

  @GetMapping("/{collegeId}/departments")
  @Operation(summary = "단과대별 학과 목록 조회")
  public BaseResponse<List<DepartmentResponse>> getDepartments(@PathVariable Long collegeId) {
    return BaseResponse.success(collegeService.getDepartments(collegeId));
  }
}
