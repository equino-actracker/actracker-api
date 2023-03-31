package ovh.equino.actracker.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ovh.equino.actracker.domain.exception.ParseException;

import java.util.UUID;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.Objects.requireNonNull;

public record Notification<T>(
        UUID id,
        long version,
        T data,
        Class<?> notificationType) {

    public Notification(UUID id, long version, T data) {
        this(id, version, data, data.getClass());
    }

    public Notification(UUID id, T data) {
        // Version must be not null, otherwise H2 fails
        this(id, 0L, data);
    }

    public Notification {
        requireNonNull(id);
        requireNonNull(data);
        requireNonNull(notificationType);
    }

    public String toJson() throws ParseException {
        try {
            return objectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new ParseException(e);
        }
    }

    public static Notification<?> fromJson(String json) throws ParseException {
        try {
            Class<?> notificationType = getNotificationType(json);
            JavaType javaType = objectMapper().getTypeFactory().constructParametricType(
                    Notification.class,
                    notificationType
            );

            return objectMapper().readValue(json, javaType);
        } catch (JsonProcessingException e) {
            throw new ParseException(e);
        }
    }

    private static Class<?> getNotificationType(String rawMessage) throws JsonProcessingException {
        Notification<?> notification = objectMapper().readValue(rawMessage, Notification.class);
        return notification.notificationType();
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
        return objectMapper;
    }
}
