package ovh.equino.actracker.repository.jpa.activity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaQuery;
import ovh.equino.actracker.domain.activity.*;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Optional;
import java.util.UUID;

class JpaActivityRepository extends JpaDAO implements ActivityRepository {

    private final ActivityFactory activityFactory;

    JpaActivityRepository(EntityManager entityManager, ActivityFactory activityFactory) {
        super(entityManager);
        this.activityFactory = activityFactory;
    }

    private final ActivityMapper mapper = new ActivityMapper(entityManager);

    @Override
    public void add(ActivityDto activity) {
        ActivityEntity activityEntity = mapper.toEntity(activity);
        entityManager.persist(activityEntity);
    }

    @Override
    public void update(UUID activityId, ActivityDto activity) {
        ActivityEntity activityEntity = mapper.toEntity(activity);
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
                .map(mapper::toDto);
    }

    @Override
    public Optional<Activity> get(ActivityId activityId) {
        return Optional.empty();
    }

    @Override
    public void add(Activity activity) {

    }

    @Override
    public void save(Activity activity) {

    }
}
