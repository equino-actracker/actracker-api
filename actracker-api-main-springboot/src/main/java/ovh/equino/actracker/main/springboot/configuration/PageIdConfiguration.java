package ovh.equino.actracker.main.springboot.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.PageIdTranslator;

import java.util.Base64;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Configuration
class PageIdConfiguration {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();

    @Bean
    PageIdTranslator pageIdTranslator() {
        return new PageIdTranslator() {
            @Override
            public String toString(EntitySearchPageId pageId) {
                if(isNull(pageId)) {
                    return null;
                }
                try {
                    var json = objectMapper.writeValueAsString(pageId);
//                    return base64Encoder.encodeToString(json.getBytes());
                    return json;
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public EntitySearchPageId fromString(String pageId) {
                if(isEmpty(pageId)) {
                    return EntitySearchPageId.firstPage();
                }
                try {
                    var entitySearchPageId = objectMapper.readValue(pageId, EntitySearchPageId.class);
                    return entitySearchPageId;
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
