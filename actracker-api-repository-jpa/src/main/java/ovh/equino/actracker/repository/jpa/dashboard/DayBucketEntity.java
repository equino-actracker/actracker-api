package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;


@Entity
@NamedStoredProcedureQueries({
        @NamedStoredProcedureQuery(
                name = DayBucketEntity.PROCEDURE_NAME,
                procedureName = "activities_duration_by_day",
                resultClasses = {DayBucketEntity.class},
                parameters = {
                        @StoredProcedureParameter(
                                name = DayBucketEntity.USER_ID_PARAM_NAME,
                                type = String.class,
                                mode = ParameterMode.IN),
                        @StoredProcedureParameter(
                                name = DayBucketEntity.RANGE_START_PARAM_NAME,
                                type = Instant.class,
                                mode = ParameterMode.IN),
                        @StoredProcedureParameter(
                                name = DayBucketEntity.RANGE_END_PARAM_NAME,
                                type = Instant.class,
                                mode = ParameterMode.IN)
                }
        )
})
class DayBucketEntity {

    static final String PROCEDURE_NAME = "day_bucket_entity_procedure";
    static final String USER_ID_PARAM_NAME = "userId";
    static final String RANGE_START_PARAM_NAME = "rangeStartTimestamp";
    static final String RANGE_END_PARAM_NAME = "rangeEndTimestamp";

    @EmbeddedId
    DayBucketEntityId id;

//    @Id
//    @Column(name = "tag_id")
//    String tagId;
//
//    @Id
//    @Column(name = "bucket_range_start")
//    Instant bucketRangeStart;

    @Column(name = "tag_duration")
    BigDecimal durationSeconds;

    @Column(name = "measured_percentage")
    BigDecimal percentage;


}
