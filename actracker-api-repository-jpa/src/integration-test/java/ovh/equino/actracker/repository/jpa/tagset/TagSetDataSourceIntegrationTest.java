package ovh.equino.actracker.repository.jpa.tagset;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tagset.TagSetId;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.IntegrationTestBase;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

class TagSetDataSourceIntegrationTest extends IntegrationTestBase {

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

    private EntityManager entityManager = entityManager();
    private JpaTagSetDataSource dataSource;

    @BeforeEach
    void setup() {
        this.entityManager = entityManager();
        this.dataSource = new JpaTagSetDataSource(entityManager);
    }

    @BeforeAll
    static void setUp() {
        TenantDto searcherTenant = newUser().build();
        TenantDto sharingUser = newUser().build();
        accessibleOwnTag1 = newTag(searcherTenant).build();
        accessibleOwnTag2 = newTag(searcherTenant).build();
        accessibleSharedTag = newTag(sharingUser).sharedWith(searcherTenant).build();
        inaccessibleOwnDeletedTag = newTag(searcherTenant).deleted().build();
        inaccessibleSharedDeletedTag = newTag(sharingUser).sharedWith(searcherTenant).deleted().build();
        inaccessibleNotSharedTag = newTag(sharingUser).build();

        accessibleOwnTagSet1 = newTagSet(searcherTenant)
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag
                ).build();
        accessibleOwnTagSet2 = newTagSet(searcherTenant)
                .withTags(
                        accessibleOwnTag1,
                        accessibleOwnTag2,
                        inaccessibleOwnDeletedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleNotSharedTag
                )
                .build();
        accessibleOwnTagSet3 = newTagSet(searcherTenant)
                .withTags(
                        inaccessibleOwnDeletedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleNotSharedTag
                )
                .build();
        accessibleOwnTagSet4 = newTagSet(searcherTenant)
                .withTags()
                .build();
        inaccessibleOwnDeletedTagSet = newTagSet(searcherTenant)
                .deleted()
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleOwnDeletedTag,
                        inaccessibleNotSharedTag
                )
                .build();
        inaccessibleForeignTagSet = newTagSet(sharingUser)
                .withTags(
                        accessibleOwnTag1,
                        accessibleSharedTag,
                        inaccessibleSharedDeletedTag,
                        inaccessibleNotSharedTag
                )
                .build();

        searcher = new User(searcherTenant.id());
    }

    @ParameterizedTest
    @MethodSource("accessibleTagSets")
    void shouldFindAccessibleTagSet(TagSetId tagSetId, TagSetDto expectedTagSet) {
        inTransaction(entityManager, () -> {
            Optional<TagSetDto> tagSetDto = dataSource.find(tagSetId, searcher);
            assertThat(tagSetDto).usingRecursiveComparison().isEqualTo(expectedTagSet);
        });
    }

    @ParameterizedTest
    @MethodSource("inaccessibleTagSets")
    void shouldNotFindInaccessibleTagSet(TagSetId tagSetId) {
        inTransaction(entityManager, () -> {
            Optional<TagSetDto> tagSetDto = dataSource.find(tagSetId, searcher);
            assertThat(tagSetDto).isEmpty();
        });
    }

    @Test
    void shouldFindAccessibleTagSets() {
        fail();
    }

    private static Stream<Arguments> accessibleTagSets() {
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
                )
        );
    }

    private static Stream<Arguments> inaccessibleTagSets() {
        return Stream.of(
                Arguments.of(new TagSetId(inaccessibleOwnDeletedTagSet.id())),
                Arguments.of(new TagSetId(inaccessibleForeignTagSet.id()))
        );
    }
}
