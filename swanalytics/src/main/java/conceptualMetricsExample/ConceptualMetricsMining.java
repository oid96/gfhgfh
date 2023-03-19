package conceptualMetricsExample;

import ckMetricsExample.CKMining;
import ckMetricsExample.MetricVisitor;
import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

public class ConceptualMetricsMining implements Study {

    public static void main(String args[]) {
        new RepoDriller().start(new ConceptualMetricsMining());
    }

    public void execute() {
        new RepositoryMining()
                .in(GitRepository.singleProject("/Users/fabiopalomba/Desktop/repodriller"))
                .through(Commits.all())
                .process(new ConceptualMetricsVisitor(), new CSVFile("/Users/fabiopalomba/Desktop/conceptual_metrics.csv"))
                .mine();

    }
}