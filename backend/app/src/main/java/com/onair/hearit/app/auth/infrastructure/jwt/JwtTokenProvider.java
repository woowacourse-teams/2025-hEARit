package com.onair.hearit.app.auth.infrastructure.jwt;

import com.onair.hearit.core.log.logger.JsonLogger;
import com.onair.hearit.core.log.property.auth.TokenRefreshLogProperty;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    private final JsonLogger jsonLogger;

    private final String secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token.expiration}") long accessTokenExpiration,
            @Value("${jwt.refresh-token.expiration}") long refreshTokenExpiration,
            JsonLogger jsonLogger) {
        this.secretKey = secretKey;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.jsonLogger = jsonLogger;
    }

    public String createAccessToken(UUID memberUuid) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(memberUuid.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public String createRefreshToken(UUID memberUuid) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshTokenExpiration);

        return Jwts.builder()
                .setSubject(memberUuid.toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public LocalDateTime extractExpiry(String token) {
        Claims claims = parseClaims(token);
        return claims.getExpiration()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            UUID memberUuid = extractUuidFromToken(e);
            jsonLogger.warn(TokenRefreshLogProperty.failure(memberUuid, "토큰이 만료되었습니다."));
            return false;
        } catch (JwtException e) {
            jsonLogger.warn(TokenRefreshLogProperty.failure(null, "토큰이 유효하지 않습니다."));
            return false;
        }
    }

    public TokenStatus getTokenStatus(String token) {
        if (token == null || token.isBlank()) {
            return TokenStatus.NOT_EXIST;
        }

        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            if (expiration.before(new Date())) {
                return TokenStatus.EXPIRED;
            }
            return TokenStatus.VALID;
        } catch (ExpiredJwtException e) {
            UUID memberUuid = extractUuidFromToken(e);
            jsonLogger.warn(TokenRefreshLogProperty.failure(memberUuid, "토큰이 만료되었습니다."));
            return TokenStatus.EXPIRED;
        } catch (JwtException e) {
            jsonLogger.warn(TokenRefreshLogProperty.failure(null, "토큰이 유효하지 않습니다."));
            return TokenStatus.INVALID;
        }
    }

    public UUID getMemberUuid(String token) {
        try {
            return UUID.fromString(parseClaims(token).getSubject());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format in token subject (legacy token?)");
            throw new JwtException("유효하지 않은 토큰 형식입니다.");
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes(StandardCharsets.UTF_8))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private UUID extractUuidFromToken(ExpiredJwtException e) {
        try {
            String subject = e.getClaims().getSubject();
            if (subject != null) {
                return UUID.fromString(subject);
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}
