package ovh.equino.actracker.rest.spring;


import java.util.List;

public record SearchResponse<ENTITY>(

        String nextPageId,
        List<ENTITY> results
) {
}
