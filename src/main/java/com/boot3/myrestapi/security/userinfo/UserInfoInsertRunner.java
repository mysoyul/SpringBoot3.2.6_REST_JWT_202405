package com.boot3.myrestapi.security.userinfo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class UserInfoInsertRunner implements ApplicationRunner {
    @Autowired
    UserInfoRepository userInfoRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setName("adminboot");
        userInfo.setPassword(passwordEncoder.encode("pwd1"));
        userInfo.setEmail("admin@aa.com");
        userInfo.setRoles("ROLE_ADMIN,ROLE_USER");
        //userId에 UUID 값으로 저장
        userInfo.setUserId(UUID.randomUUID().toString());

        UserInfo userInfo2 = new UserInfo();
        userInfo2.setName("userboot");
        userInfo2.setPassword(passwordEncoder.encode("pwd2"));
        userInfo2.setEmail("user@aa.com");
        userInfo2.setRoles("ROLE_USER");
        //userId에 UUID 값으로 저장
        userInfo2.setUserId(UUID.randomUUID().toString());

        userInfoRepository.saveAll(List.of(userInfo, userInfo2));

    }
}