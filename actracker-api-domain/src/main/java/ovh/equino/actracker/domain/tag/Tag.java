package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.Entity;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.UUID.randomUUID;
import static java.util.function.Predicate.isEqual;

public class Tag implements Entity {

    private final TagId id;
    private final User creator;
    private String name;
    final List<Metric> metrics;
    final List<Share> shares;
    private boolean deleted;

    private final TagValidator validator;

    Tag(TagId id,
        User creator,
        String name,
        Collection<Metric> metrics,
        List<Share> shares,
        boolean deleted,
        TagValidator validator) {

        this.id = requireNonNull(id);
        this.creator = requireNonNull(creator);
        this.name = name;
        this.metrics = new ArrayList<>(metrics);
        this.shares = new ArrayList<>(shares);
        this.deleted = deleted;

        this.validator = validator;
    }

    public static Tag create(TagDto tag, User creator) {

        List<Metric> metrics = requireNonNullElse(tag.metrics(), new ArrayList<MetricDto>()).stream()
                .map(metric -> Metric.create(metric, creator))
                .toList();

        Tag newTag = new Tag(
                new TagId(),
                creator,
                tag.name(),
                metrics,
                tag.shares(),
                false,
                new TagValidator()
        );

        newTag.validate();
        return newTag;
    }

    public void rename(String newName, User updater) {
        new TagEditOperation(updater, this, () ->
                this.name = newName
        ).execute();
    }

    public void addMetric(String name, MetricType type, User updater) {
        Metric newMetric = new Metric(new MetricId(randomUUID()), updater, name, type, false);
        new TagEditOperation(updater, this, () ->
                this.metrics.add(newMetric)
        ).execute();
    }

    public void deleteMetric(MetricId metricId, User updater) {
        new TagEditOperation(updater, this, () ->
                this.metrics.stream()
                        .filter(metric -> metric.id().equals(metricId))
                        .findFirst()
                        .ifPresent(Metric::delete)
        ).execute();
    }

    public void renameMetric(String newName, MetricId metricId, User updater) {
        new TagEditOperation(updater, this, () ->
                this.metrics.stream()
                        .filter(metric -> metric.id().equals(metricId))
                        .findFirst()
                        .ifPresent(metric -> metric.rename(newName))
        ).execute();
    }

    public void delete(User remover) {
        new TagEditOperation(remover, this, () -> {
            this.metrics.forEach(Metric::delete);
            this.deleted = true;
        }).execute();
    }

    public void share(Share newShare, User granter) {
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
        new TagEditOperation(granter, this, () -> {

            List<Share> sharesWithExclusion = this.shares.stream()
                    .filter(share -> !share.granteeName().equals(granteeName))
                    .toList();
            this.shares.clear();
            this.shares.addAll(sharesWithExclusion);

        }).execute();
    }

    public static Tag fromStorage(TagDto tag) {
        List<Metric> metrics = tag.metrics().stream()
                .map(Metric::fromStorage)
                .toList();
        return new Tag(
                new TagId(tag.id()),
                new User(tag.creatorId()),
                tag.name(),
                metrics,
                tag.shares(),
                tag.deleted(),
                new TagValidator()
        );
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

    boolean isDeleted() {
        return deleted;
    }

    boolean isNotDeleted() {
        return !isDeleted();
    }

    boolean isAccessibleFor(User user) {
        return creator.equals(user) || isGrantee(user);
    }

    private boolean isGrantee(User user) {
        return shares.stream()
                .map(Share::grantee)
                .filter(Objects::nonNull)
                .anyMatch(isEqual(user));
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

    // TODO think about extracting it to superclass
    public TagId id() {
        return this.id;
    }

}
