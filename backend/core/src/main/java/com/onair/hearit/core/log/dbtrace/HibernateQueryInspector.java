package com.onair.hearit.core.log.dbtrace;

import org.hibernate.resource.jdbc.spi.StatementInspector;

public class HibernateQueryInspector implements StatementInspector {

    @Override
    public String inspect(String sql) {
        QueryExecutionContext.setLastSql(sql);
        return sql;
    }
}
