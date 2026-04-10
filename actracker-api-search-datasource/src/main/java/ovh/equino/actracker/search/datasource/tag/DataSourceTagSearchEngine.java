package ovh.equino.actracker.search.datasource.tag;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tag.TagSearchEngine;
import ovh.equino.actracker.search.datasource.DataSourceSearchEngine;

import java.util.List;

class DataSourceTagSearchEngine
        extends DataSourceSearchEngine<TagDto, TagSearchCriteria>
        implements TagSearchEngine {

    private final TagDataSource tagDataSource;

    DataSourceTagSearchEngine(TagDataSource tagDataSource) {
        super(new TagAttributeExtractor());
        this.tagDataSource = tagDataSource;
    }

    @Override
    public EntitySearchResult<TagDto> findTags(TagSearchCriteria searchCriteria) {
        return findBy(searchCriteria);
    }

    @Override
    protected List<TagDto> searchInDataSource(TagSearchCriteria searchCriteria) {
        return tagDataSource.find(searchCriteria);
    }

    @Override
    protected TagSearchCriteria withCommonCriteriaReplaced(TagSearchCriteria searchCriteria,
                                                           EntitySearchCriteria.Common newCommonCriteria) {
        return new TagSearchCriteria(
                newCommonCriteria,
                searchCriteria.term(),
                searchCriteria.excludeFilter()
        );
    }
}
