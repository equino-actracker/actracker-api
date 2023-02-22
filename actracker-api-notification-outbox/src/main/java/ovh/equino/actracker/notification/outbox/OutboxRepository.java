package ovh.equino.actracker.notification.outbox;

import java.util.List;

public interface OutboxRepository {

    void save(Notification notification);

    List<Notification> getPage(int limit);
}
