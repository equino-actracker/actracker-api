package ovh.equino.actracker.rest.spring.activity;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.domain.EntitySearchCriteria;
import ovh.equino.actracker.domain.EntitySearchResult;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityService;
import ovh.equino.actracker.domain.activity.ActivitySortField;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.actracker.rest.spring.EntitySearchCriteriaBuilder;
import ovh.equino.actracker.rest.spring.SearchResponse;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/activity")
class ActivityController {

    private final ActivityService activityService;
    private final IdentityProvider identityProvider;
    private final ActivityMapper mapper = new ActivityMapper();

    ActivityController(ActivityService activityService, IdentityProvider identityProvider) {
        this.activityService = activityService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Activity createActivity(@RequestBody Activity activity) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        ActivityDto activityDto = mapper.fromRequest(activity);
        ActivityDto createdActivity = activityService.createActivity(activityDto, requester);

        return mapper.toResponse(createdActivity);
    }

    @RequestMapping(method = PUT, path = "/{id}")
    @ResponseStatus(OK)
    Activity updateActivity(@PathVariable("id") String id, @RequestBody Activity activity) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        ActivityDto activityDto = mapper.fromRequest(activity);
        ActivityDto updatedActivity = activityService.updateActivity(UUID.fromString(id), activityDto, requester);

        return mapper.toResponse(updatedActivity);
    }

    @RequestMapping(method = GET, path = "/matching")
    @ResponseStatus(OK)
    SearchResponse<Activity> searchActivities(
            @RequestParam(name = "pageId", required = false) String pageId,
            @RequestParam(name = "pageSize", required = false) Integer pageSize,
            @RequestParam(name = "term", required = false) String term,
            @RequestParam(name = "excludedActivities", required = false) String excludedActivities,
            @RequestParam(name = "orderBy", required = false) String orderBy) {

        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        EntitySearchCriteria searchCriteria = new EntitySearchCriteriaBuilder()
                .withSearcher(requester)
                .withPageId(pageId)
                .withPageSize(pageSize)
                .withTerm(term)
                .withExcludedIdsJointWithComma(excludedActivities)
                .withPossibleSortFields(ActivitySortField.START_TIME)
                .withSortLevelsJointWithComma(orderBy)
                .build();

        EntitySearchResult<ActivityDto> searchResult = activityService.searchActivities(searchCriteria);
        return mapper.toResponse(searchResult);
    }

    @RequestMapping(method = DELETE, path = "/{id}")
    @ResponseStatus(OK)
    void deleteActivity(@PathVariable("id") String id) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User requester = new User(requestIdentity.getId());

        activityService.deleteActivity(UUID.fromString(id), requester);
    }

    @RequestMapping(method = POST, path = "/switched")
    @ResponseStatus(OK)
    Activity switchToActivity(@RequestBody Activity activity) {
        Identity requesterIdentity = identityProvider.provideIdentity();
        User requester = new User(requesterIdentity.getId());

        ActivityDto activityDto = mapper.fromRequest(activity);
        ActivityDto switchedActivity = activityService.switchToNewActivity(activityDto, requester);
        return mapper.toResponse(switchedActivity);
    }

}
