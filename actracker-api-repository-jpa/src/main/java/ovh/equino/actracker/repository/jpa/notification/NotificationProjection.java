package ovh.equino.actracker.repository.jpa.notification;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.exception.ParseException;

record NotificationProjection(String data) {

    Notification<?> toNotification() {
        try {
            return Notification.fromJson(data);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
