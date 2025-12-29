package com.onair.hearit.app.userinfo.application;

import com.onair.hearit.core.domain.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoService {

    public String getUuid(UserInfo userInfo) {
        return userInfo.getUuid().toString();
    }
}
