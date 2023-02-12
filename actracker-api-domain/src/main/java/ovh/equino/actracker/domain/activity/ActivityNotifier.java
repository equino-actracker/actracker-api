package ovh.equino.actracker.domain.activity;

public interface ActivityNotifier {

    void notifyChanged(ActivityDto activity);
}
