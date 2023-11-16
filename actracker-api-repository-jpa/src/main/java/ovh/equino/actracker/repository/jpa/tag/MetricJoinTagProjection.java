package ovh.equino.actracker.repository.jpa.tag;

record MetricJoinTagProjection(
        String id,
        String creatorId,
        String name,
        String type,
        String tagId,
        Boolean deleted) {
}
