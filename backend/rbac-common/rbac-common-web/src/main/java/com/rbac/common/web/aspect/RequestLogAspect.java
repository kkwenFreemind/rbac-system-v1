package com.rbac.common.web.aspect;

import com.rbac.common.web.context.TraceContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Request logging aspect for controller methods.
 * Logs request details, execution time, and response status.
 * Integrates with TraceContext for distributed tracing.
 */
@Aspect
@Component
@Slf4j
public class RequestLogAspect {

    /**
     * Pointcut for all controller methods in the application.
     * Targets methods within classes annotated with @RestController.
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void controllerMethods() {
        // Pointcut definition
    }

    /**
     * Around advice for logging controller method execution.
     * Logs request start, execution time, and completion.
     *
     * @param joinPoint the join point
     * @return the method result
     * @throws Throwable if the method throws an exception
     */
    @Around("controllerMethods()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // Get request details
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        String method = request != null ? request.getMethod() : "UNKNOWN";
        String uri = request != null ? request.getRequestURI() : "UNKNOWN";
        String traceId = TraceContext.getTraceId();

        // Log request start
        log.info("Request started - Method: {}, URI: {}, TraceId: {}", method, uri, traceId);

        try {
            // Proceed with the method execution
            Object result = joinPoint.proceed();

            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Log successful completion
            log.info("Request completed - Method: {}, URI: {}, ExecutionTime: {}ms, TraceId: {}",
                    method, uri, executionTime, traceId);

            return result;

        } catch (Exception e) {
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Log error
            log.error("Request failed - Method: {}, URI: {}, ExecutionTime: {}ms, TraceId: {}, Error: {}",
                    method, uri, executionTime, traceId, e.getMessage(), e);

            throw e;
        }
    }
}