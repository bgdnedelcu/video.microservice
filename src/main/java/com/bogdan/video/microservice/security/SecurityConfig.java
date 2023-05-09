package com.bogdan.video.microservice.security;

import com.bogdan.video.microservice.filter.MyAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public InMemoryUserDetailsManager userDetailsService() {
        UserDetails user = User.withUsername("admin")
                .password("admin")
                .roles("admin")
                .build();
        return new InMemoryUserDetailsManager(user);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable();
        http.httpBasic().disable();
        http.formLogin().disable();
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.authorizeHttpRequests()
                .requestMatchers("/videoplatform/api/video/play/**").permitAll()
                .requestMatchers("/videoplatform/api/video/commentsByVideoId/**").permitAll()
                .requestMatchers("/videoplatform/api/video/getVideoDetails/**").permitAll()
                .requestMatchers("/videoplatform/api/video/checkVideoId/**").permitAll()
                .requestMatchers("/videoplatform/api/video/videoDetailsForNonUsers/**").permitAll()
                .anyRequest().authenticated();
        http.addFilterBefore(new MyAuthorizationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

}