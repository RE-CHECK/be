package com.be.recheckbe.domain.receipt.repository;

import com.be.recheckbe.domain.receipt.entity.Receipt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {
}