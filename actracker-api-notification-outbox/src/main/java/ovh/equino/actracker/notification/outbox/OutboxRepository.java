package ovh.equino.actracker.notification.outbox;

public interface OutboxRepository {

    void save(Notification notification);
}
