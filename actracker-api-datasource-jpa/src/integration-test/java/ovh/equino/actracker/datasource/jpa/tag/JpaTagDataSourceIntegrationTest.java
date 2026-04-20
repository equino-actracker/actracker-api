package ovh.equino.actracker.datasource.jpa.tag;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchPageId;
import ovh.equino.actracker.domain.EntitySearchPageId.Value;
import ovh.equino.actracker.domain.EntitySortCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.tag.TagSearchCriteria;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.IntegrationTestConfiguration;
import ovh.equino.actracker.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static java.util.stream.Stream.concat;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.domain.EntitySearchPageId.aPageId;
import static ovh.equino.actracker.domain.EntitySearchPageId.firstPage;
import static ovh.equino.actracker.domain.EntitySortCriteria.CommonField.ID;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.ASC;
import static ovh.equino.actracker.domain.EntitySortCriteria.Order.DESC;
import static ovh.equino.actracker.domain.EntitySortCriteria.sortBy;
import static ovh.equino.actracker.domain.tag.TagSearchCriteria.SortableField.NAME;
import static ovh.equino.actracker.jpa.tag.TagTestData.aTag;

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
        var searchCriteria = new TagSearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        LARGE_PAGE_SIZE,
                        FIRST_PAGE,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                null
        );

        inTransaction(() -> {
            var foundTags = dataSource.find(searchCriteria);
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
        var pageSize = 2;
        var offset = 1;
        var expectedTags = testConfiguration.tags.accessibleForWithLimitOffset(searcher, pageSize, offset);
        var pageId = aPageId().with(Value.of(ID, ASC, expectedTags.get(0).id().toString()));
        var searchCriteria = new TagSearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        pageSize,
                        pageId,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                null
        );
        inTransaction(() -> {
            var foundTags = dataSource.find(searchCriteria);
            assertThat(foundTags)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("shares", "metrics")
                    .containsExactlyElementsOf(expectedTags);
        });
    }

    @Test
    void shouldFindNotExcludedTags() {
        var allAccessibleTags = testConfiguration.tags.accessibleFor(searcher);
        var excludedTags = Set.of(allAccessibleTags.get(1).id(), allAccessibleTags.get(2).id());
        var expectedTags = testConfiguration.tags.accessibleForExcluding(searcher, excludedTags);
        var searchCriteria = new TagSearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        LARGE_PAGE_SIZE,
                        FIRST_PAGE,
                        EntitySortCriteria.irrelevant()
                ),
                null,
                excludedTags
        );
        inTransaction(() -> {
            var foundTags = dataSource.find(searchCriteria);
            assertThat(foundTags)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("shares", "metrics")
                    .containsExactlyElementsOf(expectedTags);
        });
    }

    @Test
    void shouldFindTagsMatchingTerm() {
        var term = "Accessible shared";
        var expectedTags = testConfiguration.tags.accessibleForMatchingTerm(searcher, term);
        var searchCriteria = new TagSearchCriteria(
                new EntitySearchCriteria.Common(
                        searcher,
                        LARGE_PAGE_SIZE,
                        FIRST_PAGE,
                        EntitySortCriteria.irrelevant()
                ),
                term,
                null
        );
        inTransaction(() -> {
            var foundTags = dataSource.find(searchCriteria);
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("tagsSortedAndPaginated")
    void shouldFindTagsSortedAndPaginated(String name,
                                          User searcher,
                                          Collection<TagDto> existingTags,
                                          EntitySortCriteria sortCriteria,
                                          Collection<TagDto> expectedFirstPage,
                                          EntitySearchPageId secondPageId,
                                          Collection<TagDto> expectedSecondPage) throws SQLException {

        // given
        database().addUsers(new TenantDto(searcher.id(), "username", "password"));
        database().addTags(existingTags);
        var pageSize = expectedFirstPage.size();

        inTransaction(() -> {
            // when
            var firstPageId = firstPage();
            var searchCriteria = new TagSearchCriteria(
                    new EntitySearchCriteria.Common(
                            searcher,
                            pageSize,
                            firstPageId,
                            sortCriteria
                    ),
                    null,
                    emptySet()
            );
            var foundFirstPage = dataSource.find(searchCriteria);

            // then
            assertThat(foundFirstPage).containsExactlyElementsOf(expectedFirstPage);
        });

        // and
        inTransaction(() -> {
            // when
            var searchCriteria = new TagSearchCriteria(
                    new EntitySearchCriteria.Common(
                            searcher,
                            pageSize,
                            secondPageId,
                            sortCriteria
                    ),
                    null,
                    emptySet()
            );
            var foundSecondPage = dataSource.find(searchCriteria);

            // then
            assertThat(foundSecondPage).containsExactlyElementsOf(expectedSecondPage);
        });
    }

    static Stream<Arguments> tagsSortedAndPaginated() {
        var user = new User(randomUUID());

        var tag1 = aTag().createdBy(user.id()).withId(new UUID(100, 1)).named("a tag").built();
        var tag2 = aTag().createdBy(user.id()).withId(new UUID(100, 2)).named("a tag").built();
        var tag3 = aTag().createdBy(user.id()).withId(new UUID(100, 3)).named("B tag").built();

        return Stream.of(
                Arguments.of(
                        "No Sort",
                        user,
                        List.of(tag1, tag2, tag3),
                        EntitySortCriteria.irrelevant(),
                        List.of(tag1, tag2),
                        aPageId().with(Value.of(ID, ASC, tag3.id())),
                        List.of(tag3)
                ),
                Arguments.of(
                        "NAME:ASC",
                        user,
                        List.of(tag1, tag2, tag3),
                        sortBy(NAME, ASC),
                        List.of(tag3, tag1),
                        aPageId()
                                .with(Value.of(NAME, ASC, tag2.name()))
                                .with(Value.of(ID, ASC, tag2.id())),
                        List.of(tag2)
                ),
                Arguments.of(
                        "NAME:DESC",
                        user,
                        List.of(tag1, tag2, tag3),
                        sortBy(NAME, ASC),
                        List.of(tag2, tag1),
                        aPageId()
                                .with(Value.of(NAME, DESC, tag3.name()))
                                .with(Value.of(ID, ASC, tag3.id())),
                        List.of(tag3)
                ),
                Arguments.of(
                        "All criteria: [NAME:ASC]",
                        user,
                        List.of(tag1, tag2, tag3),
                        sortBy(NAME, ASC),
                        List.of(tag3, tag1),
                        aPageId()
                                .with(Value.of(NAME, ASC, tag2.name()))
                                .with(Value.of(ID, ASC, tag2.id())),
                        List.of(tag2)
                )
        );
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
