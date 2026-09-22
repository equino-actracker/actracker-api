package ovh.equino.actracker.rest.spring.tag;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.tag.TagApplicationService;

import static org.assertj.core.api.Fail.fail;

@WebMvcTest(TagController.class)
class TagControllerTest {

    private static final String TAG_URL = "/api/tag";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagApplicationService tagApplicationService;

    @Test
    void shouldGetTag() {
        fail("Not yet implemented");
    }
}
