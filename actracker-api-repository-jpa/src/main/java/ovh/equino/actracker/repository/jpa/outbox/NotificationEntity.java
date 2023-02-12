package ovh.equino.actracker.repository.jpa.outbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
class NotificationEntity {

    @Id
    String id;
    String entity;
}
