package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.*;

import java.math.BigDecimal;


@Entity
@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
                name = TagBucketEntity.PROCEDURE_NAME,
                procedureName = "activities_duration_by_tag",
                resultClasses = {TagBucketEntity.class},
                parameters = {
                        @StoredProcedureParameter(
                                name = TagBucketEntity.USER_ID_PARAM_NAME,
                                type = String.class,
                                mode = ParameterMode.IN)
                }
        )
})
class TagBucketEntity {

    static final String PROCEDURE_NAME = "tag_bucket_entity_procedure";
    static final String USER_ID_PARAM_NAME = "userId";

    @Id
    @Column(name = "tag_id")
    String tagId;

    @Column(name = "tag_duration")
    BigDecimal durationSeconds;

    @Column(name = "measured_percentage")
    BigDecimal percentage;
}
