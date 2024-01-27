package ovh.equino.actracker.main.springboot.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import ovh.equino.security.spring.basic.config.UserDetailsService;
import ovh.equino.security.spring.basic.crypto.BCryptPasswordEncoder;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
class SecurityConfiguration {

    SecurityConfiguration(AuthenticationManagerBuilder auth, UserDetailsService userDetailsService) throws Exception {

        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(new BCryptPasswordEncoder());
    }

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .authorizeHttpRequests()
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
//                .anyRequest().anonymous()
                .and()
                .httpBasic()
                .and()
                .csrf().disable()
                .cors()
                .and()
                .headers().frameOptions().disable() // for H2 console to work correctly
                .and()
                .sessionManagement().sessionCreationPolicy(STATELESS);

        return http.build();
    }
}
