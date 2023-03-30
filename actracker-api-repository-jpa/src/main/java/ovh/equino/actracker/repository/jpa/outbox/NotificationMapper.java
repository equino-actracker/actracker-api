package ovh.equino.actracker.repository.jpa.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ovh.equino.actracker.domain.Notification;

import java.util.UUID;

class NotificationMapper {

    private final ObjectMapper objectMapper;

    NotificationMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    Notification<?> toDto(NotificationEntity entity) {
        try {
            Object data = objectMapper.readValue(
                    entity.data,
                    Class.forName(entity.dataType)
            );
            return new Notification<>(UUID.fromString(entity.id), entity.version, data);
        } catch (JsonProcessingException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    NotificationEntity toEntity(Notification<?> dto) {
        try {
            NotificationEntity entity = new NotificationEntity();
            entity.id = dto.id().toString();
            entity.data = objectMapper.writeValueAsString(dto.data());
            entity.dataType = dto.notificationType().getCanonicalName();
            return entity;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
