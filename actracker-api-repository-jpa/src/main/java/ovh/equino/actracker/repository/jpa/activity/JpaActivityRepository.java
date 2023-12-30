package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;
import java.util.UUID;

class JpaActivityRepository extends JpaDAO implements ActivityRepository {

    private final ActivityMapper activityMapper;

    JpaActivityRepository(EntityManager entityManager, ActivityFactory activityFactory) {
        super(entityManager);
        this.activityMapper = new ActivityMapper(activityFactory, entityManager);
    }

    @Override
    public void add(ActivityDto activity) {
        ActivityEntity activityEntity = activityMapper.toEntity(activity);
        entityManager.persist(activityEntity);
    }

    @Override
    public void update(UUID activityId, ActivityDto activity) {
        ActivityEntity activityEntity = activityMapper.toEntity(activity);
        activityEntity.id = activityId.toString();
        entityManager.merge(activityEntity);
    }

    @Override
    public Optional<ActivityDto> findById(UUID activityId) {

        ActivityQueryBuilder queryBuilder = new ActivityQueryBuilder(entityManager);

        // If Hibernate were used instead of JPA API, filters could be used instead for soft delete:
        // https://www.baeldung.com/spring-jpa-soft-delete
        CriteriaQuery<ActivityEntity> query = queryBuilder.select()
                .where(
                        queryBuilder.and(
                                queryBuilder.hasId(activityId),
                                queryBuilder.isNotDeleted()
                        )
                );

        TypedQuery<ActivityEntity> typedQuery = entityManager.createQuery(query);

        // If Hibernate were used instead of JPA API, result transformers could do mapping rather than custom mapper:
        // https://thorben-janssen.com/object-mapper-dto/
        return typedQuery.getResultList().stream()
                .findFirst()
                .map(activityMapper::toDto);
    }

    @Override
    public Optional<Activity> get(ActivityId activityId) {
        ActivityEntity entity = entityManager.find(ActivityEntity.class, activityId.id().toString());
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
