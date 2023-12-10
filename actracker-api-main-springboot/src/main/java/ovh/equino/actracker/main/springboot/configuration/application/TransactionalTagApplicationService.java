package ovh.equino.actracker.main.springboot.configuration.application;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.tag.CreateTagCommand;
import ovh.equino.actracker.application.tag.SearchTagsQuery;
import ovh.equino.actracker.application.tag.TagApplicationService;
import ovh.equino.actracker.application.tag.TagResult;
import ovh.equino.actracker.domain.tag.*;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.security.identity.IdentityProvider;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Transactional
@Service
class TransactionalTagApplicationService extends TagApplicationService {

    TransactionalTagApplicationService(TagFactory tagFactory,
                                       MetricFactory metricFactory,
                                       TagRepository tagRepository,
                                       TagDataSource tagDataSource,
                                       TagSearchEngine tagSearchEngine,
                                       TagNotifier tagNotifier,
                                       IdentityProvider identityProvider,
                                       TenantDataSource tenantDataSource) {

        super(
                tagFactory,
                metricFactory,
                tagRepository,
                tagDataSource,
                tagSearchEngine,
                tagNotifier,
                identityProvider,
                tenantDataSource
        );
    }

    @Override
    public TagResult createTag(CreateTagCommand createTagCommand) {
        return super.createTag(createTagCommand);
    }

    @Override
    public List<TagResult> resolveTags(Set<UUID> tagIds) {
        return super.resolveTags(tagIds);
    }

    @Override
    public SearchResult<TagResult> searchTags(SearchTagsQuery searchTagsQuery) {
        return super.searchTags(searchTagsQuery);
    }

    @Override
    public TagResult renameTag(String newName, UUID tagId) {
        return super.renameTag(newName, tagId);
    }

    @Override
    public void deleteTag(UUID tagId) {
        super.deleteTag(tagId);
    }

    @Override
    public TagResult addMetricToTag(String metricName, String metricType, UUID tagId) {
        return super.addMetricToTag(metricName, metricType, tagId);
    }

    @Override
    public TagResult deleteMetric(UUID metricId, UUID tagId) {
        return super.deleteMetric(metricId, tagId);
    }

    @Override
    public TagResult renameMetric(String newName, UUID metricId, UUID tagId) {
        return super.renameMetric(newName, metricId, tagId);
    }

    @Override
    public TagResult shareTag(String newGrantee, UUID tagId) {
        return super.shareTag(newGrantee, tagId);
    }

    @Override
    public TagResult unshareTag(String granteeName, UUID tagId) {
        return super.unshareTag(granteeName, tagId);
    }
}
