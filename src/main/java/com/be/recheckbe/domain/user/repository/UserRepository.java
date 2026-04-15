package com.be.recheckbe.domain.user.repository;

import com.be.recheckbe.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  @Query("SELECT u FROM User u JOIN FETCH u.department d JOIN FETCH d.college WHERE u.id = :userId")
  Optional<User> findByIdWithCollegeInfo(@Param("userId") Long userId);
}
