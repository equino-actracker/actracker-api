package ovh.equino.actracker.repository.jpa;

import ovh.equino.actracker.domain.tagset.TagSetDto;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class IntegrationTestTagSetsConfiguration {

    private final List<TagSetDto> addedTagSets = new ArrayList<>();
    private final List<TagSetDto> transientTagSets = new ArrayList<>();

    void persistIn(IntegrationTestRelationalDataBase dataBase) throws SQLException {
        dataBase.addTagSets(addedTagSets.toArray(new TagSetDto[0]));
    }

    public void add(TagSetDto tagSet) {
        addedTagSets.add(tagSet);
    }

    public void addTransient(TagSetDto tagSet) {
        transientTagSets.add(tagSet);
    }
}
