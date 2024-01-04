package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;

import static java.util.Objects.nonNull;

class JpaActivityRepository extends JpaDAO implements ActivityRepository {

    private final ActivityMapper activityMapper;

    JpaActivityRepository(EntityManager entityManager, ActivityFactory activityFactory) {
        super(entityManager);
        this.activityMapper = new ActivityMapper(activityFactory, entityManager);
    }

    @Override
    public Optional<Activity> get(ActivityId activityId) {
        ActivityEntity entity = entityManager.find(ActivityEntity.class, activityId.id().toString());
        if (nonNull(entity)) {
            entityManager.detach(entity);
        }
        Activity activity = activityMapper.toDomainObject(entity);
        return Optional.ofNullable(activity);
    }

    @Override
    public void add(Activity activity) {
        ActivityDto dto = activity.forStorage();
        ActivityEntity entity = activityMapper.toEntity(dto);
        entityManager.persist(entity);
    }

    @Override
    public void save(Activity activity) {
        ActivityDto dto = activity.forStorage();
        ActivityEntity entity = activityMapper.toEntity(dto);
        entityManager.merge(entity);
    }
}
