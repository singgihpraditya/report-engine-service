package com.singgih.reportengineservice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singgih.reportengineservice.config.LoggingProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class LoggingAspect {

    private final ObjectMapper objectMapper;
    private final LoggingProperties loggingProperties;

    @Pointcut("within(com.singgih.reportengineservice.controller..*)")
    public void controllerLayer() {}

    @Pointcut("within(com.singgih.reportengineservice.service..*)")
    public void serviceLayer() {}

    @Around("controllerLayer() || serviceLayer()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String className  = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        log.info("[{}#{}] --> {}", className, methodName,
                trim(serializeArgs(joinPoint.getArgs())));

        long start = System.currentTimeMillis();
        try {
            Object result   = joinPoint.proceed();
            long   elapsed  = System.currentTimeMillis() - start;

            log.info("[{}#{}] <-- ({}ms) {}", className, methodName,
                    elapsed, trim(serializeResult(result)));

            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - start;
            log.error("[{}#{}] <-- ({}ms) EXCEPTION: {}",
                    className, methodName, elapsed, ex.getMessage());
            throw ex;
        }
    }

    // -------------------------------------------------------------------------
    // Serialization helpers
    // -------------------------------------------------------------------------

    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) return "[]";
        try {
            Object[] safe = Arrays.stream(args)
                    .map(arg -> isSafeToSerialize(arg) ? arg : "<" + arg.getClass().getSimpleName() + ">")
                    .toArray();
            return objectMapper.writeValueAsString(safe);
        } catch (Exception e) {
            return Arrays.toString(args);
        }
    }

    private String serializeResult(Object result) {
        if (result == null) return "null";

        // ResponseEntity: log status + body saja agar lebih readable
        if (result instanceof ResponseEntity<?> re) {
            try {
                String body = objectMapper.writeValueAsString(re.getBody());
                return "ResponseEntity[status=" + re.getStatusCode().value() + ", body=" + body + "]";
            } catch (Exception e) {
                return "ResponseEntity[status=" + re.getStatusCode().value() + "]";
            }
        }

        try {
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            return result.toString();
        }
    }

    private boolean isSafeToSerialize(Object arg) {
        if (arg == null) return true;
        return !(arg instanceof HttpServletRequest)
                && !(arg instanceof HttpServletResponse)
                && !(arg instanceof BindingResult)
                && !(arg instanceof InputStream)
                && !(arg instanceof OutputStream);
    }

    // -------------------------------------------------------------------------
    // Trim helper
    // -------------------------------------------------------------------------

    private String trim(String message) {
        if (message == null) return null;
        int max = loggingProperties.getMaxMessageLength();
        if (message.length() > max) {
            return message.substring(0, max)
                    + "...[TRIMMED, total=" + message.length() + " chars]";
        }
        return message;
    }

}
