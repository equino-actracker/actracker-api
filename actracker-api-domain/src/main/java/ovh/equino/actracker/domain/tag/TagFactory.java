package ovh.equino.actracker.domain.tag;

import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;

import static java.lang.Boolean.TRUE;
import static java.util.Collections.emptyList;
import static java.util.Objects.requireNonNullElse;

public final class TagFactory {

    private static final Boolean DELETED = TRUE;

    private final TagDataSource tagDataSource;
    private final TenantDataSource tenantDataSource;

    TagFactory(TagDataSource tagDataSource, TenantDataSource tenantDataSource) {
        this.tagDataSource = tagDataSource;
        this.tenantDataSource = tenantDataSource;
    }

    public Tag create(User creator, String name, Collection<Metric> metrics, Collection<Share> shares) {
        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource, creator);
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
                tagsAccessibilityVerifier,
                validator
        );
        tag.validate();
        return tag;
    }

    // TODO should be user in context, not creator!!!
    public Tag reconstitute(TagId id,
                            User creator,
                            String name,
                            Collection<Metric> metrics,
                            Collection<Share> shares,
                            boolean deleted) {

        var tagsAccessibilityVerifier = new TagsAccessibilityVerifier(tagDataSource, creator);
        var validator = new TagValidator();

        return new Tag(
                id,
                creator,
                name,
                metrics,
                shares,
                deleted,
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
