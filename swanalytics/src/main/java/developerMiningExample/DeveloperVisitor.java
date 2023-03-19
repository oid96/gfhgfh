package developerMiningExample;

import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

public class DeveloperVisitor implements CommitVisitor {

    @Override
    public void process(SCMRepository repo, Commit commit, PersistenceMechanism writer) {

        writer.write(
                commit.getHash(),
                commit.getCommitter().getName(),
                commit.getAuthor(),
                commit.getMsg()
        );

    }
}