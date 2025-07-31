package com.onair.hearit.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequest(
        String localId,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        String password
) {
}
