package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.Notification;

import java.util.List;

public interface NotificationDataSource {

    List<Notification<?>> getPage(int limit);
}
