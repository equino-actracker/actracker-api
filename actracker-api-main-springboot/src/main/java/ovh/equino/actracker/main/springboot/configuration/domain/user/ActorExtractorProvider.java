package ovh.equino.actracker.main.springboot.configuration.domain.user;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.spring.basic.identity.IdentityProvider;

@Configuration
class ActorExtractorProvider {

    @Bean
    ActorExtractor actorExtractor() {
        var identityProvider = new IdentityProvider();

        return () -> {
            var identity = identityProvider.provideIdentity();
            return new User(identity.getId());
        };
    }
}
