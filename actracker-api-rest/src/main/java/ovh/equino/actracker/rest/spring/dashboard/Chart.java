package ovh.equino.actracker.rest.spring.dashboard;

record Chart(

        String name,
        GroupBy groupBy

) {

    enum GroupBy {
        TAG, DAY
    }
}
