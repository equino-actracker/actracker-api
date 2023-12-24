package ovh.equino.actracker.domain.dashboard;

import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static java.lang.Boolean.TRUE;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.toUnmodifiableSet;

public final class DashboardFactory {

    private static final Boolean DELETED = TRUE;

    private final TenantDataSource tenantDataSource;

    private final ActorExtractor actorExtractor;
    private final DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier;
    private final TagsAccessibilityVerifier tagsAccessibilityVerifier;

    DashboardFactory(ActorExtractor actorExtractor,
                     DashboardsAccessibilityVerifier dashboardsAccessibilityVerifier,
                     TagsAccessibilityVerifier tagsAccessibilityVerifier,
                     TenantDataSource tenantDataSource) {

        this.actorExtractor = actorExtractor;
        this.dashboardsAccessibilityVerifier = dashboardsAccessibilityVerifier;
        this.tagsAccessibilityVerifier = tagsAccessibilityVerifier;
        this.tenantDataSource = tenantDataSource;
    }

    public Dashboard create(User creator,
                            String name,
                            Collection<Chart> charts,
                            Collection<Share> shares) {

        var validator = new DashboardValidator();

        var nonNullCharts = requireNonNullElse(charts, new ArrayList<Chart>());
        validateTagsAccessibleFor(creator, nonNullCharts);

        var resolvedShares = requireNonNullElse(shares, new ArrayList<Share>())
                .stream()
                .map(share -> resolveShare(share.granteeName()))
                .toList();

        var dashboard = new Dashboard(
                new DashboardId(),
                creator,
                name,
                nonNullCharts,
                resolvedShares,
                !DELETED,
                dashboardsAccessibilityVerifier,
                tagsAccessibilityVerifier,
                validator
        );
        dashboard.validate();
        return dashboard;
    }

    public Dashboard reconstitute(DashboardId id,
                                  User creator,
                                  String name,
                                  Collection<Chart> charts,
                                  Collection<Share> shares,
                                  boolean deleted) {

        var validator = new DashboardValidator();

        return new Dashboard(
                id,
                creator,
                name,
                new ArrayList<>(charts),
                new ArrayList<>(shares),
                deleted,
                dashboardsAccessibilityVerifier,
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

    private void validateTagsAccessibleFor(User user, Collection<Chart> charts) {

        Set<TagId> includedTags = charts
                .stream()
                .flatMap(chart -> chart.includedTags().stream())
                .map(TagId::new)
                .collect(toUnmodifiableSet());
        tagsAccessibilityVerifier.nonAccessibleFor(user, includedTags)
                .stream()
                .findFirst()
                .ifPresent((inaccessibleTag) -> {
                    String errorMessage = "Tag with ID %s not found".formatted(inaccessibleTag.id());
                    throw new EntityInvalidException(Dashboard.class, errorMessage);
                });
    }
}
