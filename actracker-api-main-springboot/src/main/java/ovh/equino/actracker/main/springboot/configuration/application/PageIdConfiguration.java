package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ovh.equino.actracker.application.PageIdTranslator;

@Configuration
class PageIdConfiguration {

    @Bean
    PageIdTranslator pageIdTranslator() {
        return new Base64JacksonPageIdTranslator();
    }
}
