package com.payment.simulator.bank.repository;

import com.payment.simulator.bank.entity.AuthorizationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorizationLogRepository extends JpaRepository<AuthorizationLog, Long> {
}
