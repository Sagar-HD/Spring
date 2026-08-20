package com.sagar.springoauth2.controller;


import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class ProfileController {
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal OidcUser oidcUser){
        return "HEY UR ARE AUTHENTICATED"+oidcUser;
    }
}
