package ovh.equino.actracker.domain.dashboard;

public record Chart(

        String name,
        GroupBy groupBy
) {

    public enum GroupBy {
        TAG,
        DAY
    }
}
