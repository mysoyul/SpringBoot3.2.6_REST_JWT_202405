package com.boot3.myrestapi.security.userinfo;

import com.boot3.myrestapi.security.jwt.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserInfoController {
    @Autowired
    private UserInfoRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtService jwtService;

    @PostMapping("/login")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                ));
        //인증 성공
        if (authentication.isAuthenticated()) {
            //return jwtService.generateToken(authRequest.getEmail());
            UserInfo userInfo = repository.findByEmail(authRequest.getEmail())
                    .orElseThrow(() -> new UsernameNotFoundException("user not found " + authRequest.getEmail()));
            return jwtService.generateTokenUserId(userInfo.getUserId());
        } else {
            throw new UsernameNotFoundException("Invalid user request !");
        }
    }
    @PostMapping("/new")
    public String addNewUser(@RequestBody UserInfo userInfo){
        //userId에 UUID 값으로 저장
        userInfo.setUserId(UUID.randomUUID().toString());
        userInfo.setPassword(passwordEncoder.encode(userInfo.getPassword()));
        UserInfo savedUserInfo = repository.save(userInfo);
        return savedUserInfo.getName() + " user added!!";
    }

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }
}