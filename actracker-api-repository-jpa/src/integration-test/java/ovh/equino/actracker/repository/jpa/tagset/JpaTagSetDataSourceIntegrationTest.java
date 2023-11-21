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
import ovh.equino.actracker.repository.jpa.IntegrationTestConfiguration;
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;

abstract class JpaTagSetDataSourceIntegrationTest extends JpaIntegrationTest {

    private static TenantDto searcherTenant;
    private static TenantDto sharingUser;

    private static TagDto accessibleOwnTag1;
    private static TagDto accessibleOwnTag2;
    private static TagDto accessibleSharedTag;
    private static TagDto inaccessibleOwnDeletedTag;
    private static TagDto inaccessibleSharedDeletedTag;
    private static TagDto inaccessibleForeignTag;

    private static TagSetDto accessibleOwnTagSet1;
    private static TagSetDto accessibleOwnTagSet2;
    private static TagSetDto accessibleOwnTagSet3;
    private static TagSetDto accessibleOwnTagSet4;
    private static TagSetDto inaccessibleOwnDeletedTagSet;
    private static TagSetDto inaccessibleForeignTagSet;
    private static TagSetDto inaccessibleNotAddedTagSet;

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
        List<TagSetDto> expectedTagSets = Stream.of(
                        accessibleOwnTagSet1,
                        accessibleOwnTagSet2,
                        accessibleOwnTagSet3,
                        accessibleOwnTagSet4
                )
                .sorted(comparing(tagSet -> tagSet.id().toString()))
                .skip(1)
                .toList();

        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                2,
                expectedTagSets.get(0).id().toString(),
                null,
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
                    .containsExactly(expectedTagSets.get(0), expectedTagSets.get(1));
        });
    }

    @Test
    void shouldFindNotExcludedTagSets() {
        List<TagSetDto> expectedTagSets = Stream.of(
                        accessibleOwnTagSet2,
                        accessibleOwnTagSet3
                )
                .sorted(comparing(tagSet -> tagSet.id().toString()))
                .toList();
        EntitySearchCriteria searchCriteria = new EntitySearchCriteria(
                searcher,
                LARGE_PAGE_SIZE,
                FIRST_PAGE,
                null,
                null,
                null,
                Set.of(accessibleOwnTagSet1.id(), accessibleOwnTagSet4.id()),
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

    @BeforeAll
    static void setUp() {
        searcherTenant = newUser().build();
        sharingUser = newUser().build();
        TenantDto grantee1 = newUser().build();
        TenantDto grantee2 = newUser().build();
        searcher = new User(searcherTenant.id());

        testConfiguration.addUser(searcherTenant);
        testConfiguration.addUser(sharingUser);

        accessibleOwnTag1 = newTag(searcherTenant).build();
        accessibleOwnTag2 = newTag(searcherTenant)
                .sharedWith(grantee1, grantee2)
                .sharedWithNonExisting("nonExistingGrantee1", "nonExistingGrantee2")
                .build();
        accessibleSharedTag = newTag(sharingUser).sharedWith(searcherTenant).build();
        inaccessibleOwnDeletedTag = newTag(searcherTenant).deleted().build();
        inaccessibleSharedDeletedTag = newTag(sharingUser).sharedWith(searcherTenant).deleted().build();
        inaccessibleForeignTag = newTag(sharingUser).build();
        TagDto inaccessibleNotPersistedTag = newTag(searcherTenant).named("Inaccessible not persisted tag").build();

        testConfiguration.tags.add(accessibleOwnTag1);
        testConfiguration.tags.add(accessibleOwnTag2);
        testConfiguration.tags.add(accessibleSharedTag);
        testConfiguration.tags.add(inaccessibleOwnDeletedTag);
        testConfiguration.tags.add(inaccessibleSharedDeletedTag);
        testConfiguration.tags.add(inaccessibleForeignTag);
        testConfiguration.tags.addTransient(inaccessibleNotPersistedTag);

        accessibleOwnTagSet1 = newTagSet(searcherTenant)
                .named("accessibleOwnTagSet1")
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleNotPersistedTag
                )
                .build();
        testConfiguration.tagSets.add(accessibleOwnTagSet1);

        accessibleOwnTagSet2 = newTagSet(searcherTenant)
                .named("accessibleOwnTagSet2")
                .withTags(
                        accessibleOwnTag1,
                        accessibleOwnTag2,
                        inaccessibleOwnDeletedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleForeignTag
                )
                .build();
        testConfiguration.tagSets.add(accessibleOwnTagSet2);

        accessibleOwnTagSet3 = newTagSet(searcherTenant)
                .named("accessibleOwnTagSet3")
                .withTags(
                        inaccessibleOwnDeletedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleForeignTag
                )
                .build();
        testConfiguration.tagSets.add(accessibleOwnTagSet3);

        accessibleOwnTagSet4 = newTagSet(searcherTenant)
                .named("accessibleOwnTagSet4")
                .withTags()
                .build();
        testConfiguration.tagSets.add(accessibleOwnTagSet4);

        inaccessibleOwnDeletedTagSet = newTagSet(searcherTenant)
                .named("inaccessibleOwnDeletedTagSet")
                .deleted()
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleOwnDeletedTag,
                        inaccessibleForeignTag
                )
                .build();
        testConfiguration.tagSets.add(inaccessibleOwnDeletedTagSet);

        inaccessibleForeignTagSet = newTagSet(sharingUser)
                .named("inaccessibleForeignTagSet")
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleForeignTag
                )
                .build();
        testConfiguration.tagSets.add(inaccessibleForeignTagSet);

        inaccessibleNotAddedTagSet = newTagSet(searcherTenant)
                .named("inaccessibleNotAddedTagSet")
                .build();
        testConfiguration.tagSets.addTransient(inaccessibleNotAddedTagSet);
    }
}
