package com.be.recheckbe.domain.admin.repository;

import com.be.recheckbe.domain.admin.entity.Blacklist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

  boolean existsByPhoneNumberAndActiveTrue(String phoneNumber);

  Optional<Blacklist> findByPhoneNumberAndActiveTrue(String phoneNumber);

  List<Blacklist> findAllByOrderByCreatedAtDesc();
}
