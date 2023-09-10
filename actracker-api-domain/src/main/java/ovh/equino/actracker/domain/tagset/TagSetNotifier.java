package ovh.equino.actracker.domain.tagset;

public interface TagSetNotifier {

    void notifyChanged(TagSetChangedNotification tagSetChangedNotification);
}
