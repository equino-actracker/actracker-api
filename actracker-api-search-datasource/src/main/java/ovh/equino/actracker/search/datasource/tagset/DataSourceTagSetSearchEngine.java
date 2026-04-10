package ovh.equino.actracker.search.datasource.tagset;

import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetSearchCriteria;
import ovh.equino.actracker.domain.tagset.TagSetSearchEngine;
import ovh.equino.actracker.search.datasource.DataSourceSearchEngine;

import java.util.List;

class DataSourceTagSetSearchEngine
        extends DataSourceSearchEngine<TagSetDto, TagSetSearchCriteria>
        implements TagSetSearchEngine {

    private final TagSetDataSource tagSetDataSource;

    DataSourceTagSetSearchEngine(TagSetDataSource tagSetDataSource) {
        super(new TagSetAttributeExtractor());
        this.tagSetDataSource = tagSetDataSource;
    }

    @Override
    public EntitySearchResult<TagSetDto> findTagSets(TagSetSearchCriteria searchCriteria) {
        return findBy(searchCriteria);
    }

    @Override
    protected List<TagSetDto> searchInDataSource(TagSetSearchCriteria searchCriteria) {
        return tagSetDataSource.find(searchCriteria);
    }

    @Override
    protected TagSetSearchCriteria withCommonCriteriaReplaced(TagSetSearchCriteria searchCriteria,
                                                              EntitySearchCriteria.Common newCommonCriteria) {
        return new TagSetSearchCriteria(
                newCommonCriteria,
                searchCriteria.term(),
                searchCriteria.excludeFilter()
        );
    }
}
