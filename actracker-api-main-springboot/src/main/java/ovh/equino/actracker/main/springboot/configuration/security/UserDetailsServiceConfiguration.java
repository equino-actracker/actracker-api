package ovh.equino.actracker.main.springboot.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ovh.equino.security.spring.basic.config.UserDetailsService;
import ovh.equino.security.spring.basic.identity.IdentityRepository;

@Configuration
class UserDetailsServiceConfiguration {

    @Bean
    UserDetailsService userDetailsService(IdentityRepository identityRepository) {
        return new UserDetailsService(identityRepository);
    }
}
