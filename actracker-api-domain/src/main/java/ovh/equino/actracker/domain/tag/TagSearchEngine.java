package ovh.equino.actracker.domain.tag;

public interface TagSearchEngine {

    TagSearchResult findTags(TagSearchCriteria searchCriteria);
}
