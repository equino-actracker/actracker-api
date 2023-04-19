package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.Predicate;
import ovh.equino.actracker.repository.jpa.JpaQueryBuilder;

class TagSetQueryBuilder extends JpaQueryBuilder<TagSetEntity> {

    TagSetQueryBuilder(EntityManager entityManager) {
        super(entityManager, TagSetEntity.class);
    }

    Predicate matchesTerm(String term) {
        return super.matchesTerm(term, "name");
    }
}
