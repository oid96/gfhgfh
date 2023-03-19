package machineLearningExample;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.core.Instances;
import weka.filters.Filter;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.supervised.instance.ClassBalancer;
import weka.filters.unsupervised.attribute.Remove;

public class Pipeline {

    public Pipeline(String dataset, String outputFilePath, Classifier classifier) {

        try {
            // Getting the data source, namely the input data to manipulate when creating the machine learner.
            String filePath = dataset;
            DataSource source = new DataSource(filePath);

            // Getting the instances contained in the dataset, we will use them during the various steps.
            Instances instances = source.getDataSet();
            // Setting the index of the dependent variable. NB: In this example, the dependent variable
            // is the last attribute of the file.
            instances.setClassIndex(instances.numAttributes()-1);

            // Let's investigate the dataset. We will print number of attributes and instances.
            // Note that, in this case, we know already that the dataset has a good quality and does not require the
            // application of data imputation techniques.
            System.out.println("Number of attributes in the dataset (# columns): " + instances.numAttributes());
            System.out.println("Number of instances in the dataset: (# rows):    " + instances.size());

            // As a first step, let's investigate the dataset for possible multi-collinearity. We will rely on InfoGain.
            Pipeline.infoGain(instances, "/Users/fabiopalomba/Desktop/infoGain.txt");

            // Suppose that, after the analysis of the attributes, we would like to remove the attributes
            // 'metric1' and 'metric5'. We create the list of attributes to remove.
            ArrayList<String> attToRemove = new ArrayList<>();
            attToRemove.add("metric1");
            attToRemove.add("metric5");

            // Then, we create a new filter that specifies the attributes to remove.
            Filter removeFilter = Pipeline.removeFeaturesAfterInfoGain(instances, attToRemove);
            // Finally, we apply the filter to the dataset.
            instances = Filter.useFilter(instances, removeFilter);

            // After pre-processing the data, we can start its training/test with the subsequent evaluation.
            Pipeline.applyTenFoldCrossValidation(instances, classifier, 10, outputFilePath);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * The infoGain method implements a GainRatio algorithm to rank attributes of the dataset based on the potential
     * gain provided to the machine learning model.
     *
     * @param data: the dataset to assess for multi-collinearity.
     * @param outputFilePath: the file where the results of InfoGain will be printed.
     *
     */
    private static void infoGain(Instances data, String outputFilePath) {
        // Weka makes the class InfoGainAttributeEval available.
        InfoGainAttributeEval eval = new InfoGainAttributeEval();
        // We will need to specify that the evaluation has to rank attributes based on the gain provided.
        Ranker search = new Ranker();

        // How to use those classes? Let's start by selecting attributes,
        AttributeSelection attSelect = new AttributeSelection();
        attSelect.setEvaluator(eval); // setting the evaluation as an InfoGain one
        attSelect.setSearch(search);  // and ranking attributes based on that.

        try {
            // Now, we just run the attribute selection method.
            attSelect.SelectAttributes(data);
            //int[] indices = attSelect.selectedAttributes();

            // Let's pretty print the results of the InfoGain algorithm.
            File igOutput = new File(outputFilePath);
            PrintWriter pw1 = new PrintWriter(igOutput);

            // In particular, we will convert the raw output of the algorithm in a csv file.
            System.out.println(attSelect.toResultsString()); // This is the raw outcome.

            // This is the pretty print.
            String outString = attSelect.toResultsString().replace("Ranked attributes:", "ranked;index;attribute");
            outString = outString.substring(outString.indexOf("ranked;index;attribute"));
            outString = outString.substring(0, outString.indexOf("\nSelected"));
            outString = outString.replaceAll("\n[ ]+", "\n");
            outString = outString.replaceAll("[ ]+", ";");

            pw1.write(outString);
            pw1.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The removeFeaturesAfterInfoGain method implements a Weka filter that removes the attributes considered
     * redundant after the assessment done with the infoGain method.
     *
     * @param instances: the dataset on which to remove attributes.
     * @param attToRemove: an ArrayList reporting the set of attributes that we want to remove.
     *
     * @return a filter to remove attributes.
     *
     * @throws Exception: the method throws a generic exception in cases where an input format error is identified.
     *
     */
    private static Filter removeFeaturesAfterInfoGain(Instances instances, ArrayList<String> attToRemove) throws Exception {
        ArrayList<Integer> indicesList = new ArrayList<>();
        int i = 0;
        for (String s : attToRemove) {
            s = s.replaceAll("\"", "");
            indicesList.add(instances.attribute(s).index());
        }

        int[] indices = Arrays.stream(indicesList.toArray()).mapToInt(o -> (int) o).toArray();

        Remove removeFilter = new Remove();
        removeFilter.setAttributeIndicesArray(indices);
        removeFilter.setInvertSelection(false);
        removeFilter.setInputFormat(instances);

        return removeFilter;
    }

    /**
     * The createBalancingFilter method implements a Weka filter that balances the dataset.
     *
     * @param instances: the instances that need to be balanced.
     *
     * @return a filter to balance attributes.
     *
     * @throws Exception: the method throws a generic exception in cases where an input format error is identified.
     */
    private static Filter createBalancingFilter(Instances instances) throws Exception {
        ClassBalancer filter = new ClassBalancer();
        filter.setInputFormat(instances);
        return filter;
    }

    /**
     * The applyTenFoldCrossValidation method applies a ten-fold cross validation against the dataset taken as input.
     *
     * @param pInstances:     the dataset to validate.
     * @param pClassifier:    the classifier to use when building the model.
     * @param repetitions:    the number of times the ten-fold cross validation must be repeated.
     * @param outputFilePath: a path to a file where the results of the model will be printed.
     *
     */
    private static void applyTenFoldCrossValidation(Instances pInstances, Classifier pClassifier,
                                                    int repetitions, String outputFilePath) {
        // When validating the model, we will need to specify a number of attributes,
        // starting from the number of random folds, that in our case if 10 since we use a 10-fold cross-validation.
        int folds = 10;

        // Declare the validation metrics that we want to use.
        double accuracy = 0.0, precision = 0.0, recall = 0.0, MCC = 0.0, fmeasure = 0.0, auc = 0.0;

        // The index of the dependent variable class (i.e., true or false in our example) that Weka should consider as
        // the positive response: this will be later used to compute the evaluation metrics.
        int positiveValueIndexOfClassFeature = 0;

        // The validation requires data to be randomized.
        for (int repetition = 0; repetition < repetitions; repetition ++) { // For all repetitions of the 10-fold cross validation

            // Let's randomize data. NB: we apply a STRATIFIED random strategy, i.e., all folds have a similar
            // distribution in terms of dependent variable values.
            Random rand = new Random();
            Instances randData = new Instances(pInstances);
            randData.randomize(rand);
            randData.stratify(folds);

            try {
                // Create an Weka Evaluation object that will act on the randomized data.
                Evaluation eval = new Evaluation(randData);

                for (int n = 0; n < folds; n++) { // for all folds

                    // Let's separate training and test data.
                    Instances trainingSet = randData.trainCV(folds, n, rand);
                    Instances testSet = randData.testCV(folds, n);

                    // Here we verify whether we need to balance data. NB: we do it at every run and NOT before,
                    // otherwise we would have modified the dataset permanently, biasing the results.
                    Filter dataBalancingFilter = createBalancingFilter(trainingSet);
                    // NB: We balance the training set only!
                    trainingSet = Filter.useFilter(trainingSet, dataBalancingFilter);

                    trainingSet.setClassIndex(trainingSet.numAttributes()-1);
                    testSet.setClassIndex(trainingSet.numAttributes()-1);

                    pClassifier.buildClassifier(trainingSet);
                    eval.evaluateModel(pClassifier, testSet);

                }

                // Let's get the number of true/false positives/negatives.
                double tp = eval.numTruePositives(positiveValueIndexOfClassFeature);
                double tn = eval.numTrueNegatives(positiveValueIndexOfClassFeature);
                double fp = eval.numFalsePositives(positiveValueIndexOfClassFeature);
                double fn = eval.numFalseNegatives(positiveValueIndexOfClassFeature);

                // Now, let' compute the metrics we are interested in.
                accuracy = (tp + tn) / (tp + fp + fn + tn);
                precision = eval.precision(positiveValueIndexOfClassFeature);
                recall = eval.recall(positiveValueIndexOfClassFeature);
                fmeasure = 2 * ((precision * recall) / (precision + recall));
                auc = eval.areaUnderROC(positiveValueIndexOfClassFeature);
                MCC = ((tp * tn) - (fp * fn)) / (Math.sqrt(((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn))));

                // Pretty printing the evaluation results.
                File wekaOutput = new File(outputFilePath);

                PrintWriter pw1 = new PrintWriter(wekaOutput);
                pw1.write("TP;TN;FP;FN;Accuracy; Precision; Recall; fmeasure; areaUnderROC; MCC\n");
                pw1.write(tp + ";" + tn + ";" + fp + ";" + fn + ";" + accuracy + ";" + precision + ";" + recall + ";" + fmeasure + ";" + auc + ";" + MCC + "\n");
                pw1.flush();
                pw1.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}