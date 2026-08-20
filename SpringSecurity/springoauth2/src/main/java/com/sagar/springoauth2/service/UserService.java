package com.sagar.springoauth2.service;

import com.sagar.springoauth2.entity.User;
import com.sagar.springoauth2.repository.UserRepository;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }



    public void registerOrUpdate(String provider, OidcUser oidcUser) {
        String subject = oidcUser.getSubject();

        Optional<User> user=userRepository.findByProviderAndProviderSubject(provider,subject);

        if(user.isPresent()){
            User existingUser=user.get();
            existingUser.setName(oidcUser.getName());
            existingUser.setEmail(oidcUser.getEmail());


        }
        else{
            User newUser=new User();
            newUser.setName(oidcUser.getName());
            newUser.setEmail(oidcUser.getEmail());
            newUser.setProvider(provider);
            newUser.setProviderSubject(subject);
            userRepository.save(newUser);

        }


    }
}
