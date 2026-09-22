package ovh.equino.actracker.rest.spring.tagset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;

import static org.assertj.core.api.Fail.fail;

@WebMvcTest(TagSetController.class)
class TagSetControllerTest {

    private static final String TAGSET_URL = "/api/tagset";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagSetApplicationService tagSetApplicationService;

    @Test
    void shouldGetTagSet() {
        fail("Not yet implemented");
    }
}
