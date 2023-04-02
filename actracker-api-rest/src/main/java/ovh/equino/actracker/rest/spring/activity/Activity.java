package ovh.equino.actracker.rest.spring.activity;

record Activity(
        String id,
        Long startTimestamp,
        Long endTimestamp,
        String comment
) {}
