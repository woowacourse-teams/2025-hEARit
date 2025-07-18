package com.onair.hearit.auth.application;

import com.onair.hearit.auth.Infrastructure.client.KakaoUserInfoClient;
import com.onair.hearit.auth.Infrastructure.jwt.JwtTokenProvider;
import com.onair.hearit.auth.dto.request.KakaoLoginRequest;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
import com.onair.hearit.auth.dto.response.KakaoUserInfoResponse;
import com.onair.hearit.auth.dto.response.TokenResponse;
import com.onair.hearit.common.exception.custom.InvalidInputException;
import com.onair.hearit.common.exception.custom.UnauthorizedException;
import com.onair.hearit.domain.Member;
import com.onair.hearit.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final KakaoUserInfoClient kakaoUserInfoClient;

    public TokenResponse login(LoginRequest request) {
        Member member = getMemberByLocalId(request.localId());
        validatePassword(request, member);
        return createTokenResponseFrom(member);
    }

    private Member getMemberByLocalId(String localId) {
        return memberRepository.findByLocalId(localId)
                .orElseThrow(() -> new UnauthorizedException("아이디나 비밀번호가 일치하지 않습니다."));
    }

    private void validatePassword(LoginRequest request, Member member) {
        if(!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new UnauthorizedException("아이디나 비밀번호가 일치하지 않습니다.");
        }
    }

    public void signup(SignupRequest request) {
        validateDuplicatedId(request);
        String hash = passwordEncoder.encode(request.password());
        memberRepository.save(Member.createLocalUser(request.localId(), request.nickname(), hash));
    }

    private void validateDuplicatedId(SignupRequest request) {
        if (memberRepository.existsByLocalId(request.localId())) {
            throw new InvalidInputException("이미 존재하는 아이디입니다.");
        }
    }

    // 소셜 로그인의 경우 회원가입이 따로 없으며 로그인 시 자동으로 회원가입되도록 구현함
    public TokenResponse loginWithKakao(KakaoLoginRequest request) {
        KakaoUserInfoResponse kakaoUser = kakaoUserInfoClient.getUserInfo(request.accessToken());

        Member member = memberRepository.findBySocialId(kakaoUser.id())
                .orElseGet(() -> signupWithKakao(kakaoUser));
        return createTokenResponseFrom(member);
    }

    private Member signupWithKakao(KakaoUserInfoResponse kakaoUser) {
        Member member = Member.createSocialUser(kakaoUser.id(), kakaoUser.nickname());
        return memberRepository.save(member);
    }

    private TokenResponse createTokenResponseFrom(Member member) {
        String token = jwtTokenProvider.createToken(member.getId());
        return new TokenResponse(token);
    }
}
