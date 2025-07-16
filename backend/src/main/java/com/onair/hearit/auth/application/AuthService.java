package com.onair.hearit.auth.application;

import com.onair.hearit.auth.Infrastructure.JwtTokenProvider;
import com.onair.hearit.auth.dto.request.LoginRequest;
import com.onair.hearit.auth.dto.request.SignupRequest;
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

    public TokenResponse login(LoginRequest request) {
        Member member = memberRepository.findByMemberId(request.memberId())
                .orElseThrow(() -> new UnauthorizedException("아이디나 비밀번호가 일치하지 않습니다."));

        if(!passwordEncoder.matches(request.password(), member.getPassword())) {
            throw new UnauthorizedException("아이디나 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtTokenProvider.createToken(member.getId(), member.getMemberRole());
        return new TokenResponse(token);
    }

    public void signup(SignupRequest request) {
        if (memberRepository.existsByMemberId(request.memberId())) {
            throw new InvalidInputException("이미 존재하는 아이디입니다.");
        }
        String hash = passwordEncoder.encode(request.password());
        memberRepository.save(Member.createUser(request.memberId(), request.nickname(), hash));
    }
}
