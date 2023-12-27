package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

class TagFactoryImpl implements TagFactory {

    private static final Boolean DELETED = TRUE;

    private final ActorExtractor actorExtractor;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;
    private final TenantDataSource tenantDataSource;

    TagFactoryImpl(ActorExtractor actorExtractor,
                   TagsAccessibilityVerifier tagsAccessibilityVerifier,
                   TenantDataSource tenantDataSource) {

        this.actorExtractor = actorExtractor;
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.tenantDataSource = tenantDataSource;
    }

    @Override
    public Tag create(String name, Collection<Metric> metrics, Collection<Share> shares) {
        var creator = actorExtractor.getActor();
        var validator = new TagValidator();

        var resolvedShares = requireNonNullElse(shares, new ArrayList<Share>())
                .stream()
                .map(share -> resolveShare(share.granteeName()))
                .toList();

        var tag = new Tag(
                new TagId(),
                creator,
                name,
                requireNonNullElse(metrics, emptyList()),
                resolvedShares,
                !DELETED,
                actorExtractor,
                tagsAccessibilityVerifier,
                validator
        );
        tag.validate();
        return tag;
    }

    @Override
    public Tag reconstitute(TagId id,
                            User creator,
                            String name,
                            Collection<Metric> metrics,
                            Collection<Share> shares,
                            boolean deleted) {

        var validator = new TagValidator();

        return new Tag(
                id,
                creator,
                name,
                metrics,
                shares,
                deleted,
                actorExtractor,
                tagsAccessibilityVerifier,
                validator
        );
    }

    // TODO extract to ShareResolver service
    private Share resolveShare(String grantee) {
        return tenantDataSource.findByUsername(grantee)
                .map(tenant -> new Share(
                        new User(tenant.id()),
                        tenant.username()
                ))
                .orElse(new Share(grantee));
    }
}
