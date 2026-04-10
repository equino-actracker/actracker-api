package ovh.equino.actracker.application;

import ovh.equino.actracker.domain.EntitySearchPageId;

public interface PageIdTranslator {

    String toString(EntitySearchPageId pageId);

    EntitySearchPageId fromString(String pageId);
}
