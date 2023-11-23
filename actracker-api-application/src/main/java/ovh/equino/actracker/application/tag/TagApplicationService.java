package ovh.equino.actracker.application.tag;

import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagApplicationService {

    private final TagRepository tagRepository;
    private final TagDataSource tagDataSource;
    private final TagSearchEngine tagSearchEngine;
    private final TagNotifier tagNotifier;
    private final IdentityProvider identityProvider;
    private final TenantRepository tenantRepository;

    public TagApplicationService(TagRepository tagRepository,
                                 TagDataSource tagDataSource,
                                 TagSearchEngine tagSearchEngine,
                                 TagNotifier tagNotifier,
                                 IdentityProvider identityProvider,
                                 TenantRepository tenantRepository) {

        this.tagRepository = tagRepository;
        this.tagDataSource = tagDataSource;
        this.tagSearchEngine = tagSearchEngine;
        this.tagNotifier = tagNotifier;
        this.identityProvider = identityProvider;
        this.tenantRepository = tenantRepository;
    }

    public TagResult createTag(CreateTagCommand createTagCommand) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User creator = new User(requesterIdentity.getId());

        TagDto tagData = new TagDto(
                createTagCommand.tagName(),
                createTagCommand.metricAssignments().stream()
                        .map(metricAssignment ->
                                new MetricDto(
                                        metricAssignment.metricName(),
                                        MetricType.valueOf(metricAssignment.metricType())
                                )
                        )
                        .toList(),
                createTagCommand.grantedShares().stream()
                        .map(this::resolveShare)
                        .toList()
        );
        Tag tag = Tag.create(tagData, creator);
        tagRepository.add(tag.forStorage());

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), creator)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find created tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });
    }

    public List<TagResult> resolveTags(Set<UUID> tagIds) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        Set<TagId> domainTagIds = tagIds
                .stream()
                .map(TagId::new)
                .collect(toUnmodifiableSet());

        return tagDataSource.find(domainTagIds, searcher).stream()
                .map(this::toTagResult)
                .toList();
    }

    public SearchResult<TagResult> searchTags(SearchTagsQuery searchTagsQuery) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                searchTagsQuery.pageSize(),
                searchTagsQuery.pageId(),
                searchTagsQuery.term(),
                null,
                null,
                searchTagsQuery.excludeFilter(),
                null,
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
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.rename(newName, updater);
        tagRepository.update(tagId, tag.forStorage());

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), updater)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });
    }

    public void deleteTag(UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User remover = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.delete(remover);
        tagRepository.update(tagId, tag.forStorage());

        tagNotifier.notifyChanged(tag.forChangeNotification());
    }

    public TagResult addMetricToTag(String metricName, String metricType, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.addMetric(metricName, MetricType.valueOf(metricType), updater);
        tagRepository.update(tagId, tag.forStorage());

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), updater)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    public TagResult deleteMetric(UUID metricId, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.deleteMetric(new MetricId(metricId), updater);
        tagRepository.update(tagId, tag.forStorage());

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), updater)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    public TagResult renameMetric(String newName, UUID metricId, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.renameMetric(newName, new MetricId(metricId), updater);
        tagRepository.update(tagId, tag.forStorage());

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), updater)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    public TagResult shareTag(String newGrantee, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User granter = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        Share share = resolveShare(newGrantee);

        tag.share(share, granter);
        tagRepository.update(tagId, tag.forStorage());

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), granter)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    public TagResult unshareTag(String granteeName, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User granter = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.unshare(granteeName, granter);
        tagRepository.update(tagId, tag.forStorage());

        tagNotifier.notifyChanged(tag.forChangeNotification());

        return tagDataSource.find(tag.id(), granter)
                .map(this::toTagResult)
                .orElseThrow(() -> {
                    String message = "Could not find updated tag with ID=%s".formatted(tag.id());
                    return new RuntimeException(message);
                });

    }

    private Share resolveShare(String grantee) {
        return tenantRepository.findByUsername(grantee)
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
