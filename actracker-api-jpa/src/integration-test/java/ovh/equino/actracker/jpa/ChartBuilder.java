package ovh.equino.actracker.jpa;

import ovh.equino.actracker.domain.dashboard.AnalysisMetric;
import ovh.equino.actracker.domain.dashboard.Chart;
import ovh.equino.actracker.domain.dashboard.ChartId;
import ovh.equino.actracker.domain.dashboard.GroupBy;
import ovh.equino.actracker.domain.tag.TagDto;
import ovh.equino.actracker.domain.tenant.TenantDto;

import java.util.Arrays;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static ovh.equino.actracker.jpa.TestUtil.nextUUID;
import static ovh.equino.actracker.jpa.TestUtil.randomString;

public final class ChartBuilder {

    private Chart newChart;

    ChartBuilder(TenantDto creator) {
        this.newChart = new Chart(
                new ChartId(),
                randomString(),
                GroupBy.SELF,
                AnalysisMetric.TAG_PERCENTAGE,
                Set.of(nextUUID(), nextUUID(), nextUUID()),
                false
        );
    }

    public ChartBuilder withTags(TagDto... tags) {
        newChart = new Chart(
                newChart.id(),
                newChart.name(),
                newChart.groupBy(),
                newChart.analysisMetric(),
                Arrays.stream(tags).map(TagDto::id).collect(toUnmodifiableSet()),
                newChart.isDeleted()
        );
        return this;
    }

    public ChartBuilder deleted() {
        newChart = new Chart(
                newChart.id(),
                newChart.name(),
                newChart.groupBy(),
                newChart.analysisMetric(),
                newChart.includedTags(),
                true
        );
        return this;

    }

    public Chart build() {
        return newChart;
    }
}
