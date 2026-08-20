package com.sagar.jwtsecurity.filter;

import com.sagar.jwtsecurity.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private JwtUtil jwtUtil;
    private UserDetailsService userDetailsService;
    public  JwtFilter(JwtUtil jwtUtil,UserDetailsService userDetailsService){
        this.jwtUtil=jwtUtil;
        this.userDetailsService=userDetailsService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //getting authroisation header
        String authorizationHeader=request.getHeader("Authorization");


        //extracting token from it
        String token = jwtUtil.extractToken(authorizationHeader);


        //if token not there pass down stream
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        //if token present fetching that user data from db everytime so we have control over changed authoroties
        //we can store all neccessary data in jwt only if nedded

        try {

            String username =
                    jwtUtil.extractUsername(token);

            UserDetails userDetails =
                    userDetailsService.loadUserByUsername(username);


            //isValid checks token username and fetched username same or not then checks is validity expired or not
            if (jwtUtil.isTokenValid(
                    token,
                    userDetails.getUsername())) {

                //creating Authenticated authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                //adding Authenticated authentication object to security context so dowmstream can access it
                SecurityContextHolder
                        .getContext()
                        .setAuthentication(authentication);
            }

        } catch (Exception e) {
            //invalid jwt so not creating authentication just moving downstream
        }

        filterChain.doFilter(request, response);



    }
}
