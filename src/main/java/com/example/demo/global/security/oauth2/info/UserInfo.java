package com.example.demo.global.security.oauth2.info;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfo {

    private String name;
    private String email;

    public UserInfo(String name, String email) {
        this.name = name;
        this.email = email;
    }

}