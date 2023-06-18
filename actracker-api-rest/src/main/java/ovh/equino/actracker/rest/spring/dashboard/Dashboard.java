package ovh.equino.actracker.rest.spring.dashboard;

import ovh.equino.actracker.rest.spring.share.Share;

import java.util.List;

record Dashboard(
        String id,
        String name,
        List<Chart> charts,
        List<Share> shares
) {
}
