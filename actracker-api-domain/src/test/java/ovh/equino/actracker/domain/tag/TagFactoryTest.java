package ovh.equino.actracker.domain.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.exception.EntityInvalidException;
import ovh.equino.actracker.domain.share.Share;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.ActorExtractor;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;

import static java.lang.Boolean.TRUE;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static ovh.equino.actracker.domain.tag.MetricType.NUMERIC;

@ExtendWith(MockitoExtension.class)
class TagFactoryTest {

    private static final TagId TAG_ID = new TagId();
    private static final User CREATOR = new User(randomUUID());
    private static final String TAG_NAME = "tag name";
    private static final Boolean DELETED = TRUE;

    @Mock
    private ActorExtractor actorExtractor;
    @Mock
    private TagsAccessibilityVerifier tagsAccessibilityVerifier;
    @Mock
    private TenantDataSource tenantDataSource;

    private TagFactory tagFactory;

    @BeforeEach
    void init() {
        tagFactory = new TagFactory(actorExtractor, tagsAccessibilityVerifier, tenantDataSource);
    }

    @Test
    void shouldCreateMinimalTag() {
        // when
        var tag = tagFactory.create(CREATOR, TAG_NAME, null, null);

        // then
        assertThat(tag.id()).isNotNull();
        assertThat(tag.name()).isEqualTo(TAG_NAME);
        assertThat(tag.creator()).isEqualTo(CREATOR);
        assertThat(tag.metrics()).isEmpty();
        assertThat(tag.shares()).isEmpty();
        assertThat(tag.deleted()).isFalse();
    }

    @Test
    void shouldCreateFullTag() {
        // given
        var metric1 = new Metric(new MetricId(), CREATOR, "metric1", NUMERIC, !DELETED);
        var metric2 = new Metric(new MetricId(), CREATOR, "metric1", NUMERIC, !DELETED);
        var share1 = new Share("grantee1");
        var share2 = new Share("grantee2");

        // when
        var tag = tagFactory.create(
                CREATOR,
                TAG_NAME,
                List.of(metric1, metric2),
                List.of(share1, share2)
        );

        // then
        assertThat(tag.id()).isNotNull();
        assertThat(tag.name()).isEqualTo(TAG_NAME);
        assertThat(tag.creator()).isEqualTo(CREATOR);
        assertThat(tag.metrics()).containsExactlyInAnyOrder(metric1, metric2);
        assertThat(tag.shares()).containsExactlyInAnyOrder(share1, share2);
        assertThat(tag.deleted()).isFalse();
    }

    @Test
    void shouldCreateTagWithResolvedShares() {
        // given
        var grantee1Id = randomUUID();
        var grantee1 = "grantee1";
        var grantee2 = "grantee2";
        var resolvedShare = new Share(new User(grantee1Id), grantee1);
        var unresolvedShare = new Share(grantee2);
        when(tenantDataSource.findByUsername(grantee1))
                .thenReturn(Optional.of(new TenantDto(grantee1Id, "grantee1", "")));

        // when
        var tag = tagFactory.create(CREATOR, TAG_NAME, null, List.of(new Share(grantee1), new Share(grantee2)));

        // then
        assertThat(tag.shares()).containsExactlyInAnyOrder(resolvedShare, unresolvedShare);
    }

    @Test
    void shouldCreateFailWhenTagInvalid() {
        // given
        var invalidTagName = "";

        // then
        assertThatThrownBy(() -> tagFactory.create(CREATOR, invalidTagName, null, null))
                .isInstanceOf(EntityInvalidException.class);
    }

    @Test
    void shouldReconstituteTag() {
        // given
        var shares = List.of(new Share("grantee1"), new Share("grantee2"));
        var metrics = List.of(
                new Metric(new MetricId(), CREATOR, "metric1", NUMERIC, DELETED),
                new Metric(new MetricId(), CREATOR, "metric2", NUMERIC, !DELETED)
        );

        // when
        var tag = tagFactory.reconstitute(TAG_ID, CREATOR, TAG_NAME, metrics, shares, DELETED);

        // then
        assertThat(tag.id()).isEqualTo(TAG_ID);
        assertThat(tag.creator()).isEqualTo(CREATOR);
        assertThat(tag.name()).isEqualTo(TAG_NAME);
        assertThat(tag.metrics()).containsExactlyInAnyOrderElementsOf(metrics);
        assertThat(tag.shares()).containsExactlyInAnyOrderElementsOf(shares);
    }
}