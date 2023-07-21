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

public class TagApplicationService {

    private final TagRepository tagRepository;
    private final TagSearchEngine tagSearchEngine;
    private final IdentityProvider identityProvider;
    private final TenantRepository tenantRepository;

    public TagApplicationService(TagRepository tagRepository,
                                 TagSearchEngine tagSearchEngine,
                                 IdentityProvider identityProvider,
                                 TenantRepository tenantRepository) {

        this.tagRepository = tagRepository;
        this.tagSearchEngine = tagSearchEngine;
        this.identityProvider = identityProvider;
        this.tenantRepository = tenantRepository;
    }

    public TagResult createTag(CreateTagCommand createTagCommand) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User creator = new User(requesterIdentity.getId());

        TagDto tagData = new TagDto(
                createTagCommand.tagName(),
                createTagCommand.assignedMetrics().stream()
                        .map(assignedMetric ->
                                new MetricDto(
                                        assignedMetric.name(),
                                        MetricType.valueOf(assignedMetric.type())
                                )
                        )
                        .toList(),
                createTagCommand.grantedShares().stream()
                        .map(this::resolveShare)
                        .toList()
        );
        Tag tag = Tag.create(tagData, creator);
        tagRepository.add(tag.forStorage());
        TagDto tagResult = tag.forClient(creator);

        return toTagResult(tagResult);
    }

    public List<TagResult> resolveTags(Set<UUID> tagIds) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        return tagRepository.findByIds(tagIds, searcher).stream()
                .map(Tag::fromStorage)
                .map(tag -> tag.forClient(searcher))
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
        List<TagResult> resultForClient = searchResult.results().stream()
                .map(Tag::fromStorage)
                .map(tag -> tag.forClient(searchCriteria.searcher()))
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
        TagDto tagResult = tag.forClient(updater);
        return toTagResult(tagResult);
    }

    public void deleteTag(UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User remover = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.delete(remover);
        tagRepository.update(tagId, tag.forStorage());
    }

    public TagResult addMetricToTag(String metricName, String metricType, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.addMetric(metricName, MetricType.valueOf(metricType), updater);
        tagRepository.update(tagId, tag.forStorage());
        TagDto tagResult = tag.forClient(updater);
        return toTagResult(tagResult);
    }

    public TagDto deleteMetric(UUID metricId, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.deleteMetric(new MetricId(metricId), updater);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(updater);
    }

    public TagDto renameMetric(String newName, UUID metricId, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.renameMetric(newName, new MetricId(metricId), updater);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(updater);
    }

    public TagDto shareTag(Share newShare, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User granter = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        Share share = resolveShare(newShare.granteeName());

        tag.share(share, granter);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(granter);
    }

    public TagDto unshareTag(String granteeName, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User granter = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.unshare(granteeName, granter);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(granter);
    }

    private Share resolveShare(String share) {
        return tenantRepository.findByUsername(share)
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(share));
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
