package ovh.equino.actracker.repository.jpa.tag;

import jakarta.persistence.*;

@Entity
@Table(name = "tag_share")
class TagShareEntity {

    @Id
    @Column(name = "id")
    String id;

    @ManyToOne
    @JoinColumn(name = "tag_id")
    TagEntity tag;

    @Column(name = "grantee_id")
    String granteeId;

    @Column(name = "grantee_name")
    String granteeName;

}
