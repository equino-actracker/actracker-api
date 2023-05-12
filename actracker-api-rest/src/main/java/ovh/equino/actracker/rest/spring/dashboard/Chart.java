package ovh.equino.actracker.rest.spring.dashboard;

import java.util.Collection;

record Chart(

        String name,
        GroupBy groupBy,
        Collection<String> includedTags

) {

    enum GroupBy {
        TAG, DAY, WEEK, MONTH;

        static GroupBy fromDomain(ovh.equino.actracker.domain.dashboard.Chart.GroupBy groupBy) {
            return switch (groupBy) {
                case TAG -> TAG;
                case DAY -> DAY;
                case WEEK -> WEEK;
                case MONTH -> MONTH;
            };
        }

        static ovh.equino.actracker.domain.dashboard.Chart.GroupBy toDomain(GroupBy groupBy) {
            return switch (groupBy) {
                case TAG -> ovh.equino.actracker.domain.dashboard.Chart.GroupBy.TAG;
                case DAY -> ovh.equino.actracker.domain.dashboard.Chart.GroupBy.DAY;
                case WEEK -> ovh.equino.actracker.domain.dashboard.Chart.GroupBy.WEEK;
                case MONTH -> ovh.equino.actracker.domain.dashboard.Chart.GroupBy.MONTH;
            };
        }
    }
}
