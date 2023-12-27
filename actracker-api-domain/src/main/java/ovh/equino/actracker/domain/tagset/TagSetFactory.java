package ovh.equino.actracker.domain.tagset;

import ovh.equino.actracker.domain.tag.TagId;
import ovh.equino.actracker.domain.user.User;

import java.util.Collection;

public interface TagSetFactory {

    TagSet create(String name, Collection<TagId> tags);

    TagSet reconstitute(TagSetId id,
                        User creator,
                        String name,
                        Collection<TagId> tags,
                        boolean deleted);
}
