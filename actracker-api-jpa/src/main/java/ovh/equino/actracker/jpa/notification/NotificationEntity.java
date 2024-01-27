package ovh.equino.actracker.jpa.notification;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ovh.equino.actracker.jpa.JpaEntity;

import static lombok.AccessLevel.PRIVATE;

@Entity
@Table(name = "outbox_notification")
@NoArgsConstructor
@Getter
@Setter
public class NotificationEntity extends JpaEntity {

    @Column(name = "version"/*, updatable = false, insertable = false*/)
    // TODO UPDATE WITH TRIGGER BEFORE INSERT
    private long version;

    @Column(name = "entity")
    private String data;

    @Column(name = "entity_type")
    private String dataType;
}
