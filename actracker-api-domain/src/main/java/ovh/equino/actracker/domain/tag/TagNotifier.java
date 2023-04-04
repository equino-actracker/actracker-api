package ovh.equino.actracker.domain.tag;

public interface TagNotifier {

    void notifyChanged(TagChangedNotification tagChangedNotification);
}
