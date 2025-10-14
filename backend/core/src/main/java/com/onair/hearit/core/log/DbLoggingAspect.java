package com.onair.hearit.core.log;

import com.onair.hearit.core.log.dbtrace.QueryExecutionContext;
import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.db.DbErrorLogProperty;
import com.onair.hearit.core.log.property.db.SlowQueryLogProperty;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class DbLoggingAspect {

    private static final long SLOW_QUERY_THRESHOLD_MS = 500;

    private final JsonLogger jsonLogger;

    @Pointcut("execution(* com.onair.hearit.core.infrastructure.jpa..*(..)) || " +
            "execution(* com.onair.hearit.core.infrastructure.jdbc..*(..))")
    public void repositoryMethod() {
    }

    @Around("repositoryMethod()")
    public Object logQuery(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();
        Throwable caughtException = null;

        try {
            return joinPoint.proceed();
        } catch (Throwable ex) {
            caughtException = ex;
            throw ex;
        } finally {
            try {
                long executionTime = System.currentTimeMillis() - startTime;
                String query = QueryExecutionContext.getLastSql();

                // 예외 발생 시 에러 로깅
                if (caughtException != null) {
                    logDbError(query, methodName, caughtException);
                }
                // Slow Query 로깅 (예외가 있어도 실행 시간은 기록)
                else if (executionTime > SLOW_QUERY_THRESHOLD_MS) {
                    logSlowQuery(query, methodName, executionTime);
                }
            } catch (Exception loggingException) {
                jsonLogger.warn(loggingException.getMessage(), loggingException);
            } finally {
                QueryExecutionContext.clear();
            }
        }
    }

    public void logDbError(String query, String methodName, Throwable ex) {
        try {
            String exceptionName = ex.getClass().getSimpleName();
            String exceptionMessage = ex.getMessage();

            DbErrorLogProperty dbErrorLogProperty = DbErrorLogProperty.of(query, methodName, exceptionName,
                    exceptionMessage);
            jsonLogger.error(dbErrorLogProperty);
        } finally {
            QueryExecutionContext.clear();
        }
    }

    public void logSlowQuery(String query, String methodName, long executionTime) {
        try {
            SlowQueryLogProperty slowQueryLogProperty =
                    SlowQueryLogProperty.of(query, executionTime, methodName);
            jsonLogger.warn(slowQueryLogProperty);
        } catch (Exception e) {
            jsonLogger.error("Slow Query 로깅 실패", e);
        }
    }
}
