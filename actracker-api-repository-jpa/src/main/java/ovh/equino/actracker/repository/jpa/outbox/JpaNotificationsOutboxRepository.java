package ovh.equino.actracker.repository.jpa.outbox;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxRepository;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

class JpaNotificationsOutboxRepository extends JpaDAO implements NotificationsOutboxRepository {

    private final NotificationMapper notificationMapper = new NotificationMapper();

    @Override
    public void save(Notification<?> notification) {
        NotificationEntity notificationEntity = notificationMapper.toEntity(notification);
        entityManager.merge(notificationEntity);
    }

    @Override
    public List<Notification<?>> getPage(int limit) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<NotificationEntity> criteriaQuery = criteriaBuilder.createQuery(NotificationEntity.class);
        Root<NotificationEntity> rootEntity = criteriaQuery.from(NotificationEntity.class);

        criteriaQuery
                .select(rootEntity);

        return entityManager.createQuery(criteriaQuery)
                .setMaxResults(limit)
                .getResultList()
                .stream()
                .map(notificationMapper::toDto)
                .collect(toList());
    }

    @Override
    public Optional<Notification<?>> findById(UUID notificationId) {
        NotificationEntity notificationEntity = entityManager.find(NotificationEntity.class, notificationId.toString());
        entityManager.detach(notificationEntity);
        return Optional.ofNullable(notificationEntity)
                .map(notificationMapper::toDto);
    }

    @Override
    public void delete(UUID notificationId) {
        NotificationEntity notificationToDelete = this.findById(notificationId)
                .map(notificationMapper::toEntity)
                .orElseThrow();
        entityManager.remove(notificationToDelete);
    }
}
