package ovh.equino.actracker.repository.jpa.dashboard;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

@Embeddable
public class DayBucketEntityId implements Serializable {

    @Column(name = "tag_id")
    public String tagId;

    @Column(name = "bucket_range_start")
    public Instant bucketRangeStart;

    public DayBucketEntityId(String tagId, Instant bucketRangeStart) {
        this.tagId = tagId;
        this.bucketRangeStart = bucketRangeStart;
    }

    public DayBucketEntityId() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DayBucketEntityId that = (DayBucketEntityId) o;

        if (!Objects.equals(tagId, that.tagId)) return false;
        return Objects.equals(bucketRangeStart, that.bucketRangeStart);
    }

    @Override
    public int hashCode() {
        int result = tagId != null ? tagId.hashCode() : 0;
        result = 31 * result + (bucketRangeStart != null ? bucketRangeStart.hashCode() : 0);
        return result;
    }
}
