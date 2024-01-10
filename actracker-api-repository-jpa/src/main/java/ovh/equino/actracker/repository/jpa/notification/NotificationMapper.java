package ovh.equino.actracker.repository.jpa.notification;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.exception.ParseException;
import ovh.equino.actracker.jpa.notification.NotificationEntity;

import java.util.UUID;

class NotificationMapper {

    NotificationMapper() {
    }

    Notification<?> toDomainObject(NotificationEntity entity) {
        try {
            Class<?> notificationType = Class.forName(entity.getDataType());
            Object data = Notification.fromJsonData(entity.getData(), notificationType);
            return new Notification<>(UUID.fromString(entity.getId()), entity.getVersion(), data, notificationType);
        } catch (ParseException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    NotificationEntity toEntity(Notification<?> dto) {
        try {
            NotificationEntity entity = new NotificationEntity();
            entity.setId(dto.id().toString());
            entity.setData(dto.toJsonData());
            entity.setDataType(dto.notificationType().getCanonicalName());
            return entity;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
