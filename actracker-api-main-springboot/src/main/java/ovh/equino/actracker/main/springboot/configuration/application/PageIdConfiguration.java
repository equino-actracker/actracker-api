package ovh.equino.actracker.main.springboot.configuration.application;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ovh.equino.actracker.application.PageIdTranslator;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;

import java.io.IOException;
import java.util.Base64;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

@Configuration
class PageIdConfiguration {

    // TODO extract to a separate class
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

        var customMappingModule = new SimpleModule();
        customMappingModule.addDeserializer(EntitySortCriteria.Field.class, new EntitySortCriteriaFieldDeserializer());
        customMappingModule.addSerializer(EntitySortCriteria.Field.class, new EntitySortCriteriaFieldSerializer());
        objectMapper.registerModules(customMappingModule);

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

    static private class EntitySortCriteriaFieldDeserializer extends StdDeserializer<EntitySortCriteria.Field> {

        private EntitySortCriteriaFieldDeserializer() {
            this(null);
        }

        private EntitySortCriteriaFieldDeserializer(Class<?> vc) {
            super(vc);
        }


        @Override
        public EntitySortCriteria.Field deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = p.readValueAsTree();
            var name = node.get("name").asText();
            var type = node.get("type").asText();
            try {

                Class<?> sortFieldType = Class.forName(type);
                Object[] enumConstants = sortFieldType.getEnumConstants();
                requireNonNull(enumConstants);
                return (EntitySortCriteria.Field) stream(enumConstants)
                        .filter(enumConstant -> enumConstant.toString().equals(name))
                        .findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("No enum constant found for " + name));


            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static private class EntitySortCriteriaFieldSerializer extends StdSerializer<EntitySortCriteria.Field> {

        private EntitySortCriteriaFieldSerializer() {
            this(null);
        }

        private EntitySortCriteriaFieldSerializer(Class<EntitySortCriteria.Field> t) {
            super(t);
        }

        @Override
        public void serialize(EntitySortCriteria.Field value,
                              JsonGenerator gen,
                              SerializerProvider provider) throws IOException {

            gen.writeStartObject();
            gen.writeStringField("name", value.toString());
            gen.writeStringField("type", value.getClass().getName());   // TODO - dengerous, replace with type registry [common: common, tag: tag...]
            gen.writeEndObject();
        }
    }
}
