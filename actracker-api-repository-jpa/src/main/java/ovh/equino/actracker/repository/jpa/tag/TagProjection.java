package ovh.equino.actracker.repository.jpa.tag;

record TagProjection(String id, String creatorId, String name, Boolean deleted) {
}
