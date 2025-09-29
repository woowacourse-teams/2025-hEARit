package com.onair.hearit.app.auth.application;

import com.onair.hearit.app.exception.custom.InvalidInputException;
import com.onair.hearit.core.domain.OAuthProvider;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class OAuthServiceRegistry {

    private final Map<OAuthProvider, OAuthService> clients;

    public OAuthServiceRegistry(List<OAuthService> clients) {
        this.clients = clients.stream()
                .collect(Collectors.toUnmodifiableMap(OAuthService::provider, client -> client));
    }

    public OAuthService get(OAuthProvider provider) {
        OAuthService oAuthService = clients.get(provider);
        if (oAuthService == null) {
            throw new InvalidInputException("지원하지 않는 OAuth입니다. :" + provider.name());
        }
        return oAuthService;
    }
}
