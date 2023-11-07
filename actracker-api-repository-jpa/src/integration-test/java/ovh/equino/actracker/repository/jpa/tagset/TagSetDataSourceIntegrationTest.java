package ovh.equino.actracker.repository.jpa.tagset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tagset.TagSetDto;
import ovh.equino.actracker.domain.tenant.TenantDto;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.repository.jpa.IntegrationTestBase;

import static org.junit.jupiter.api.Assertions.fail;

class TagSetDataSourceIntegrationTest extends IntegrationTestBase {

    private User searcher;

    private TagDto accessibleOwnTag1;
    private TagDto accessibleOwnTag2;
    private TagDto accessibleSharedTag;
    private TagDto inaccessibleOwnDeletedTag;
    private TagDto inaccessibleSharedDeletedTag;
    private TagDto inaccessibleNotSharedTag;

    private TagSetDto accessibleOwnTagSet1;
    private TagSetDto accessibleOwnTagSet2;
    private TagSetDto inaccessibleOwnDeletedTagSet;
    private TagSetDto inaccessibleNotSharedTagSet;

    @BeforeEach
    void setUp() {
        TenantDto searcherTenant = newUser();
        TenantDto sharingUser = newUser();
        accessibleOwnTag1 = newTag(searcherTenant).build();
        accessibleOwnTag2 = newTag(searcherTenant).build();
        accessibleSharedTag = newTag(sharingUser).sharedWith(searcherTenant).build();
        inaccessibleOwnDeletedTag = newTag(searcherTenant).deleted().build();
        inaccessibleSharedDeletedTag = newTag(sharingUser).sharedWith(searcherTenant).deleted().build();
        inaccessibleNotSharedTag = newTag(sharingUser).build();
    }

    @Test
    void shouldFindAccessibleTagSet() {
        fail();
    }

    @Test
    void shouldNotFindInaccessibleTagSet() {
        fail();
    }

    @Test
    void shouldFindAccessibleTagSets() {
        fail();
    }
}
