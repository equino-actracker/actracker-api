package ovh.equino.actracker.repository.jpa.tagset;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.jpa.IntegrationTestConfiguration;
import ovh.equino.actracker.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

abstract class JpaTagSetDataSourceIntegrationTest extends JpaIntegrationTest {

    private static final IntegrationTestConfiguration testConfiguration = new IntegrationTestConfiguration();
    private static User searcher;
    private JpaTagSetDataSource dataSource;

    @BeforeEach
    void init() throws SQLException {
        this.dataSource = new JpaTagSetDataSource(entityManager);
        testConfiguration.persistIn(database());
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("accessibleTagSet")
    void shouldFindAccessibleTagSet(String testName, TagSetId tagSetId, TagSetDto expectedTagSet) {
        inTransaction(() -> {
            Optional<TagSetDto> foundTagSet = dataSource.find(tagSetId, searcher);
            assertThat(foundTagSet).isPresent();
            assertThat(foundTagSet.get()).usingRecursiveComparison().ignoringFields("tags").isEqualTo(expectedTagSet);
            assertThat(foundTagSet.get().tags()).containsExactlyInAnyOrderElementsOf(expectedTagSet.tags());
        });
    }

    private static Stream<Arguments> accessibleTagSet() {
        return testConfiguration.tagSets.accessibleFor(searcher)
                .stream()
                .map(tagSet -> Arguments.of(
                        tagSet.name(),
                        new TagSetId(tagSet.id()),
                        tagSet
                ));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inaccessibleTagSet")
    void shouldNotFindInaccessibleTagSet(String testName, TagSetId tagSetId) {
        inTransaction(() -> {
            Optional<TagSetDto> foundTagSet = dataSource.find(tagSetId, searcher);
            assertThat(foundTagSet).isEmpty();
        });
    }

    private static Stream<Arguments> inaccessibleTagSet() {
        return testConfiguration.tagSets.inaccessibleFor(searcher)
                .stream()
                .map(tagSet -> Arguments.of(
                        tagSet.name(),
                        new TagSetId(tagSet.id())
                ));
    }

    @Test
    void shouldFindAllAccessibleTagSets() {
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
            List<TagSetDto> foundTagSets = dataSource.find(searchCriteria);
            assertThat(foundTagSets)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags")
                    .containsExactlyElementsOf(testConfiguration.tagSets.accessibleFor(searcher));
            assertThat(foundTagSets)
                    .flatMap(TagSetDto::tags)
                    .containsExactlyInAnyOrderElementsOf(testConfiguration.tagSets.flatTagsAccessibleFor(searcher));
        });
    }

    @Test
    void shouldFindSecondPageOfTagSets() {
        int pageSize = 2;
        int offset = 1;
        List<TagSetDto> expectedTagSets = testConfiguration.tagSets
                .accessibleForWithLimitOffset(searcher, pageSize, offset);
        String pageId = expectedTagSets.get(0).id().toString();

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
            List<TagSetDto> foundTagSets = dataSource.find(searchCriteria);
            assertThat(foundTagSets)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags")
                    .containsExactlyElementsOf(expectedTagSets);
        });
    }

    @Test
    void shouldFindNotExcludedTagSets() {
        List<TagSetDto> allAccessibleTagSets = testConfiguration.tagSets.accessibleFor(searcher);
        Set<UUID> excludedTagSets = Set.of(allAccessibleTagSets.get(1).id(), allAccessibleTagSets.get(3).id());
        List<TagSetDto> expectedTagSets = testConfiguration.tagSets.accessibleForExcluding(searcher, excludedTagSets);
        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                excludedTagSets,
                null
        );
        inTransaction(() -> {
            List<TagSetDto> foundTagSets = dataSource.find(searchCriteria);
            assertThat(foundTagSets)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags")
                    .containsExactlyElementsOf(expectedTagSets);
        });
    }

    @BeforeAll
    static void setUp() {
        TenantDto searcherTenant = newUser().build();
        TenantDto sharingUser = newUser().build();
        TenantDto grantee1 = newUser().build();
        TenantDto grantee2 = newUser().build();
        searcher = new User(searcherTenant.id());

        testConfiguration.addUser(searcherTenant);
        testConfiguration.addUser(sharingUser);

        TagDto accessibleOwnTag1 = newTag(searcherTenant).build();
        TagDto accessibleOwnTag2 = newTag(searcherTenant)
                .sharedWith(grantee1, grantee2)
                .sharedWithNonExisting("nonExistingGrantee1", "nonExistingGrantee2")
                .build();
        TagDto accessibleSharedTag = newTag(sharingUser).sharedWith(searcherTenant).build();
        TagDto inaccessibleOwnDeletedTag = newTag(searcherTenant).deleted().build();
        TagDto inaccessibleSharedDeletedTag = newTag(sharingUser).sharedWith(searcherTenant).deleted().build();
        TagDto inaccessibleForeignTag = newTag(sharingUser).build();
        TagDto inaccessibleNotPersistedTag = newTag(searcherTenant).named("Inaccessible not persisted tag").build();

        testConfiguration.tags.add(accessibleOwnTag1);
        testConfiguration.tags.add(accessibleOwnTag2);
        testConfiguration.tags.add(accessibleSharedTag);
        testConfiguration.tags.add(inaccessibleOwnDeletedTag);
        testConfiguration.tags.add(inaccessibleSharedDeletedTag);
        testConfiguration.tags.add(inaccessibleForeignTag);
        testConfiguration.tags.addTransient(inaccessibleNotPersistedTag);

        testConfiguration.tagSets.add(newTagSet(searcherTenant)
                .named("accessibleOwnTagSet1")
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleNotPersistedTag
                )
                .build());

        testConfiguration.tagSets.add(newTagSet(searcherTenant)
                .named("accessibleOwnTagSet2")
                .withTags(
                        accessibleOwnTag1,
                        accessibleOwnTag2,
                        inaccessibleOwnDeletedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleForeignTag
                )
                .build());

        testConfiguration.tagSets.add(newTagSet(searcherTenant)
                .named("accessibleOwnTagSet3")
                .withTags(
                        inaccessibleOwnDeletedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleForeignTag
                )
                .build());

        testConfiguration.tagSets.add(newTagSet(searcherTenant)
                .named("accessibleOwnTagSet4")
                .withTags()
                .build());

        testConfiguration.tagSets.add(newTagSet(searcherTenant)
                .named("inaccessibleOwnDeletedTagSet")
                .deleted()
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleOwnDeletedTag,
                        inaccessibleForeignTag
                )
                .build());

        testConfiguration.tagSets.add(newTagSet(sharingUser)
                .named("inaccessibleForeignTagSet")
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleForeignTag
                )
                .build());

        testConfiguration.tagSets.addTransient(newTagSet(searcherTenant)
                .named("inaccessibleNotAddedTagSet")
                .build());
    }
}
