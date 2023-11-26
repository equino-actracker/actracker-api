package ovh.equino.actracker.repository.jpa.notification;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.exception.ParseException;

import java.util.UUID;

class NotificationMapper {

    NotificationMapper() {
    }

    Notification<?> toDto(NotificationEntity entity) {
        try {
            Class<?> notificationType = Class.forName(entity.dataType);
            Object data = Notification.fromJsonData(entity.data, notificationType);
            return new Notification<>(UUID.fromString(entity.id), entity.version, data, notificationType);
        } catch (ParseException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    NotificationEntity toEntity(Notification<?> dto) {
        try {
            NotificationEntity entity = new NotificationEntity();
            entity.id = dto.id().toString();
            entity.data = dto.toJsonData();
            entity.dataType = dto.notificationType().getCanonicalName();
            return entity;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
