package machineLearningExample;

import weka.classifiers.Classifier;
import weka.classifiers.trees.RandomForest;

public class PipelineRunner {

    public static void main(String args[]) {

        Pipeline pipeline = new Pipeline("/Users/fabiopalomba/Desktop/dataset.csv",
                "/Users/fabiopalomba/Desktop/output-results.csv",
                new RandomForest());
    }
}
