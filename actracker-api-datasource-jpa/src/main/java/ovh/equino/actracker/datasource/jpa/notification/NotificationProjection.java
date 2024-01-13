package ovh.equino.actracker.datasource.jpa.notification;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.exception.ParseException;

import java.util.UUID;

record NotificationProjection(String id, Long version, String dataType, String data) {

    Notification<?> toNotification() {
        try {
            Class<?> notificationType = Class.forName(dataType);
            Object deserializedData = Notification.fromJsonData(data, notificationType);
            return new Notification<>(UUID.fromString(id), version, deserializedData, notificationType);
        } catch (ParseException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
