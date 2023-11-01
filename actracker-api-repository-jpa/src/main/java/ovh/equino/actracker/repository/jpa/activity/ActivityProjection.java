package ovh.equino.actracker.repository.jpa.activity;

import java.sql.Timestamp;

record ActivityProjection(String id,
                          String creatorId,
                          String title,
                          Timestamp startTime,
                          Timestamp endTime,
                          String comment,
                          Boolean deleted) {
}
