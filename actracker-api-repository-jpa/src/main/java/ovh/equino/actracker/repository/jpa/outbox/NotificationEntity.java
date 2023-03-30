package ovh.equino.actracker.repository.jpa.outbox;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "outbox_notification")
class NotificationEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "version"/*, updatable = false, insertable = false*/)
    // TODO UPDATE WITH TRIGGER BEFORE INSERT
    long version;

    @Column(name = "entity")
    String data;
}
