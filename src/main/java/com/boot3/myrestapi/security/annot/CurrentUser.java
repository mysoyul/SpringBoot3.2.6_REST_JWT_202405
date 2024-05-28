package com.boot3.myrestapi.security.annot;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/*
    @AuthenticationPrincipal 은 인증토큰에서 UserDetails 객체를 반환해주는 역할을 하는 어노테이션
    UserDetails 하위 객체인 UserInfoUserDetails 에서 UserInfo 객체를 꺼내는 역할을 하는 어노테이션
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@AuthenticationPrincipal(expression = "#this == 'anonymousUser' ? null : userInfo")
public @interface CurrentUser {
}
