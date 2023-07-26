package ovh.equino.actracker.rest.spring.dashboard;

import java.util.Collection;

record Chart(

        String id,
        String name,
        String groupBy,
        String metric,
        Collection<String> includedTags

) {
}
