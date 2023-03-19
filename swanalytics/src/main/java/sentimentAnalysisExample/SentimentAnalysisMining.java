package sentimentAnalysisExample;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

public class SentimentAnalysisMining implements Study {

    public static void main(String args[]) {
        new RepoDriller().start(new SentimentAnalysisMining());
    }

    public void execute() {
        new RepositoryMining()
                .in(GitRepository.singleProject("/Users/fabiopalomba/Desktop/repodriller"))
                .through(Commits.all())
                .process(new SentimentAnalysisVisitor(), new CSVFile("/Users/fabiopalomba/Desktop/sentiments.csv"))
                .mine();
    }
}
