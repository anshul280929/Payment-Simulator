package com.payment.simulator.common.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Aspect
public class AuditLogAspect {

    private static final Logger log = LoggerFactory.getLogger(AuditLogAspect.class);

    @Around("@annotation(com.payment.simulator.common.audit.AuditLog)")
    public Object logAudit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AuditLog auditLog = signature.getMethod().getAnnotation(AuditLog.class);

        String action = auditLog.action().isEmpty()
                ? signature.getMethod().getName()
                : auditLog.action();
        String entity = auditLog.entity().isEmpty()
                ? signature.getDeclaringType().getSimpleName()
                : auditLog.entity();

        LocalDateTime startTime = LocalDateTime.now();
        log.info("[AUDIT] Action={} Entity={} StartTime={} Method={}",
                action, entity, startTime, signature.toShortString());

        try {
            Object result = joinPoint.proceed();
            log.info("[AUDIT] Action={} Entity={} Status=SUCCESS Duration={}ms",
                    action, entity, System.currentTimeMillis());
            return result;
        } catch (Exception e) {
            log.warn("[AUDIT] Action={} Entity={} Status=FAILED Error={}",
                    action, entity, e.getMessage());
            throw e;
        }
    }
}
