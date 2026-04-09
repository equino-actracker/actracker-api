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
    protected List<TagDto> findPage(TagSearchCriteria searchCriteria) {
        return tagDataSource.find(searchCriteria);
    }

    @Override
    protected TagSearchCriteria forNextPageIdSearchCriteria(TagSearchCriteria searchCriteria) {
        return new TagSearchCriteria(
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
