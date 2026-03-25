package com.be.recheckbe.domain.department.repository;

import com.be.recheckbe.domain.department.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    List<Department> findByCollegeId(Long collegeId);
}