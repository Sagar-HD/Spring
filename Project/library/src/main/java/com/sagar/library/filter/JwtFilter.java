package com.sagar.library.filter;

import com.sagar.library.repository.UserRepository;
import com.sagar.library.security.CustomUserDetails;
import com.sagar.library.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
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

    public JwtFilter(JwtUtil jwtUtil,UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService=userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authenticationHeader=request.getHeader("Authorization");
        String token= jwtUtil.extractToken(authenticationHeader);


      if(token==null){
          filterChain.doFilter(request,response);
          return;
      }
      try{
          String userName= jwtUtil.extractUsername(token);
          CustomUserDetails user =
                  (CustomUserDetails) userDetailsService.loadUserByUsername(userName);



          if(jwtUtil.isTokenValid(token,user.getUsername())) {

              UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());

              SecurityContextHolder.getContext().setAuthentication(authenticationToken);

          }


      }
      catch (Exception e){
        //invalid jwt

      }

        filterChain.doFilter(request,response);
    }
}
