package ovh.equino.actracker.rest.spring.tagset;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;
import ovh.equino.actracker.domain.tagset.TagSetTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ovh.equino.actracker.domain.tagset.TagSetTestData.minimalTagSet;
import static ovh.equino.actracker.rest.spring.tagset.TagSetRestTestData.aRestfulTagSet;

@WebMvcTest(TagSetController.class)
class TagSetControllerTest implements ControllerIntegrationTest {

    private static final String TAGSET_URL = "/api/tag-set/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagSetApplicationService tagSetApplicationService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("tagSetsToGet")
    void shouldGetTagSet(String testName, TagSetTestData tagSetToGet) throws Exception {
        // given
        var restfulTagSet = aRestfulTagSet(tagSetToGet);
        when(tagSetApplicationService.getTagSet(tagSetToGet.id()))
                .thenReturn(restfulTagSet.asTagSetResult());

        // when / then
        mockMvc.perform(get(TAGSET_URL + tagSetToGet.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulTagSet.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> tagSetsToGet() {
        return Stream.of(
                Arguments.of("Minimalistic tag set", minimalTagSet()),
                Arguments.of("Full tag set", null)
        );
    }
}
