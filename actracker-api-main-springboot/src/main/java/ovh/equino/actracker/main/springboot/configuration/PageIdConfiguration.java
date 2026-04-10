package ovh.equino.actracker.main.springboot.configuration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.PageIdTranslator;

import java.util.Base64;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Configuration
class PageIdConfiguration {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();

    PageIdConfiguration() {
        objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(ANY)
                .withGetterVisibility(NONE)
                .withIsGetterVisibility(NONE)
                .withSetterVisibility(NONE)
                .withCreatorVisibility(NONE)
        );
        objectMapper.setVisibility(objectMapper.getDeserializationConfig().getDefaultVisibilityChecker()
                .withFieldVisibility(ANY)
                .withGetterVisibility(NONE)
                .withIsGetterVisibility(NONE)
                .withSetterVisibility(NONE)
                .withCreatorVisibility(NONE)
        );
    }

    @Bean
    PageIdTranslator pageIdTranslator() {
        return new PageIdTranslator() {
            @Override
            public String toString(EntitySearchPageId pageId) {
                if (isNull(pageId)) {
                    return null;
                }
                try {
                    var serializedPageId = objectMapper.writeValueAsString(pageId);
                    return base64Encoder.encodeToString(serializedPageId.getBytes());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public EntitySearchPageId fromString(String pageId) {
                if (isEmpty(pageId)) {
                    return EntitySearchPageId.firstPage();
                }
                try {
                    var decodedPageId = new String(base64Decoder.decode(pageId));
                    return objectMapper.readValue(decodedPageId, EntitySearchPageId.class);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
