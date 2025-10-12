package com.onair.hearit.core.log.dto;

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
    LOGOUT("Auth_Logout"),
    REFRESH_TOKEN_EXPIRED("Auth_RefreshTokenExpired"),
    WITHDRAWAL("Auth_Withdrawal"),

    /* db log event */
    DB_SLOW_QUERY("DB_SlowQuery"),
    DB_ERROR("DB_Error"),
    ;

    private final String eventName;
}
