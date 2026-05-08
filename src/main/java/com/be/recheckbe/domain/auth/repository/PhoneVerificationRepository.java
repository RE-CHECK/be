package com.be.recheckbe.domain.auth.repository;

import com.be.recheckbe.domain.auth.entity.PhoneVerification;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

  Optional<PhoneVerification> findTopByPhoneNumberOrderByCreatedAtDesc(String phoneNumber);

  Optional<PhoneVerification> findByVerifiedTokenAndVerifiedTrue(String verifiedToken);

  @Transactional
  void deleteByPhoneNumber(String phoneNumber);
}
