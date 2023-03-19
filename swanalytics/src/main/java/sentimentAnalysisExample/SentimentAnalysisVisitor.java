package sentimentAnalysisExample;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.repodriller.domain.Commit;
import org.repodriller.persistence.PersistenceMechanism;
import org.repodriller.scm.CommitVisitor;
import org.repodriller.scm.SCMRepository;

import java.util.Properties;

public class SentimentAnalysisVisitor implements CommitVisitor {

    @Override
    public void process(SCMRepository scmRepository, Commit commit, PersistenceMechanism writer) {

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        System.out.println(commit.getMsg());
        Annotation annotation = pipeline.process(commit.getMsg());

        double overallSentiment = 0.0;
        double sentences = 0.0;

        for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
            sentences++;

            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            overallSentiment+=RNNCoreAnnotations.getPredictedClass(tree);
        }

        overallSentiment=overallSentiment/sentences;

        // Now, let's pretty-print our results.
        writer.write(
                commit.getHash(),
                overallSentiment
        );

        // That's all folks!
    }
}
