package ovh.equino.actracker.application;

import java.util.List;

public record SearchResult<T>(String nextPageId,
                              List<T> results) {
}
