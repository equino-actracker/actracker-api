package ovh.equino.actracker.rest.spring.tagset;

import java.util.Collection;

record TagSet(
        String id,
        String name,
        Collection<String> tags
) {
}
