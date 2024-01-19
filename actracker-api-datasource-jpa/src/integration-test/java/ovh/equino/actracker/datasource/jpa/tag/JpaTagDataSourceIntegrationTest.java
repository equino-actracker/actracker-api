package ovh.equino.actracker.datasource.jpa.tag;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.datasource.jpa.tag.JpaTagDataSource;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.IntegrationTestConfiguration;
import ovh.equino.actracker.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;

abstract class JpaTagDataSourceIntegrationTest extends JpaIntegrationTest {

    private static final IntegrationTestConfiguration testConfiguration = new IntegrationTestConfiguration();
    private static User searcher;
    private JpaTagDataSource dataSource;

    @BeforeEach
    void init() throws SQLException {
        this.dataSource = new JpaTagDataSource(entityManager);
        testConfiguration.persistIn(database());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("accessibleTag")
    void shouldFindAccessibleTag(String testName, TagId tagId, TagDto expectedTag) {
        inTransaction(() -> {
            Optional<TagDto> foundTag = dataSource.find(tagId, searcher);
            assertThat(foundTag).isPresent();
            assertThat(foundTag.get())
                    .usingRecursiveComparison()
                    .ignoringFields("metrics", "shares")
                    .isEqualTo(expectedTag);
            assertThat(foundTag.get().metrics()).containsExactlyInAnyOrderElementsOf(expectedTag.metrics());
            assertThat(foundTag.get().shares()).containsExactlyInAnyOrderElementsOf(expectedTag.shares());
        });
    }

    private static Stream<Arguments> accessibleTag() {
        return testConfiguration.tags.accessibleFor(searcher)
                .stream()
                .map(tag -> Arguments.of(
                        tag.name(), new TagId(tag.id()), tag
                ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inaccessibleTag")
    void shouldNotFindInaccessibleTag(String testName, TagId tagId) {
        inTransaction(() -> {
            Optional<TagDto> foundTag = dataSource.find(tagId, searcher);
            assertThat(foundTag).isEmpty();
        });
    }

    private static Stream<Arguments> inaccessibleTag() {
        return testConfiguration.tags.inaccessibleFor(searcher)
                .stream()
                .map(tag -> Arguments.of(
                        tag.name(), new TagId(tag.id())
                ));
    }

    @Test
    void shouldFindAllAccessibleTags() {
        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                null,
                null
        );

        inTransaction(() -> {
            List<TagDto> foundTags = dataSource.find(searchCriteria);
            assertThat(foundTags)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("shares", "metrics")
                    .containsExactlyElementsOf(testConfiguration.tags.accessibleFor(searcher));
            assertThat(foundTags)
                    .flatMap(TagDto::metrics)
                    .containsExactlyInAnyOrderElementsOf(testConfiguration.tags.flatMetricsAccessibleFor(searcher));
            assertThat(foundTags)
                    .flatMap(TagDto::shares)
                    .containsExactlyInAnyOrderElementsOf(testConfiguration.tags.flatSharesAccessibleFor(searcher));
        });
    }

    @Test
    void shouldFindSecondPageOfTags() {
        int pageSize = 2;
        int offset = 1;
        List<TagDto> expectedTags = testConfiguration.tags.accessibleForWithLimitOffset(searcher, pageSize, offset);
        String pageId = expectedTags.get(0).id().toString();
        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                pageSize,
                pageId,
                null,
                null,
                null,
                null,
                null
        );
        inTransaction(() -> {
            List<TagDto> foundTags = dataSource.find(searchCriteria);
            assertThat(foundTags)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("shares", "metrics")
                    .containsExactlyElementsOf(expectedTags);
        });
    }

    @Test
    void shouldFindNotExcludedTags() {
        List<TagDto> allAccessibleTags = testConfiguration.tags.accessibleFor(searcher);
        Set<UUID> excludedTags = Set.of(allAccessibleTags.get(1).id(), allAccessibleTags.get(2).id());
        List<TagDto> expectedTags = testConfiguration.tags.accessibleForExcluding(searcher, excludedTags);
        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                excludedTags,
                null
        );
        inTransaction(() -> {
            List<TagDto> foundTags = dataSource.find(searchCriteria);
            assertThat(foundTags)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("shares", "metrics")
                    .containsExactlyElementsOf(expectedTags);
        });
    }

    @Test
    void shouldFindTagsMatchingTerm() {
        String term = "Accessible shared";
        List<TagDto> expectedTags = testConfiguration.tags.accessibleForMatchingTerm(searcher, term);
        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                term,
                null,
                null,
                null,
                null
        );
        inTransaction(() -> {
            List<TagDto> foundTags = dataSource.find(searchCriteria);
            assertThat(foundTags)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("shares", "metrics")
                    .containsExactlyElementsOf(expectedTags);
        });
    }

    @Test
    void shouldFindTagsByIds() {
        List<TagDto> accessibleTags = testConfiguration.tags.accessibleFor(searcher);
        Collection<TagDto> inaccessibleTags = testConfiguration.tags.inaccessibleFor(searcher);
        Set<TagId> allTagIds = concat(accessibleTags.stream(), inaccessibleTags.stream())
                .map(TagDto::id)
                .map(TagId::new)
                .collect(toUnmodifiableSet());

        inTransaction(() -> {
            List<TagDto> foundTags = dataSource.find(allTagIds, searcher);
            assertThat(foundTags)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("shares", "metrics")
                    .containsExactlyElementsOf(accessibleTags);
            assertThat(foundTags)
                    .flatMap(TagDto::metrics)
                    .containsExactlyInAnyOrderElementsOf(testConfiguration.tags.flatMetricsAccessibleFor(searcher));
            assertThat(foundTags)
                    .flatMap(TagDto::shares)
                    .containsExactlyInAnyOrderElementsOf(testConfiguration.tags.flatSharesAccessibleFor(searcher));
        });
    }

    @BeforeAll
    static void setUp() {
        TenantDto searcherTenant = newUser().build();
        TenantDto sharingTenant = newUser().build();
        TenantDto grantee1 = newUser().build();
        TenantDto grantee2 = newUser().build();
        searcher = new User(searcherTenant.id());

        testConfiguration.addUser(searcherTenant);
        testConfiguration.addUser(sharingTenant);

        testConfiguration.tags.add(newTag(searcherTenant)
                .named("Accessible own tag without metrics")
                .withMetrics()
                .sharedWith()
                .build()
        );

        testConfiguration.tags.add(newTag(searcherTenant)
                .named("Accessible own tag with metrics")
                .withMetrics(newMetric(searcherTenant).build(), newMetric(searcherTenant).build())
                .sharedWith(grantee1, grantee2)
                .build()
        );

        testConfiguration.tags.add(newTag(searcherTenant)
                .named("Accessible own tag with deleted metric shared with non existing users")
                .withMetrics(newMetric(searcherTenant).deleted().build())
                .sharedWithNonExisting("nonExistingGrantee1", "nonExistingGrantee2")
                .build()
        );

        testConfiguration.tags.add(newTag(searcherTenant)
                .named("Inaccessible own deleted tag")
                .deleted()
                .build()
        );

        testConfiguration.tags.add(newTag(sharingTenant)
                .named("Accessible shared tag with metrics")
                .withMetrics(newMetric(sharingTenant).build(), newMetric(sharingTenant).build())
                .sharedWith(searcherTenant, grantee1, grantee2)
                .build()
        );

        testConfiguration.tags.add(newTag(sharingTenant)
                .named("Inaccessible shared deleted tag")
                .sharedWith(searcherTenant)
                .deleted()
                .build()
        );

        testConfiguration.tags.add(newTag(sharingTenant)
                .named("Inaccessible foreign tag")
                .build()
        );

        testConfiguration.tags.addTransient(newTag(searcherTenant)
                .named("Inaccessible not persisted tag")
                .build()
        );
    }
}
