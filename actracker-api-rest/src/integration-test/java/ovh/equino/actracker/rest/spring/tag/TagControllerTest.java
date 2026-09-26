package ovh.equino.actracker.rest.spring.tag;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.tag.TagApplicationService;
import ovh.equino.actracker.domain.tag.TagTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;

import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ovh.equino.actracker.domain.tag.TagTestData.minimalTag;
import static ovh.equino.actracker.rest.spring.tag.TagRestTestData.aRestfulTag;

@WebMvcTest(TagController.class)
class TagControllerTest implements ControllerIntegrationTest {

    private static final String TAG_URL = "/api/tag/";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagApplicationService tagApplicationService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("tagsToGet")
    void shouldGetTag(String testName, TagTestData tagToGet) throws Exception {
        // given
        var restfulTag = aRestfulTag(tagToGet);
        when(tagApplicationService.getTag(tagToGet.id()))
                .thenReturn(restfulTag.asTagResult());

        // when / then
        mockMvc.perform(get(TAG_URL + tagToGet.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulTag.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> tagsToGet() {
        return Stream.of(
                Arguments.of("Minimalistic tag", minimalTag()),
                Arguments.of("Full tag", null)
        );
    }
}
