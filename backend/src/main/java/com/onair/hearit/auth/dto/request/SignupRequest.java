package com.onair.hearit.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SignupRequest(
        String localId,
        String nickname,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {
}
