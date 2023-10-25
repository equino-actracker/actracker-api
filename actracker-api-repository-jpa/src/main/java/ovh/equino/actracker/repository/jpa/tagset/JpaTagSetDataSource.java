package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.Tuple;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

class JpaTagSetDataSource extends JpaDAO implements TagSetDataSource {

    @Override
    public Optional<TagSetDto> find(TagSetId tagSetId, User searcher) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> tupleQuery = criteriaBuilder.createTupleQuery();
        Root<TagSetEntity> root = tupleQuery.from(TagSetEntity.class);
        Join<Object, Object> tagSetTags = root.join("tags", JoinType.LEFT);
        tupleQuery.select(criteriaBuilder.tuple(
                root.get("id").alias("id"),
                root.get("creatorId"),
                root.get("name"),
                tagSetTags.get("id")
        ));

        TypedQuery<Tuple> query = entityManager.createQuery(tupleQuery);
        List<Tuple> results = query.getResultList()
                .stream()
                .toList();


//        CriteriaQuery<TagSetDto> criteriaQuery = criteriaBuilder.createQuery(TagSetDto.class);
//        Root<TagSetEntity> root = criteriaQuery.from(TagSetEntity.class);
//
//        criteriaQuery.select(criteriaBuilder.construct(
//                        TagSetDto.class,
//                        root.get("id"),
//                        root.get("creatorId"),
//                        root.get("name"),
//                        root.get("tags"),
//                        root.get("deleted")
//                )
//        );
//
//        TypedQuery<TagSetDto> query = entityManager.createQuery(criteriaQuery);

//        return query.getResultList()
//                .stream()
//                .findFirst();

        return Optional.empty();
    }

    @Override
    public List<TagSetDto> find(EntitySearchCriteria searchCriteria) {


        return null;
    }
}
