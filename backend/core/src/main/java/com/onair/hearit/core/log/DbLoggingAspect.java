package com.onair.hearit.core.log;

import com.onair.hearit.core.log.dbtrace.QueryExecutionContext;
import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.db.DbErrorLogProperty;
import com.onair.hearit.core.log.property.db.SlowQueryLogProperty;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class DbLoggingAspect {

    private static final long SLOW_QUERY_THRESHOLD_MS = 0;

    private final JsonLogger jsonLogger;

    @Pointcut("execution(* com.onair.hearit.core.infrastructure.jpa..*(..)) || " +
            "execution(* com.onair.hearit.core.infrastructure.jdbc..*(..))")
    public void repositoryMethod() {
    }

    @Around("repositoryMethod()")
    public Object logSlowQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        try {
            return joinPoint.proceed();
        } finally {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;

            if (executionTime > SLOW_QUERY_THRESHOLD_MS) {
                String query = QueryExecutionContext.getLastSql();
                String methodName = joinPoint.getSignature().getName();
                SlowQueryLogProperty slowQueryLogProperty = SlowQueryLogProperty.of(query, executionTime, methodName);
                jsonLogger.warn(slowQueryLogProperty);
                QueryExecutionContext.clear();
            }
        }
    }

    @AfterThrowing(pointcut = "repositoryMethod()", throwing = "ex")
    public void logDbError(JoinPoint joinPoint, Throwable ex) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String query = QueryExecutionContext.getLastSql();
            String exceptionName = ex.getClass().getSimpleName();
            String exceptionMessage = ex.getMessage();

            DbErrorLogProperty dbErrorLogProperty = DbErrorLogProperty.of(query, methodName, exceptionName,
                    exceptionMessage);
            jsonLogger.error(dbErrorLogProperty);
        } finally {
            QueryExecutionContext.clear();
        }
    }
}
