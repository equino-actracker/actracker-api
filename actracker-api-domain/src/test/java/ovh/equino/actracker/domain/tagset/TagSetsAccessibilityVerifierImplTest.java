package ovh.equino.actracker.domain.tagset;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ovh.equino.actracker.domain.user.User;

import java.util.Optional;

import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TagSetsAccessibilityVerifierImplTest {

    private static final User USER = null;
    private static final TagSetDto ACCESSIBLE_TAG_SET = new TagSetDto(
            randomUUID(),
            randomUUID(),
            "accessible tag set",
            emptySet(),
            false
    );

    @Mock
    private TagSetDataSource tagSetDataSource;
    private TagSetsAccessibilityVerifierImpl tagSetsAccessibilityVerifier;

    @BeforeEach
    void init() {
        tagSetsAccessibilityVerifier = new TagSetsAccessibilityVerifierImpl(tagSetDataSource);
    }

    @Test
    void shouldConfirmTagSetAccessible() {
        // given
        when(tagSetDataSource.find(any(), any())).thenReturn(Optional.of(ACCESSIBLE_TAG_SET));

        // when
        boolean isAccessible = tagSetsAccessibilityVerifier.isAccessibleFor(USER, new TagSetId());

        // then
        assertThat(isAccessible).isTrue();
    }

    @Test
    void shouldConfirmTagSetInaccessible() {
        // given
        when(tagSetDataSource.find(any(), any())).thenReturn(Optional.empty());

        // when
        boolean isAccessible = tagSetsAccessibilityVerifier.isAccessibleFor(USER, new TagSetId());

        // then
        assertThat(isAccessible).isFalse();
    }
}
