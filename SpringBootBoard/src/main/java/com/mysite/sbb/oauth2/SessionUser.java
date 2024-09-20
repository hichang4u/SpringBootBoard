package com.mysite.sbb.oauth2;

import java.io.Serializable;

import com.mysite.sbb.user.SiteUser;

import lombok.Getter;

@Getter
public class SessionUser implements Serializable {

    private String username;
    private String email;
    private String password;

    public SessionUser(SiteUser user) {
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.password = user.getPassword();
    }
}