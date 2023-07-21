package ovh.equino.actracker.application.tag;

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

    TagApplicationService(TagRepository tagRepository,
                          TagSearchEngine tagSearchEngine,
                          IdentityProvider identityProvider,
                          TenantRepository tenantRepository) {

        this.tagRepository = tagRepository;
        this.tagSearchEngine = tagSearchEngine;
        this.identityProvider = identityProvider;
        this.tenantRepository = tenantRepository;
    }

    public TagDto createTag(TagDto newTagData) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User creator = new User(requesterIdentity.getId());

        TagDto tagDataWithSharesResolved = new TagDto(
                newTagData.id(),
                newTagData.creatorId(),
                newTagData.name(),
                newTagData.metrics(),
                newTagData.shares().stream()
                        .map(this::resolveShare)
                        .toList(),
                newTagData.deleted()
        );
        Tag tag = Tag.create(tagDataWithSharesResolved, creator);
        tagRepository.add(tag.forStorage());
        return tag.forClient(creator);
    }

    public List<TagDto> resolveTags(Set<UUID> tagIds) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        return tagRepository.findByIds(tagIds, searcher).stream()
                .map(Tag::fromStorage)
                .map(tag -> tag.forClient(searcher))
                .toList();
    }

    public EntitySearchResult<TagDto> searchTags(EntitySearchCriteria searchCriteria) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User searcher = new User(requesterIdentity.getId());

        EntitySearchResult<TagDto> searchResult = tagSearchEngine.findTags(searchCriteria);
        List<TagDto> resultForClient = searchResult.results().stream()
                .map(Tag::fromStorage)
                .map(tag -> tag.forClient(searchCriteria.searcher()))
                .toList();
        return new EntitySearchResult<>(searchResult.nextPageId(), resultForClient);
    }

    public TagDto renameTag(String newName, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.rename(newName, updater);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(updater);
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

    public TagDto addMetricToTag(String metricName, MetricType metricType, UUID tagId) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User updater = new User(requesterIdentity.getId());

        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.addMetric(metricName, metricType, updater);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(updater);
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

        Share share = resolveShare(newShare);

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

    private Share resolveShare(Share share) {
        return tenantRepository.findByUsername(share.granteeName())
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(share.granteeName()));
    }

}
