package ovh.equino.actracker.application.tag;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagApplicationService {

    private final TagFactory tagFactory;
    private final MetricFactory metricFactory;
    private final TagRepository tagRepository;
    private final TagDataSource tagDataSource;
    private final TagSearchEngine tagSearchEngine;
    private final TagNotifier tagNotifier;
    private final ActorExtractor actorExtractor;
    private final TenantDataSource tenantDataSource;

    public TagApplicationService(TagFactory tagFactory,
                                 MetricFactory metricFactory,
                                 TagRepository tagRepository,
                                 TagDataSource tagDataSource,
                                 TagSearchEngine tagSearchEngine,
                                 TagNotifier tagNotifier,
                                 ActorExtractor actorExtractor,
                                 TenantDataSource tenantDataSource) {

        this.tagFactory = tagFactory;
        this.metricFactory = metricFactory;
        this.tagRepository = tagRepository;
        this.tagDataSource = tagDataSource;
        this.tagSearchEngine = tagSearchEngine;
        this.tagNotifier = tagNotifier;
        this.actorExtractor = actorExtractor;
        this.tenantDataSource = tenantDataSource;
    }

    public TagResult getTag(UUID tagId) {
        User actor = actorExtractor.getActor();

        return tagDataSource.find(new TagId(tagId), actor)
                .map(this::toTagResult)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
    }

    public TagResult createTag(CreateTagCommand createTagCommand) {
        User creator = actorExtractor.getActor();

        List<Metric> metrics = createTagCommand.metricAssignments()
                .stream()
                .map(metric -> metricFactory.create(
                        creator, metric.metricName(), MetricType.valueOf(metric.metricType())
                ))
                .toList();
        List<Share> shares = createTagCommand.grantedShares()
                .stream()
                .map(Share::new)
                .toList();

        Tag tag = tagFactory.create(createTagCommand.tagName(), metrics, shares);
        tagRepository.add(tag);

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), creator)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find created tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });
    }

    public List<TagResult> resolveTags(Set<UUID> tagIds) {
        User searcher = actorExtractor.getActor();

        Set<TagId> domainTagIds = tagIds
                .stream()
                .map(TagId::new)
                .collect(toUnmodifiableSet());

        return tagDataSource.find(domainTagIds, searcher).stream()
                .map(this::toTagResult)
                .toList();
    }

    public SearchResult<TagResult> searchTags(SearchTagsQuery searchTagsQuery) {
        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                actorExtractor.getActor(),
                searchTagsQuery.pageSize(),
                searchTagsQuery.pageId(),
                searchTagsQuery.term(),
                null,
                null,
                searchTagsQuery.excludeFilter(),
                null
        );
        EntitySearchResult<TagDto> searchResult = tagSearchEngine.findTags(searchCriteria);
        List<TagResult> resultForClient = searchResult.results()
                .stream()
                .map(this::toTagResult)
                .toList();

        return new SearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public TagResult renameTag(String newName, UUID tagId) {
        User updater = actorExtractor.getActor();
        TagId id = new TagId(tagId);

        Tag tag = tagRepository.get(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        tag.rename(newName);
        tagRepository.save(tag);

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), updater)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });
    }

    public void deleteTag(UUID tagId) {
        TagId id = new TagId(tagId);

        Tag tag = tagRepository.get(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        tag.delete();
        tagRepository.save(tag);

        tagNotifier.notifyChanged(tag.forChangeNotification());
    }

    public TagResult addMetricToTag(String metricName, String metricType, UUID tagId) {
        User updater = actorExtractor.getActor();
        TagId id = new TagId(tagId);

        Tag tag = tagRepository.get(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        tag.addMetric(metricName, MetricType.valueOf(metricType));
        tagRepository.save(tag);

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), updater)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    public TagResult deleteMetric(UUID metricId, UUID tagId) {
        User updater = actorExtractor.getActor();
        TagId id = new TagId(tagId);

        Tag tag = tagRepository.get(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        tag.deleteMetric(new MetricId(metricId));
        tagRepository.save(tag);

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), updater)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    public TagResult renameMetric(String newName, UUID metricId, UUID tagId) {
        User updater = actorExtractor.getActor();
        TagId id = new TagId(tagId);

        Tag tag = tagRepository.get(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        tag.renameMetric(newName, new MetricId(metricId));
        tagRepository.save(tag);

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), updater)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    public TagResult shareTag(String newGrantee, UUID tagId) {
        User granter = actorExtractor.getActor();
        TagId id = new TagId(tagId);

        Tag tag = tagRepository.get(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        Share share = resolveShare(newGrantee);

        tag.share(share);
        tagRepository.save(tag);

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), granter)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    public TagResult unshareTag(String granteeName, UUID tagId) {
        User granter = actorExtractor.getActor();
        TagId id = new TagId(tagId);

        Tag tag = tagRepository.get(id)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));

        tag.unshare(granteeName);
        tagRepository.save(tag);

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), granter)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });
    }

    // TODO extract to share resolver service
    private Share resolveShare(String grantee) {
        return tenantDataSource.findByUsername(grantee)
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(grantee));
    }

    private TagResult toTagResult(TagDto tagDto) {
        List<MetricResult> metricResults = tagDto.metrics().stream()
                .map(this::toMetricResult)
                .toList();
        List<String> shares = tagDto.shares().stream()
                .map(Share::granteeName)
                .toList();
        return new TagResult(tagDto.id(), tagDto.name(), metricResults, shares);
    }

    private MetricResult toMetricResult(MetricDto metricDto) {
        return new MetricResult(metricDto.id(), metricDto.name(), metricDto.type().toString());
    }
}
