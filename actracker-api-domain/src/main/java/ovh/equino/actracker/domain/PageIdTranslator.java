package ovh.equino.actracker.domain;

public interface PageIdTranslator {

    String toString(EntitySearchPageId pageId);

    EntitySearchPageId fromString(String pageId);
}
