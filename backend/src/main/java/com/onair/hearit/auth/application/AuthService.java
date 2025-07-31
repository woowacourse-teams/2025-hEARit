package com.onair.hearit.auth.application;

import com.onair.hearit.auth.dto.request.KakaoLoginRequest;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.response.KakaoUserInfoResponse;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.infrastructure.client.KakaoUserInfoClient;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.auth.infrastructure.jwt.RefreshToken;
import com.onair.hearit.auth.infrastructure.jwt.RefreshTokenRepository;
import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.domain.Member;
import com.onair.hearit.infrastructure.MemberRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoUserInfoClient kakaoUserInfoClient;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${hearit.profile.default-image-url}")
    private String defaultProfileImage;

    @Transactional
    public LoginTokenResponse login(LoginRequest request) {
        Member member = getMemberByLocalId(request.localId());
        validatePassword(request, member);
        return createTokenResponseFrom(member);
    }

    private Member getMemberByLocalId(String localId) {
        return memberRepository.findByLocalId(localId)
                .orElseThrow(() -> new UnauthorizedException("아이디나 비밀번호가 일치하지 않습니다."));
    }

    private void validatePassword(LoginRequest request, Member member) {
        if (!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new UnauthorizedException("아이디나 비밀번호가 일치하지 않습니다.");
        }
    }

    public void signup(SignupRequest request) {
        validateDuplicatedId(request);
        String hash = passwordEncoder.encode(request.password());
        memberRepository.save(Member.createLocalUser(request.localId(), request.nickname(), hash, defaultProfileImage));
    }

    private void validateDuplicatedId(SignupRequest request) {
        if (memberRepository.existsByLocalId(request.localId())) {
            throw new InvalidInputException("이미 존재하는 아이디입니다.");
        }
    }

    // 소셜 로그인의 경우 회원가입이 따로 없으며 로그인 시 자동으로 회원가입되도록 구현
    @Transactional
    public LoginTokenResponse loginWithKakao(KakaoLoginRequest request) {
        KakaoUserInfoResponse kakaoUser = kakaoUserInfoClient.getUserInfo(request.accessToken());
        Member member = memberRepository.findBySocialId(kakaoUser.id())
                .orElseGet(() -> signupWithKakao(kakaoUser));
        return createTokenResponseFrom(member);
    }

    private Member signupWithKakao(KakaoUserInfoResponse kakaoUser) {
        Member member = Member.createSocialUser(kakaoUser.id(), kakaoUser.nickname(), kakaoUser.profileImage());
        return memberRepository.save(member);
    }

    private LoginTokenResponse createTokenResponseFrom(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        LocalDateTime expiryDate = jwtTokenProvider.extractExpiry(refreshToken);
        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        existing -> existing.update(refreshToken, expiryDate),
                        () -> refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken, expiryDate))
                );
        log.info("로그인 토큰 발급 완료 - memberId: {}", member.getId());
        return new LoginTokenResponse(accessToken, refreshToken);
    }

    public String reissue(String refreshToken) {
        validateRefreshTokenExpired(refreshToken);
        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        RefreshToken stored = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> {
                    log.warn("토큰 재발급 실패 - 저장된 리프레시 토큰 없음");
                    return new UnauthorizedException("저장된 토큰이 없습니다.");
                });
        validateRefreshTokenValue(refreshToken, stored);
        return jwtTokenProvider.createAccessToken(memberId);
    }

    private void validateRefreshTokenExpired(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.warn("리프레시토큰 검증 실패 - 만료된 토큰");
            throw new UnauthorizedException("만료된 리프레시토큰입니다.");
        }
    }

    private void validateRefreshTokenValue(String refreshToken, RefreshToken stored) {
        if (!stored.getToken().equals(refreshToken)) {
            log.warn("리프레시토큰 검증 실패 - 기존 토큰과 불일치");
            throw new UnauthorizedException("리프레시 토큰이 불일치합니다.");
        }
    }

    public void logout(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
    }
}
