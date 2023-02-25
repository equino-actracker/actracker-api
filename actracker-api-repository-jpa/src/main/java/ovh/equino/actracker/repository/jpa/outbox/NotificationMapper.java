package ovh.equino.actracker.repository.jpa.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import ovh.equino.actracker.notification.outbox.Notification;

import java.util.UUID;

class NotificationMapper {

    private final ObjectMapper objectMapper;

    NotificationMapper() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    Notification toDto(NotificationEntity entity) {
        try {
            Notification notification = objectMapper.readValue(entity.entity, Notification.class);
            return new Notification(UUID.fromString(entity.id), notification);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    NotificationEntity toEntity(Notification dto) {
        try {
            NotificationEntity entity = new NotificationEntity();
            entity.id = dto.id().toString();
            entity.entity = objectMapper.writeValueAsString(dto);
            return entity;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
