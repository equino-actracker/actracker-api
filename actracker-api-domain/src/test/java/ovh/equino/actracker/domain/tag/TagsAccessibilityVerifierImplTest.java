package ovh.equino.actracker.domain.tag;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.user.User;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static ovh.equino.actracker.domain.tag.MetricType.NUMERIC;

@ExtendWith(MockitoExtension.class)
class TagsAccessibilityVerifierImplTest {

    private static final User USER = new User(randomUUID());
    private static final boolean DELETED = true;

    private static final MetricDto ACCESSIBLE_METRIC_1 = metric("accessible metric 1");
    private static final MetricDto ACCESSIBLE_METRIC_2 = metric("accessible metric 2");
    private static final MetricDto INACCESSIBLE_METRIC_1 = metric("inaccessible metric 1");
    private static final MetricDto INACCESSIBLE_METRIC_2 = metric("inaccessible metric 2");

    private static final TagDto ACCESSIBLE_TAG_1 = tag("accessible tag 1", ACCESSIBLE_METRIC_1);
    private static final TagDto ACCESSIBLE_TAG_2 = tag("accessible tag 2", ACCESSIBLE_METRIC_2);
    private static final TagDto INACCESSIBLE_TAG_1 = tag("inaccessible tag 1", INACCESSIBLE_METRIC_1);
    private static final TagDto INACCESSIBLE_TAG_2 = tag("inaccessible tag 2", INACCESSIBLE_METRIC_2);

    private static final TagId ACCESSIBLE_TAG_1_ID = new TagId(ACCESSIBLE_TAG_1.id());
    private static final TagId ACCESSIBLE_TAG_2_ID = new TagId(ACCESSIBLE_TAG_2.id());
    private static final TagId INACCESSIBLE_TAG_1_ID = new TagId(INACCESSIBLE_TAG_1.id());
    private static final TagId INACCESSIBLE_TAG_2_ID = new TagId(INACCESSIBLE_TAG_2.id());

    @Mock
    private TagDataSource tagDataSource;
    private TagsAccessibilityVerifierImpl tagsAccessibilityVerifier;

    @BeforeEach
    void init() {
        tagsAccessibilityVerifier = new TagsAccessibilityVerifierImpl(tagDataSource);
    }

    @Test
    void shouldFindInaccessibleTags() {
        // given
        when(tagDataSource.find(any(Set.class), any(User.class)))
                .thenReturn(List.of(ACCESSIBLE_TAG_1, ACCESSIBLE_TAG_2));

        // when
        Set<TagId> nonAccessibleTags = tagsAccessibilityVerifier.nonAccessibleFor(
                USER,
                List.of(
                        ACCESSIBLE_TAG_1_ID,
                        ACCESSIBLE_TAG_2_ID,
                        INACCESSIBLE_TAG_1_ID,
                        INACCESSIBLE_TAG_2_ID
                )
        );

        // then
        assertThat(nonAccessibleTags).containsExactlyInAnyOrder(INACCESSIBLE_TAG_1_ID, INACCESSIBLE_TAG_2_ID);
    }

    @Test
    void shouldConfirmTagAccessible() {
        // given
        when(tagDataSource.find(any(TagId.class), any(User.class)))
                .thenReturn(Optional.of(ACCESSIBLE_TAG_1));

        // when
        boolean isAccessible = tagsAccessibilityVerifier.isAccessibleFor(USER, ACCESSIBLE_TAG_1_ID);

        // then
        assertThat(isAccessible).isTrue();
    }

    @Test
    void shouldConfirmTagInaccessible() {
        // given
        when(tagDataSource.find(any(TagId.class), any(User.class)))
                .thenReturn(Optional.empty());

        // when
        boolean isAccessible = tagsAccessibilityVerifier.isAccessibleFor(USER, INACCESSIBLE_TAG_1_ID);

        // then
        assertThat(isAccessible).isFalse();
    }

    private static MetricDto metric(String name) {
        return new MetricDto(
                randomUUID(),
                randomUUID(),
                name,
                NUMERIC,
                !DELETED
        );
    }

    private static TagDto tag(String name, MetricDto metric) {
        return new TagDto(
                randomUUID(),
                randomUUID(),
                name,
                singletonList(metric),
                emptyList(),
                !DELETED
        );
    }
}
