package com.sagar.springoauth2.security;

import com.sagar.springoauth2.service.UserService;
import org.jspecify.annotations.Nullable;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private UserService userService;
    private final OidcUserService oidcUserService=new OidcUserService();

    public CustomOidcUserService(UserService userService){
        this.userService=userService;
    }



    @Override
    public @Nullable OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser=oidcUserService.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        //saving that user in our db if exist updating
         userService.registerOrUpdate(provider, oidcUser);
         return oidcUser;

    }
}
