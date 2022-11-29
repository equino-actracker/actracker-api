package ovh.equino.actracker.rest.spring;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/activity")
class ActivityController {


    @RequestMapping(method = POST)
    @ResponseStatus(CREATED)
    Activity createActivity(@RequestBody Activity activity) {
        return new Activity(
                UUID.randomUUID().toString(),
                activity.startTimestamp(),
                activity.endTimestamp()
        );
    }

    @RequestMapping(method = PUT, path = "/{id}")
    @ResponseStatus(NO_CONTENT)
    Activity updateActivity(@PathVariable("id") String id, @RequestBody Activity activity) {
        return activity;
    }

    @RequestMapping(method = GET)
    @ResponseStatus(OK)
    List<Activity> getActivities() {
        return List.of(
                new Activity(UUID.randomUUID().toString(), Instant.now().toEpochMilli(), Instant.now().toEpochMilli()),
                new Activity(UUID.randomUUID().toString(), Instant.now().toEpochMilli(), Instant.now().toEpochMilli()),
                new Activity(UUID.randomUUID().toString(), Instant.now().toEpochMilli(), Instant.now().toEpochMilli())
        );
    }

}
