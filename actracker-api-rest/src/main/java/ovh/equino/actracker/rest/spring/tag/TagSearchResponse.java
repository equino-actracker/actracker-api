package ovh.equino.actracker.rest.spring.tag;

import java.util.List;

record TagSearchResponse(

        String nextPageId,
        List<Tag> results
) {
}
