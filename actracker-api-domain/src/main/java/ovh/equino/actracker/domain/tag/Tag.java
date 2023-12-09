package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static java.util.UUID.randomUUID;

public class Tag implements Entity {

    private final TagId id;
    private final User creator;
    private String name;
    final List<Metric> metrics;
    final List<Share> shares;
    private boolean deleted;

    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final TagValidator validator;

    Tag(TagId id,
        User creator,
        String name,
        Collection<Metric> metrics,
        Collection<Share> shares,
        boolean deleted,
        TagsAccessibilityVerifier tagsAccessibilityVerifier,
        TagValidator validator) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.metrics = new ArrayList<>(metrics);
        this.shares = new ArrayList<>(shares);
        this.deleted = deleted;

        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.validator = validator;
    }

    public void rename(String newName, User updater) {
        if (!creator.equals(updater) && !tagsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Tag.class, this.id.id());
        }
        new TagEditOperation(updater, this, () ->
                this.name = newName
        ).execute();
    }

    public void addMetric(String name, MetricType type, User updater) {
        if (!creator.equals(updater) && !tagsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Tag.class, this.id.id());
        }
        // TODO replace with metric factory?
        Metric newMetric = new Metric(new MetricId(randomUUID()), updater, name, type, false);
        new TagEditOperation(updater, this, () ->
                this.metrics.add(newMetric)
        ).execute();
    }

    public void deleteMetric(MetricId metricId, User updater) {
        if (!creator.equals(updater) && !tagsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Tag.class, this.id.id());
        }
        new TagEditOperation(updater, this, () ->
                this.metrics.stream()
                        .filter(metric -> metric.id().equals(metricId))
                        .findFirst()
                        .ifPresent(Metric::delete)
        ).execute();
    }

    public void renameMetric(String newName, MetricId metricId, User updater) {
        if (!creator.equals(updater) && !tagsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Tag.class, this.id.id());
        }
        new TagEditOperation(updater, this, () ->
                this.metrics.stream()
                        .filter(metric -> metric.id().equals(metricId))
                        .findFirst()
                        .ifPresent(metric -> metric.rename(newName))
        ).execute();
    }

    public void delete(User remover) {
        if (!creator.equals(remover) && !tagsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Tag.class, this.id.id());
        }
        new TagEditOperation(remover, this, () -> {
            this.metrics.forEach(Metric::delete);
            this.deleted = true;
        }).execute();
    }

    public void share(Share newShare, User granter) {
        if (!creator.equals(granter) && !tagsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Tag.class, this.id.id());
        }
        new TagEditOperation(granter, this, () -> {

            List<String> existingGranteeNames = this.shares.stream()
                    .map(Share::granteeName)
                    .toList();
            if (!existingGranteeNames.contains(newShare.granteeName())) {
                this.shares.add(newShare);
            }

        }).execute();
    }

    public void unshare(String granteeName, User granter) {
        if (!creator.equals(granter) && !tagsAccessibilityVerifier.isAccessible(this.id)) {
            throw new EntityNotFoundException(Tag.class, this.id.id());
        }
        new TagEditOperation(granter, this, () -> {

            List<Share> sharesWithExclusion = this.shares.stream()
                    .filter(share -> !share.granteeName().equals(granteeName))
                    .toList();
            this.shares.clear();
            this.shares.addAll(sharesWithExclusion);

        }).execute();
    }

    public TagDto forStorage() {
        List<MetricDto> metrics = this.metrics.stream()
                .map(Metric::forStorage)
                .toList();
        return new TagDto(id.id(), creator.id(), name, metrics, shares, deleted);
    }

    public TagChangedNotification forChangeNotification() {
        List<MetricDto> metrics = this.metrics.stream()
                .map(Metric::forStorage)
                .toList();
        TagDto dto = new TagDto(id.id(), creator.id(), name, metrics, shares, deleted);
        return new TagChangedNotification(dto);
    }

    boolean deleted() {
        return deleted;
    }

    @Override
    public void validate() {
        validator.validate(this);
    }

    String name() {
        return name;
    }

    @Override
    public User creator() {
        return creator;
    }

    List<Metric> metrics() {
        return metrics;
    }

    List<Share> shares() {
        return shares;
    }

    // TODO think about extracting it to superclass
    public TagId id() {
        return this.id;
    }

}
