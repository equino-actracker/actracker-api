package ovh.equino.actracker.rest.spring.dashboard;

import java.util.Collection;

record Chart(

        String name,
        GroupBy groupBy,
        Collection<String> includedTags

) {

    enum GroupBy {
        TAG, DAY
    }
}
