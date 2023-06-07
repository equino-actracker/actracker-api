package ovh.equino.actracker.rest.spring.dashboard;

enum GroupBy {
    SELF, DAY, WEEK, MONTH, WEEKEND;

    static GroupBy fromDomain(ovh.equino.actracker.domain.dashboard.GroupBy groupBy) {
        return switch (groupBy) {
            case SELF -> SELF;
            case DAY -> DAY;
            case WEEK -> WEEK;
            case MONTH -> MONTH;
            case WEEKEND -> WEEKEND;
        };
    }

    static ovh.equino.actracker.domain.dashboard.GroupBy toDomain(GroupBy groupBy) {
        return switch (groupBy) {
            case SELF -> ovh.equino.actracker.domain.dashboard.GroupBy.SELF;
            case DAY -> ovh.equino.actracker.domain.dashboard.GroupBy.DAY;
            case WEEK -> ovh.equino.actracker.domain.dashboard.GroupBy.WEEK;
            case MONTH -> ovh.equino.actracker.domain.dashboard.GroupBy.MONTH;
            case WEEKEND -> ovh.equino.actracker.domain.dashboard.GroupBy.WEEKEND;
        };
    }
}
