package ovh.equino.actracker.rest.spring.tag;

import java.util.List;

record Tag(
        String id,
        String name,
        List<Metric> metrics
) {
}
