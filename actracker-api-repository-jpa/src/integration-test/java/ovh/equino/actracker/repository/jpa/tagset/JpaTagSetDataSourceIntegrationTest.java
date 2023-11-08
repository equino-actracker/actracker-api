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
import ovh.equino.actracker.repository.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;

class JpaTagSetDataSourceIntegrationTest extends JpaIntegrationTest {

    private static TagDto accessibleOwnTag1;
    private static TagDto accessibleOwnTag2;
    private static TagDto accessibleSharedTag;
    private static TagDto inaccessibleOwnDeletedTag;
    private static TagDto inaccessibleSharedDeletedTag;
    private static TagDto inaccessibleNotSharedTag;

    private static TagSetDto accessibleOwnTagSet1;
    private static TagSetDto accessibleOwnTagSet2;
    private static TagSetDto accessibleOwnTagSet3;
    private static TagSetDto accessibleOwnTagSet4;
    private static TagSetDto inaccessibleOwnDeletedTagSet;
    private static TagSetDto inaccessibleForeignTagSet;

    private static User searcher;

    private JpaTagSetDataSource dataSource;

    @BeforeEach
    void setup() {
        this.dataSource = new JpaTagSetDataSource(entityManager);
    }

    @BeforeAll
    static void setUp() throws SQLException {
        TenantDto searcherTenant = newUser().build();
        DATABASE.addUser(searcherTenant);
        TenantDto sharingUser = newUser().build();
        DATABASE.addUser(sharingUser);

        accessibleOwnTag1 = newTag(searcherTenant).build();
        DATABASE.addTag(accessibleOwnTag1);
        accessibleOwnTag2 = newTag(searcherTenant).build();
        DATABASE.addTag(accessibleOwnTag2);
        accessibleSharedTag = newTag(sharingUser).sharedWith(searcherTenant).build();
        DATABASE.addTag(accessibleSharedTag);
        inaccessibleOwnDeletedTag = newTag(searcherTenant).deleted().build();
        DATABASE.addTag(inaccessibleOwnDeletedTag);
        inaccessibleSharedDeletedTag = newTag(sharingUser).sharedWith(searcherTenant).deleted().build();
        DATABASE.addTag(inaccessibleSharedDeletedTag);
        inaccessibleNotSharedTag = newTag(sharingUser).build();
        DATABASE.addTag(inaccessibleNotSharedTag);

        accessibleOwnTagSet1 = newTagSet(searcherTenant)
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag
                ).build();
        DATABASE.addTagSet(accessibleOwnTagSet1);
        accessibleOwnTagSet2 = newTagSet(searcherTenant)
                .withTags(
                        accessibleOwnTag1,
                        accessibleOwnTag2,
                        inaccessibleOwnDeletedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleNotSharedTag
                )
                .build();
        DATABASE.addTagSet(accessibleOwnTagSet2);
        accessibleOwnTagSet3 = newTagSet(searcherTenant)
                .withTags(
                        inaccessibleOwnDeletedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleNotSharedTag
                )
                .build();
        DATABASE.addTagSet(accessibleOwnTagSet3);
        accessibleOwnTagSet4 = newTagSet(searcherTenant)
                .withTags()
                .build();
        DATABASE.addTagSet(accessibleOwnTagSet4);
        inaccessibleOwnDeletedTagSet = newTagSet(searcherTenant)
                .deleted()
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleOwnDeletedTag,
                        inaccessibleNotSharedTag
                )
                .build();
        DATABASE.addTagSet(inaccessibleOwnDeletedTagSet);
        inaccessibleForeignTagSet = newTagSet(sharingUser)
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleNotSharedTag
                )
                .build();
        DATABASE.addTagSet(inaccessibleForeignTagSet);

        searcher = new User(searcherTenant.id());
    }

    @ParameterizedTest
    @MethodSource("accessibleTagSet")
    void shouldFindAccessibleTagSet(TagSetId tagSetId, TagSetDto expectedTagSet) {
        inTransaction(() -> {
            Optional<TagSetDto> tagSetDto = dataSource.find(tagSetId, searcher);
            assertThat(tagSetDto).isPresent();
            assertThat(tagSetDto.get()).usingRecursiveComparison().ignoringFields("tags").isEqualTo(expectedTagSet);
            assertThat(tagSetDto.get().tags()).containsExactlyInAnyOrderElementsOf(expectedTagSet.tags());
        });
    }

    private static Stream<Arguments> accessibleTagSet() {
        return Stream.of(
                Arguments.of(new TagSetId(accessibleOwnTagSet1.id()), new TagSetDto(
                                accessibleOwnTagSet1.id(),
                                accessibleOwnTagSet1.creatorId(),
                                accessibleOwnTagSet1.name(),
                                Set.of(accessibleOwnTag1.id(), accessibleSharedTag.id()),
                                accessibleOwnTagSet1.deleted()
                        )
                ),
                Arguments.of(new TagSetId(accessibleOwnTagSet2.id()), new TagSetDto(
                                accessibleOwnTagSet2.id(),
                                accessibleOwnTagSet2.creatorId(),
                                accessibleOwnTagSet2.name(),
                                Set.of(accessibleOwnTag1.id(), accessibleOwnTag2.id()),
                                accessibleOwnTagSet2.deleted()
                        )
                ),
                Arguments.of(new TagSetId(accessibleOwnTagSet3.id()), new TagSetDto(
                                accessibleOwnTagSet3.id(),
                                accessibleOwnTagSet3.creatorId(),
                                accessibleOwnTagSet3.name(),
                                emptySet(),
                                accessibleOwnTagSet4.deleted()
                        )
                ),
                Arguments.of(new TagSetId(accessibleOwnTagSet4.id()), new TagSetDto(
                                accessibleOwnTagSet4.id(),
                                accessibleOwnTagSet4.creatorId(),
                                accessibleOwnTagSet4.name(),
                                emptySet(),
                                accessibleOwnTagSet4.deleted()
                        )
                )
        );
    }

    @ParameterizedTest
    @MethodSource("inaccessibleTagSet")
    void shouldNotFindInaccessibleTagSet(TagSetId tagSetId) {
        inTransaction(() -> {
            Optional<TagSetDto> tagSetDto = dataSource.find(tagSetId, searcher);
            assertThat(tagSetDto).isEmpty();
        });
    }

    private static Stream<Arguments> inaccessibleTagSet() {
        return Stream.of(
                Arguments.of(new TagSetId(inaccessibleOwnDeletedTagSet.id())),
                Arguments.of(new TagSetId(inaccessibleForeignTagSet.id()))
        );
    }

    @Test
    void shouldFindAllAccessibleTagSets() {
        List<TagSetDto> expectedTagSets = Stream.of(
                        accessibleOwnTagSet1,
                        accessibleOwnTagSet2,
                        accessibleOwnTagSet3,
                        accessibleOwnTagSet4
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
                null,
                null,
                null
        );

        inTransaction(() -> {
            List<TagSetDto> foundTagSets = dataSource.find(searchCriteria);
            assertThat(foundTagSets)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("tags")
                    .containsExactlyElementsOf(expectedTagSets);
            assertThat(foundTagSets)
                    .flatMap(TagSetDto::tags)
                    .containsOnly(
                            accessibleOwnTag1.id(),
                            accessibleOwnTag2.id(),
                            accessibleSharedTag.id()
                    );
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
}
