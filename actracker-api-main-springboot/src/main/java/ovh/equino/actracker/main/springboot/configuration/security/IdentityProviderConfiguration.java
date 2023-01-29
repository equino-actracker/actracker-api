package ovh.equino.actracker.main.springboot.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ovh.equino.security.spring.basic.identity.IdentityProvider;

@Configuration
class IdentityProviderConfiguration {

    @Bean
    IdentityProvider identityProvider() {
        return new IdentityProvider();
    }
}
