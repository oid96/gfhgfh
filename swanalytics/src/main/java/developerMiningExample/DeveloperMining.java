package developerMiningExample;

import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

public class DeveloperMining implements Study {

    public static void main(String args[]) {
        new RepoDriller().start(new DeveloperMining());
    }

    public void execute() {
        new RepositoryMining()
                .in(GitRepository.singleProject("/Users/fabiopalomba/Desktop/repodriller"))
                .through(Commits.all())
                .process(new DeveloperVisitor(), new CSVFile("/Users/fabiopalomba/Desktop/devs.csv"))
                .mine();
    }
}