package ovh.equino.actracker.application.tag;

import ovh.equino.actracker.domain.exception.EntityNotFoundException;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.user.User;

import java.util.UUID;

public class TagApplicationService {

    private final TagRepository tagRepository;

    TagApplicationService(TagRepository tagRepository) {
        this.tagRepository = tagRepository;
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
        return tag.forClient(updater);
    }
}
