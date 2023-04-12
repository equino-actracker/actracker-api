package ovh.equino.actracker.domain.tag;

import java.util.List;

public record TagSearchResult(

        String nextPageId,
        List<TagDto> tags
) {
}
