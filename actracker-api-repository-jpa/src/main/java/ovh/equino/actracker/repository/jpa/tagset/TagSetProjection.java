package ovh.equino.actracker.repository.jpa.tagset;

record TagSetProjection(String id,
                        String creatorId,
                        String name,
                        Boolean deleted
) {
}
