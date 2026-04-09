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
    protected List<TagSetDto> findPage(TagSetSearchCriteria searchCriteria) {
        return tagSetDataSource.find(searchCriteria);
    }

    @Override
    protected TagSetSearchCriteria forNextPageIdSearchCriteria(TagSetSearchCriteria searchCriteria) {
        return new TagSetSearchCriteria(
                new EntitySearchCriteria.Common(
                        searchCriteria.common().searcher(),
                        searchCriteria.common().pageSize() + 1,   // additional one to calculate next page ID
                        searchCriteria.common().pageId(),
                        searchCriteria.common().sortCriteria()
                ),
                searchCriteria.term(),
                searchCriteria.excludeFilter()
        );
    }
}
