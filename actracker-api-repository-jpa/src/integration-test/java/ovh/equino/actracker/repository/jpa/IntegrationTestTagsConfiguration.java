package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.user.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.function.Predicate.not;
import static java.util.stream.Stream.concat;

public class IntegrationTestTagsConfiguration {

    private final List<TagDto> addedTags = new ArrayList<>();
    private final List<TagDto> transientTags = new ArrayList<>();

    void persistIn(IntegrationTestRelationalDataBase database) throws SQLException {
        database.addTags(addedTags.toArray(new TagDto[0]));
    }

    public void add(TagDto tag) {
        addedTags.add(tag);
    }

    public void addTransient(TagDto tag) {
        transientTags.add(tag);
    }

    public Collection<TagDto> accessibleFor(User user) {
        return addedTags;
    }

    public Collection<TagDto> inaccessibleFor(User user) {
        Collection<TagDto> accessibleTags = accessibleFor(user);
        return concat(addedTags.stream(), transientTags.stream())
                .filter(not(accessibleTags::contains))
                .toList();
    }
}
