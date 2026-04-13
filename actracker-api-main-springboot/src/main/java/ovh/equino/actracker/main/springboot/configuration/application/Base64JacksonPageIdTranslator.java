package ovh.equino.actracker.main.springboot.configuration.application;

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
import ovh.equino.actracker.application.PageIdTranslator;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.activity.ActivitySearchCriteria;
import ovh.equino.actracker.domain.dashboard.DashboardSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;

import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static java.util.Arrays.stream;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;

class Base64JacksonPageIdTranslator implements PageIdTranslator {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Base64.Encoder base64Encoder = Base64.getEncoder();
    private final Base64.Decoder base64Decoder = Base64.getDecoder();

    Base64JacksonPageIdTranslator() {
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

            var pageableFieldAlias = PageableFieldAlias.fromString(type)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown pageable field type %s".formatted(type)));
            return pageableFieldAlias.findField(name)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No pageable field name %s found in type %s".formatted(name, pageableFieldAlias.fieldType)
                    ));
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
        public void serialize(EntitySortCriteria.Field field,
                              JsonGenerator gen,
                              SerializerProvider provider) throws IOException {

            var pageableFieldAlias = PageableFieldAlias.fromClass(field.getClass())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown pageable field type %s".formatted(field.getClass())
                    ));
            gen.writeStartObject();
            gen.writeStringField("name", field.toString());
            gen.writeStringField("type", pageableFieldAlias.toString());
            gen.writeEndObject();
        }
    }

    private enum PageableFieldAlias {
        COMMON(EntitySortCriteria.CommonField.class, EntitySortCriteria.CommonField.values()),
        ACTIVITY(ActivitySearchCriteria.SortableField.class, ActivitySearchCriteria.SortableField.values()),
        DASHBOARD(DashboardSearchCriteria.SortableField.class, DashboardSearchCriteria.SortableField.values()),
        TAG(TagSearchCriteria.SortableField.class, TagSearchCriteria.SortableField.values()),
        TAG_SET(TagSetSearchCriteria.SortableField.class, TagSetSearchCriteria.SortableField.values());

        private final Class<? extends EntitySortCriteria.Field> fieldType;
        private final EntitySortCriteria.Field[] fields;

        PageableFieldAlias(Class<? extends EntitySortCriteria.Field> fieldType, EntitySortCriteria.Field[] fields) {
            this.fieldType = fieldType;
            this.fields = fields;
        }

        private static Optional<PageableFieldAlias> fromClass(Class<? extends EntitySortCriteria.Field> type) {
            return stream(values())
                    .filter(value -> value.fieldType.equals(type))
                    .findAny();
        }

        private static Optional<PageableFieldAlias> fromString(String type) {
            return stream(values())
                    .filter(value -> value.toString().equals(type))
                    .findAny();
        }

        private Optional<EntitySortCriteria.Field> findField(String fieldName) {
            return stream(fields)
                    .filter(field -> field.toString().equals(fieldName))
                    .findAny();
        }
    }
}
