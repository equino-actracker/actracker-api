package ovh.equino.actracker.rest.spring.tag;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.application.tag.SearchTagsQuery;
import ovh.equino.actracker.application.tag.TagApplicationService;
import ovh.equino.actracker.application.tag.TagResult;
import ovh.equino.actracker.domain.tag.TagTestData;
import ovh.equino.actracker.rest.spring.ControllerIntegrationTest;
import ovh.equino.actracker.rest.spring.SearchRestTestData;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ovh.equino.actracker.domain.tag.TagTestData.complexTag;
import static ovh.equino.actracker.domain.tag.TagTestData.minimalTag;
import static ovh.equino.actracker.rest.spring.tag.TagRestTestData.aRestfulTag;

@WebMvcTest(TagController.class)
class TagControllerIntegrationTest implements ControllerIntegrationTest {

    private static final String TAG_URL = "/api/tag";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagApplicationService tagApplicationService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("tags")
    void shouldGetTag(String testName, TagTestData tagToGet) throws Exception {
        // given
        var restfulTag = aRestfulTag(tagToGet);
        when(tagApplicationService.getTag(tagToGet.id()))
                .thenReturn(restfulTag.asEntityResult());

        // when / then
        mockMvc.perform(get(TAG_URL + "/" + tagToGet.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulTag.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tags")
    void shouldCreateTag(String testName, TagTestData tagToCreate) throws Exception {
        // given
        var restfulTag = aRestfulTag(tagToCreate);
        when(tagApplicationService.createTag(restfulTag.asCreateTagCommand()))
                .thenReturn(restfulTag.asEntityResult());

        // when / then
        mockMvc.perform(post(TAG_URL)
                        .contentType(APPLICATION_JSON)
                        .content(restfulTag.asHttpRequest()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulTag.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> tags() {
        return Stream.of(
                Arguments.of("Minimalistic tag", minimalTag()),
                Arguments.of("Full tag", complexTag())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searchTagsCriteria")
    void shouldSearchTagsMatchingCriteria(String testName,
                                          SearchTagsQuery expectedQuery,
                                          Map<String, String> searchCriteria)
            throws Exception {

        // given
        // TODO should not be necessary, mapping in controller should handle null
        when(tagApplicationService.searchTags(any(SearchTagsQuery.class)))
                .thenReturn(new SearchResult<>(null, List.of()));

        // when
        var requestBuilder = get(TAG_URL + "/matching");
        searchCriteria.forEach(requestBuilder::queryParam);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        // then
        verify(tagApplicationService).searchTags(expectedQuery);
    }

    private static Stream<Arguments> searchTagsCriteria() {
        return Stream.of(
                Arguments.of("empty criteria",
                        new SearchTagsQuery(null, null, null, null, null),
                        Map.of()),
                Arguments.of("pageSize filter",
                        new SearchTagsQuery(25, null, null, null, null),
                        Map.of("pageSize", "25")),
                Arguments.of("pageId filter",
                        new SearchTagsQuery(null, "page-id", null, null, null),
                        Map.of("pageId", "page-id")),
                Arguments.of("term filter",
                        new SearchTagsQuery(null, null, "gym", null, null),
                        Map.of("term", "gym")),
                Arguments.of("excludedTags filter",
                        new SearchTagsQuery(null, null, null, Set.of(new UUID(1L, 1L)), null),
                        Map.of("excludedTags", new UUID(1L, 1L).toString())),
                Arguments.of("orderBy filter",
                        new SearchTagsQuery(null, null, null, null,
                                new SortCriteria().orderBy("name", "desc")),
                        Map.of("orderBy", "name.desc")),
                Arguments.of("all filters combined",
                        new SearchTagsQuery(10, "p1", "run", Set.of(new UUID(2L, 2L)),
                                new SortCriteria().orderBy("name", "desc")),
                        Map.of("pageSize", "10",
                                "pageId", "p1",
                                "term", "run",
                                "excludedTags", new UUID(2L, 2L).toString(),
                                "orderBy", "name.desc"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matchingTags")
    void shouldFindMatchingTags(String testName, SearchRestTestData<TagResult> expectedSearchResult)
            throws Exception {

        // given
        when(tagApplicationService.searchTags(any(SearchTagsQuery.class)))
                .thenReturn(expectedSearchResult.asEntitySearchResult());

        // when / then
        mockMvc.perform(get(TAG_URL + "/matching"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        expectedSearchResult.asHttpResponse(),
                        NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER)
                );
    }

    private static Stream<Arguments> matchingTags() {
        return Stream.of(
                Arguments.of("no matching tags and no next page",
                        new SearchRestTestData<TagResult>(null, List.of())),
                Arguments.of("single minimal tag and no next page",
                        new SearchRestTestData<>(null, List.of(aRestfulTag(minimalTag())))),
                Arguments.of("three complex tags and next page",
                        new SearchRestTestData<>("next-page-id", List.of(
                                aRestfulTag(complexTag()),
                                aRestfulTag(complexTag()),
                                aRestfulTag(complexTag()))))
        );
    }
}
