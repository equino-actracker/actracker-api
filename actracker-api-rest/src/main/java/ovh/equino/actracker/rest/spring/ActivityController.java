package ovh.equino.actracker.rest.spring;

import org.springframework.web.bind.annotation.*;
import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityService;
import ovh.equino.actracker.domain.user.User;
import ovh.equino.security.identity.Identity;
import ovh.equino.security.identity.IdentityProvider;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/activity")
class ActivityController {

    private final ActivityService activityService;
    private final IdentityProvider identityProvider;
    private final ActivityMapper activityMapper = new ActivityMapper();

    ActivityController(ActivityService activityService, IdentityProvider identityProvider) {
        this.activityService = activityService;
        this.identityProvider = identityProvider;
    }

    @RequestMapping(method = POST)
    @ResponseStatus(OK)
    Activity createActivity(@RequestBody Activity activity) {
        Identity requestIdentity = identityProvider.provideIdentity();
        User activityCreator = new User(requestIdentity.getId());

        ActivityDto activityDto = activityMapper.fromRequest(activity);
        ActivityDto createdActivity = activityService.createActivity(activityDto, activityCreator);

        return activityMapper.toResponse(createdActivity);
    }

    @RequestMapping(method = PUT, path = "/{id}")
    @ResponseStatus(OK)
    Activity updateActivity(@PathVariable("id") String id, @RequestBody Activity activity) {
        ActivityDto activityDto = activityMapper.fromRequest(activity);
        ActivityDto updateActivity = activityService.updateActivity(UUID.fromString(id), activityDto);
        return activityMapper.toResponse(updateActivity);
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    List<Activity> getActivities() {
        List<ActivityDto> activities = activityService.getActivities();
        return activityMapper.toResponse(activities);
    }

}
