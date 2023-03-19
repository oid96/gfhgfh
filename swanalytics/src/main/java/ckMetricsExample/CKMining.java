package ckMetricsExample;

import com.opencsv.CSVReader;
import org.eclipse.jgit.api.Git;
import org.repodriller.RepoDriller;
import org.repodriller.RepositoryMining;
import org.repodriller.Study;
import org.repodriller.filter.commit.OnlyInMainBranch;
import org.repodriller.filter.commit.OnlyNoMerge;
import org.repodriller.filter.range.Commits;
import org.repodriller.persistence.csv.CSVFile;
import org.repodriller.scm.GitRepository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CKMining implements Study {

    public static void main(String args[]) {

        new RepoDriller().start(new CKMining());
    }

    public void execute() {
        new RepositoryMining()
                .in(GitRepository.singleProject("/home/gerardo/jsouptest"))
                .through(Commits.all())
                .filters(
                        new OnlyNoMerge(),
                        new OnlyInMainBranch()
                )
                .process(new MetricVisitor(), new CSVFile("/home/gerardo/Scrivania/metricheSwAnalytics/NEWJsoupMetricsMAIN.csv"))
                .mine();
    }

}