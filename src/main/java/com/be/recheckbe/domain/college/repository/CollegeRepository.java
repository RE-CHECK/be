package com.be.recheckbe.domain.college.repository;

import com.be.recheckbe.domain.college.entity.College;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollegeRepository extends JpaRepository<College, Long> {}
