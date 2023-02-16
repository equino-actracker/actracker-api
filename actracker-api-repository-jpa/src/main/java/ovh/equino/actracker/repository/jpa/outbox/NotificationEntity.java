package ovh.equino.actracker.repository.jpa.outbox;

import jakarta.persistence.*;

@Entity
@Table(name = "outbox_notification")
class NotificationEntity {

    @Id
    @Column(name = "id")
    String id;

    @Column(name = "version")
    @GeneratedValue(generator = "notificationVersion")
    @SequenceGenerator(name = "notificationVersion",
            sequenceName = "outbox_notification_version_seq", allocationSize = 1
    )
    long version;

    @Column(name = "entity")
    String entity;
}
