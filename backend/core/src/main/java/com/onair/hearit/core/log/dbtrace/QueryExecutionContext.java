package com.onair.hearit.core.log.dbtrace;

/**
 * 현재 스레드에서 실행된 SQL 쿼리를 저장·조회하기 위한 유틸리티 클래스.
 *
 * <p>{@code QueryExecutionContext}는 Hibernate가 실행한 마지막 SQL 문장을
 * 스레드 단위로 임시 저장하기 위해 {@link ThreadLocal}을 사용한다. 이를 통해 AOP 기반 로깅이나 모니터링 로직이 Hibernate 내부에 직접 접근하지 않고도 실행된 쿼리 문자열을 추적할 수
 * 있다.
 *
 * <p>일반적인 사용 흐름:
 * <ul>
 *   <li>{@link #setLastSql(String)}: {@code HibernateQueryInspector}가 쿼리 실행 직전에 호출하여 SQL을 저장한다.</li>
 *   <li>{@link #getLastSql()}: 외부 로거나 AOP가 실행 완료 후 SQL 문자열을 조회한다.</li>
 *   <li>{@link #clear()}: 요청 처리 완료 후 반드시 호출하여 ThreadLocal에 남은 값을 제거한다.</li>
 * </ul>
 *
 * <p><b>스레드 안전성:</b> 각 스레드는 독립적으로 SQL 값을 저장하므로,
 * 스레드 간 공유하거나 병렬 접근해서는 안 된다.
 */
public class QueryExecutionContext {

    private QueryExecutionContext() {
        throw new IllegalStateException("Utility class");
    }

    private static final ThreadLocal<String> LAST_SQL = new ThreadLocal<>();

    public static void setLastSql(String sql) {
        LAST_SQL.set(sql);
    }

    public static String getLastSql() {
        return LAST_SQL.get();
    }

    public static void clear() {
        LAST_SQL.remove();
    }
}
