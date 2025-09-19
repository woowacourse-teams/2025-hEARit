package com.onair.hearit.auth.application;

import com.onair.hearit.auth.domain.OAuthProvider;
import com.onair.hearit.auth.domain.RefreshToken;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.OAuthLoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.response.LoginTokenResponse;
import com.onair.hearit.auth.dto.response.OAuthUserInfoResponse;
import com.onair.hearit.auth.infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.auth.infrastructure.repository.RefreshTokenRepository;
import com.onair.hearit.common.domain.Member;
import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.common.exception.custom.NotFoundException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.common.infrastructure.jpa.MemberRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OAuthServiceRegistry oAuthServiceRegistry;

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
        memberRepository.save(
                Member.createLocalUser(UUID.randomUUID().toString(), request.localId(), request.nickname(), hash,
                        defaultProfileImage));
    }

    private void validateDuplicatedId(SignupRequest request) {
        if (memberRepository.existsByLocalId(request.localId())) {
            throw new InvalidInputException("이미 존재하는 아이디입니다.");
        }
    }

    @Transactional
    public LoginTokenResponse loginOrSignUp(OAuthLoginRequest request, OAuthProvider provider) {
        OAuthService oAuthService = oAuthServiceRegistry.get(provider);
        OAuthUserInfoResponse userInfo = oAuthService.fetchUser(request.accessToken());
        Member member = memberRepository.findBySocialIdAndOAuthProvider(userInfo.id(), provider)
                .orElseGet(() -> signupWithUserInfo(userInfo, provider));
        LoginTokenResponse loginTokenResponse = createTokenResponseFrom(member);
        log.warn("memberId:{}가 {} 로그인 성공", member.getId(), provider.name());
        return loginTokenResponse;
    }

    private Member signupWithUserInfo(OAuthUserInfoResponse userInfo, OAuthProvider provider) {
        Member member = Member.createSocialUser(
                UUID.randomUUID().toString(), userInfo.id(), userInfo.nickname(), userInfo.profileImageUrl(), provider);
        return memberRepository.save(member);
    }

    private LoginTokenResponse createTokenResponseFrom(Member member) {
        String accessToken = jwtTokenProvider.createAccessToken(member.getId());
        String refreshToken = jwtTokenProvider.createRefreshToken(member.getId());
        saveOrUpdateRefreshToken(member, refreshToken);
        return new LoginTokenResponse(accessToken, refreshToken);
    }

    private void saveOrUpdateRefreshToken(Member member, String refreshToken) {
        LocalDateTime expiryDate = jwtTokenProvider.extractExpiry(refreshToken);
        refreshTokenRepository.findByMemberId(member.getId())
                .ifPresentOrElse(
                        existing -> existing.update(refreshToken, expiryDate),
                        () -> refreshTokenRepository.save(new RefreshToken(member.getId(), refreshToken, expiryDate))
                );
    }

    public String reissue(String refreshToken) {
        validateRefreshTokenExpired(refreshToken);
        Long memberId = jwtTokenProvider.getMemberId(refreshToken);
        log.info("memberId:{}가 reissue 요청 수신", memberId);
        validateRefreshToken(refreshToken, memberId);
        String newAccessToken = jwtTokenProvider.createAccessToken(memberId);
        log.info("memberId:{}의 token reissue 성공", memberId);
        return newAccessToken;
    }

    private void validateRefreshTokenExpired(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new UnauthorizedException("만료된 리프레시토큰입니다.");
        }
    }

    private void validateRefreshToken(String refreshToken, Long memberId) {
        RefreshToken stored = refreshTokenRepository.findByMemberId(memberId)
                .orElseThrow(() -> {
                    log.warn("memberId={} 저장된 리프레시토큰 없음", memberId);
                    return new UnauthorizedException("저장된 토큰이 없습니다.");
                });

        if (!stored.getToken().equals(refreshToken)) {
            log.warn("memberId={} 저장된 토큰과 요청 토큰 불일치", memberId);
            throw new UnauthorizedException("리프레시 토큰이 불일치합니다.");
        }
    }

    @Transactional
    public void withdraw(Long memberId) {
        refreshTokenRepository.deleteByMemberId(memberId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("memberId", memberId.toString()));
        member.withdraw();
    }
}
