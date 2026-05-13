package com.singgih.reportengineservice.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.singgih.reportengineservice.config.LoggingProperties;
import com.singgih.reportengineservice.dto.ReportRequest;
import com.singgih.reportengineservice.model.Report;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoggingAspectTest {

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    private LoggingAspect loggingAspect;
    private LoggingProperties loggingProperties;

    @BeforeEach
    void setUp() {
        loggingProperties = new LoggingProperties();
        loggingProperties.setMaxMessageLength(5000);
        loggingAspect = new LoggingAspect(new ObjectMapper(), loggingProperties);

        when(joinPoint.getTarget()).thenReturn(new Object());
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("testMethod");
    }

    @Test
    void logAround_shouldReturnResult_andNotAlterIt() throws Throwable {
        Report report = Report.builder()
                .id("test-id")
                .title("Test")
                .type("PDF")
                .status("PENDING")
                .createdAt(LocalDateTime.now())
                .build();

        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn(report);

        Object result = loggingAspect.logAround(joinPoint);

        assertThat(result).isEqualTo(report);
        verify(joinPoint, times(1)).proceed();
    }

    @Test
    void logAround_shouldRethrowException() throws Throwable {
        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenThrow(new RuntimeException("service error"));

        assertThatThrownBy(() -> loggingAspect.logAround(joinPoint))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("service error");
    }

    @Test
    void logAround_shouldSerializeArgs_andNotFail() throws Throwable {
        ReportRequest request = ReportRequest.builder()
                .title("Monthly Report")
                .type("EXCEL")
                .build();

        when(joinPoint.getArgs()).thenReturn(new Object[]{request});
        when(joinPoint.proceed()).thenReturn(null);

        loggingAspect.logAround(joinPoint);

        verify(joinPoint).proceed();
    }

    @Test
    void logAround_shouldTrimLongOutput() throws Throwable {
        loggingProperties.setMaxMessageLength(10);

        String longValue = "x".repeat(200);
        Report report = Report.builder().id(longValue).title("t").type("PDF").status("PENDING").build();

        when(joinPoint.getArgs()).thenReturn(new Object[]{});
        when(joinPoint.proceed()).thenReturn(report);

        // Should not throw — trim is applied silently
        loggingAspect.logAround(joinPoint);
        verify(joinPoint).proceed();
    }

    @Test
    void logAround_shouldSkipSerializationOfHttpServletRequest() throws Throwable {
        jakarta.servlet.http.HttpServletRequest httpRequest =
                mock(jakarta.servlet.http.HttpServletRequest.class);

        when(joinPoint.getArgs()).thenReturn(new Object[]{httpRequest, "safe-arg"});
        when(joinPoint.proceed()).thenReturn(null);

        loggingAspect.logAround(joinPoint);

        verify(joinPoint).proceed();
    }

}
