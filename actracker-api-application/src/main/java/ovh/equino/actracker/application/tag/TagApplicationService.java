package ovh.equino.actracker.application.tag;

import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.tenant.TenantRepository;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

public class TagApplicationService {

    private final TagRepository tagRepository;
    private final TenantRepository tenantRepository;

    TagApplicationService(TagRepository tagRepository, TenantRepository tenantRepository) {
        this.tagRepository = tagRepository;
        this.tenantRepository = tenantRepository;
    }

    public TagDto renameTag(String newName, UUID tagId, User updater) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.rename(newName, updater);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(updater);
    }

    public void deleteTag(UUID tagId, User remover) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.delete(remover);
        tagRepository.update(tagId, tag.forStorage());
    }

    public TagDto addMetricToTag(String metricName, MetricType metricType, UUID tagId, User updater) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.addMetric(metricName, metricType, updater);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(updater);
    }

    public TagDto deleteMetric(UUID metricId, UUID tagId, User updater) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.deleteMetric(new MetricId(metricId), updater);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(updater);
    }

    public TagDto renameMetric(String newName, UUID metricId, UUID tagId, User updater) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.renameMetric(newName, new MetricId(metricId), updater);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(updater);
    }

    public TagDto shareTag(Share newShare, UUID tagId, User granter) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        Share share = tenantRepository.findByUsername(newShare.granteeName())
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(newShare.granteeName()));

        tag.share(share, granter);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(granter);
    }

    public TagDto unshareTag(String granteeName, UUID tagId, User granter) {
        TagDto tagDto = tagRepository.findById(tagId)
                .orElseThrow(() -> new EntityNotFoundException(Tag.class, tagId));
        Tag tag = Tag.fromStorage(tagDto);

        tag.unshare(granteeName, granter);
        tagRepository.update(tagId, tag.forStorage());
        return tag.forClient(granter);
    }
}
