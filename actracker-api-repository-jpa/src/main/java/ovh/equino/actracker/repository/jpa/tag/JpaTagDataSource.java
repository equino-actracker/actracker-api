package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

class JpaTagDataSource extends JpaDAO implements TagDataSource {

    JpaTagDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public Optional<TagDto> find(TagId tagId, User searcher) {
        return Optional.empty();
    }

    @Override
    public List<TagDto> find(EntitySearchCriteria searchCriteria) {
        return Collections.emptyList();
    }
}
