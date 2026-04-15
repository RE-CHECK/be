package com.be.recheckbe.domain.department.repository;

import com.be.recheckbe.domain.department.entity.Department;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
  List<Department> findByCollegeId(Long collegeId);
}
