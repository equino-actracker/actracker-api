package ovh.equino.actracker.repository.jpa.notification;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.jpa.notification.NotificationEntity;
import ovh.equino.actracker.notification.outbox.NotificationRepository;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.isNull;

class JpaNotificationRepository extends JpaDAO implements NotificationRepository {

    JpaNotificationRepository(EntityManager entityManager) {
        super(entityManager);
    }

    private final NotificationMapper notificationMapper = new NotificationMapper();

    @Override
    public void save(Notification<?> notification) {
        NotificationEntity notificationEntity = notificationMapper.toEntity(notification);
        entityManager.merge(notificationEntity);
        entityManager.flush();  // TODO ?there is something wrong with transactional if this needs to be called?
    }

    @Override
    public Optional<Notification<?>> get(UUID notificationId) {
        NotificationEntity notificationEntity = entityManager.find(NotificationEntity.class, notificationId.toString());
        if (isNull(notificationEntity)) {
            return Optional.empty();
        }
        entityManager.refresh(notificationEntity); // TODO ?there is something wrong with transactional if this needs to be called?
        return Optional.of(notificationEntity)
                .map(notificationMapper::toDomainObject);
    }

    @Override
    public void delete(UUID notificationId) {
        NotificationEntity notification = entityManager.find(NotificationEntity.class, notificationId.toString());
        entityManager.remove(notification);
    }
}
