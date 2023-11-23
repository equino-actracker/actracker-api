package ovh.equino.actracker.domain.tag;

import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.isNull;
import static java.util.UUID.randomUUID;

public record TagId(UUID id) {

    public TagId {
        if (isNull(id)) {
            throw new IllegalArgumentException("TagId.id must not be null");
        }
    }

    public TagId() {
        this(randomUUID());
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagId tagId = (TagId) o;
        return Objects.equals(id, tagId.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
