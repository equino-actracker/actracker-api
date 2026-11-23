package ovh.equino.actracker.rest.spring.tagset;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import ovh.equino.actracker.application.SearchResult;
import ovh.equino.actracker.application.SortCriteria;
import ovh.equino.actracker.application.tagset.SearchTagSetsQuery;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;
import ovh.equino.actracker.application.tagset.TagSetResult;
import ovh.equino.actracker.domain.tagset.TagSetTestData;
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
import static ovh.equino.actracker.domain.tagset.TagSetTestData.complexTagSet;
import static ovh.equino.actracker.domain.tagset.TagSetTestData.minimalTagSet;
import static ovh.equino.actracker.rest.spring.tagset.TagSetRestTestData.aRestfulTagSet;

@WebMvcTest(TagSetController.class)
class TagSetControllerIntegrationTest implements ControllerIntegrationTest {

    private static final String TAGSET_URL = "/api/tag-set";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TagSetApplicationService tagSetApplicationService;

    @ParameterizedTest(name = "{0}")
    @MethodSource("tagSets")
    void shouldGetTagSet(String testName, TagSetTestData tagSetToGet) throws Exception {
        // given
        var restfulTagSet = aRestfulTagSet(tagSetToGet);
        when(tagSetApplicationService.getTagSet(tagSetToGet.id()))
                .thenReturn(restfulTagSet.asEntityResult());

        // when / then
        mockMvc.perform(get(TAGSET_URL + "/" + tagSetToGet.id()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulTagSet.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("tagSets")
    void shouldCreateTagSet(String testName, TagSetTestData tagSetToCreate) throws Exception {
        // given
        var restfulTagSet = aRestfulTagSet(tagSetToCreate);
        when(tagSetApplicationService.createTagSet(restfulTagSet.asCreateTagSetCommand()))
                .thenReturn(restfulTagSet.asEntityResult());

        // when / then
        mockMvc.perform(post(TAGSET_URL)
                        .contentType(APPLICATION_JSON)
                        .content(restfulTagSet.asHttpRequest()))
                .andExpect(status().isOk())
                .andExpect(content().json(restfulTagSet.asHttpResponse(), NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER));
    }

    private static Stream<Arguments> tagSets() {
        return Stream.of(
                Arguments.of("Minimalistic tag set", minimalTagSet()),
                Arguments.of("Full tag set", complexTagSet())
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("searchTagSetsCriteria")
    void shouldSearchTagSetsMatchingCriteria(String testName,
                                             SearchTagSetsQuery expectedQuery,
                                             Map<String, String> searchCriteria)
            throws Exception {

        // given
        // TODO should not be necessary, mapping in controller should handle null
        when(tagSetApplicationService.searchTagSets(any(SearchTagSetsQuery.class)))
                .thenReturn(new SearchResult<>(null, List.of()));

        // when
        var requestBuilder = get(TAGSET_URL + "/matching");
        searchCriteria.forEach(requestBuilder::queryParam);
        mockMvc.perform(requestBuilder)
                .andExpect(status().isOk());

        // then
        verify(tagSetApplicationService).searchTagSets(expectedQuery);
    }

    private static Stream<Arguments> searchTagSetsCriteria() {
        return Stream.of(
                Arguments.of("empty criteria",
                        new SearchTagSetsQuery(null, null, null, null, null),
                        Map.of()),
                Arguments.of("pageSize filter",
                        new SearchTagSetsQuery(25, null, null, null, null),
                        Map.of("pageSize", "25")),
                Arguments.of("pageId filter",
                        new SearchTagSetsQuery(null, "page-id", null, null, null),
                        Map.of("pageId", "page-id")),
                Arguments.of("term filter",
                        new SearchTagSetsQuery(null, null, "gym", null, null),
                        Map.of("term", "gym")),
                Arguments.of("excludedTagSets filter",
                        new SearchTagSetsQuery(null, null, null, Set.of(new UUID(1L, 1L)), null),
                        Map.of("excludedTagSets", new UUID(1L, 1L).toString())),
                Arguments.of("orderBy filter",
                        new SearchTagSetsQuery(null, null, null, null,
                                new SortCriteria().orderBy("name", "desc")),
                        Map.of("orderBy", "name.desc")),
                Arguments.of("all filters combined",
                        new SearchTagSetsQuery(10, "p1", "run", Set.of(new UUID(2L, 2L)),
                                new SortCriteria().orderBy("name", "desc")),
                        Map.of("pageSize", "10",
                                "pageId", "p1",
                                "term", "run",
                                "excludedTagSets", new UUID(2L, 2L).toString(),
                                "orderBy", "name.desc"))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("matchingTagSets")
    void shouldFindMatchingTagSets(String testName, SearchRestTestData<TagSetResult> expectedSearchResult)
            throws Exception {

        // given
        when(tagSetApplicationService.searchTagSets(any(SearchTagSetsQuery.class)))
                .thenReturn(expectedSearchResult.asEntitySearchResult());

        // when / then
        mockMvc.perform(get(TAGSET_URL + "/matching"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        expectedSearchResult.asHttpResponse(),
                        NO_EXTRA_FIELDS__IGNORE_COLLECTION_ORDER)
                );
    }

    private static Stream<Arguments> matchingTagSets() {
        return Stream.of(
                Arguments.of("no matching tag sets and no next page",
                        new SearchRestTestData<TagSetResult>(null, List.of())),
                Arguments.of("single minimal tag set and no next page",
                        new SearchRestTestData<>(null, List.of(aRestfulTagSet(minimalTagSet())))),
                Arguments.of("three complex tag sets and next page",
                        new SearchRestTestData<>("next-page-id", List.of(
                                aRestfulTagSet(complexTagSet()),
                                aRestfulTagSet(complexTagSet()),
                                aRestfulTagSet(complexTagSet()))))
        );
    }
}
