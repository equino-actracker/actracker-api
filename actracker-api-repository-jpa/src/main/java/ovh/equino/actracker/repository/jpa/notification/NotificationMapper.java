package ovh.equino.actracker.repository.jpa.notification;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.exception.ParseException;

class NotificationMapper {

    NotificationMapper() {
    }

    Notification<?> toDto(NotificationEntity entity) {
        try {
            return Notification.fromJson(entity.data);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    NotificationEntity toEntity(Notification<?> dto) {
        try {
            NotificationEntity entity = new NotificationEntity();
            entity.id = dto.id().toString();
            entity.data = dto.toJson();
            entity.dataType = dto.notificationType().getCanonicalName();
            return entity;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
