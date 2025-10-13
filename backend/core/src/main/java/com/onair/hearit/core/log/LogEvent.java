package com.onair.hearit.core.log;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogEvent {

    /* api log event*/
    REQUEST("Api_Request"),
    RESPONSE("Api_Response"),
    EXCEPTION("Api_Exception"),

    /* auth log event */
    SIGNUP("Auth_Signup"),
    LOGIN("Auth_Login"),
    TOKEN_REFRESH("Auth_Token_Refresh"),
    WITHDRAWAL("Auth_Withdrawal"),

    /* db log event */
    DB_SLOW_QUERY("DB_SlowQuery"),
    DB_ERROR("DB_Error"),
    ;

    private final String eventName;
}
