/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.mlprocess
 * class: WEKARunner
 * 
 * Authors: E-Mail
 * Thomas Boegel: boegel@cl.uni-heidelberg.de
 * Lukas Funk: funk@cl.uni-heidelberg.de
 * Andreas Kull: kull@cl.uni-heidelberg.de
 * 
 * Please find a detailed explanation of this particular class/package and its role and usage at
 * the first JavaDoc following this comment.
 * 
 * Copyright 2010 Thomas Boegel & Lukas Funk & Andreas Kull
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.uniheidelberg.cl.swp.mlprocess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import de.uniheidelberg.cl.swp.io.Logging;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.Stacking;
import weka.core.converters.ArffSaver;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;


/**
 * An interface class to access the <a href="http://weka.sourceforge.net/doc.dev/">WEKA API</a>.
 * <br>
 * Several parts of the code are taken from the <a href="http://weka.wikispaces.com/">WEKA Wiki</a>
 * and adapted to our needs in order to classify a single instance.
 * <br>
 * To use this class, <a href="http://www.cs.waikato.ac.nz/~ml/weka/">WEKA 3.7.1</a> is needed and
 * to be placed in the resource folder.
 */
public class WEKARunner {
	private Instances train;
	private Classifier classifier;
	
	/* internal WEKA evaluation not used yet */
	private Evaluation evaluation;

	/**
	 * Holds the classifiers we currently support and use.
	 */
	private enum Type {
		J48,
		BAYES,
		BAGGING,
		ADABOOST,
		NEARESTNEIGHBOR,
		ZEROR,
		KSTAR,
		BFTREE
	}

	/**
	 * Creates a WEKA interface with a local ARFF file for training. 
	 * 
	 * @param trainArff Local ARFF file for training.
	 * @throws If WEKA couldn't be initialized.
	 */
	public WEKARunner(String trainArff) throws Exception {
		train = new Instances(new BufferedReader(new FileReader(trainArff)));
		train.setClassIndex(train.numAttributes() - 1);
	}

	/**
	 * Creates a WEKA interface with a provided Instances for training.
	 * 
	 * @param trainInstances Instances used for training.
	 * @throws If WEKA couldn't be initialized.
	 */
	public WEKARunner(Instances trainInstances) throws Exception {
		train = trainInstances;
	}
	
	
	/**
	 * Returns the class-name and path for a given classifier type.
	 * <br>
	 * This is mainly for security so that we can guarantee functionality.
	 * 
	 * @param type Given classifier type.
	 * @return String representation to the class-name and its path.
	 */
	private String getClass(Type type) throws Exception {
		switch (type) {
			case J48:
				return "weka.classifiers.trees.J48";
			case BAYES:
				return "weka.classifiers.bayes.NaiveBayes";
			case BAGGING:
				return "weka.classifiers.meta.Bagging";
			case ADABOOST:
				return "weka.classifiers.meta.AdaBoostM1";
			case NEARESTNEIGHBOR:
				return "weka.classifiers.lazy.IB1";
			case ZEROR:
				return "weka.classifiers.rules.ZeroR";
			case KSTAR:
				return "weka.classifiers.lazy.KStar";
			case BFTREE:
				return "weka.classifiers.trees.BFTree";
			default:
				throw new ClassifierException("Classifier not found.");
		}
	}

	/**
	 * Internal class for useful exception handling.
	 */
	private class ClassifierException extends Exception {
		private static final long serialVersionUID = 1L;

		public ClassifierException(String msg) {
			super(msg);
		}
	}
	
	/**
	 * Entry point for external usage to construct the classifier given a machine learning
	 * algorithm.
	 * 
	 * @param type A base classifier (machine learning algorithm).
	 * @param options Options for the classifier.
	 * @throws If the classifier couldn't be initialized.
	 */
	public void run(String type, String options) throws Exception {
		StringBuffer sb = new StringBuffer();
		
		if (type.equals("J48") || type.equals("BAYES") || type.equals("KSTAR") ||
			type.equals("ZEROR") || type.equals("BFTREE")) {
			buildClassifier(getClass(Type.valueOf(type.toUpperCase())),
					Utils.splitOptions(sb.toString()));
		}
		else { throw new ClassifierException("Please select a subclassifier"); }
	}

	/**
	 * Entry point for external usage to construct the classifier given a meta machine learning
	 * algorithm and a machine learning algorithm for a base-learner.
	 * 
	 * @param type A meta machine learning algorithm.
	 * @param subtype A machine learning algorithm.
	 * @param options Options for the classifiers.
	 * @throws If the classifier couldn't be initialized.
	 */
	public void run(String type, String subtype, String options) throws Exception {
		if (subtype.equals("J48") || subtype.equals("BAYES")) {
			StringBuffer sb = new StringBuffer();
			
			sb.append("-W " + getClass(Type.valueOf(subtype.toUpperCase())) + " " + options);
			buildClassifier(getClass(Type.valueOf(type.toUpperCase())),
					Utils.splitOptions(sb.toString()));
		}
		else {throw new ClassifierException("Please specify a proper subclassifier for " +
				"base-learning"); }
	}
		
	/**
	 * Entry point for external usage to construct a stacking classifier with a meta machine
	 * learning algorithm for level 1 and multiple machine learning algorithms for level 0.
	 * 
	 * @param type A meta machine learning algorithm for level 1.
	 * @param subtypes Multiple machine learning algorithms for level 0.
	 * @param options Options for the classifiers.
	 * @throws If the classifier couldn't be initialized.
	 */
	public void run(String type, String[] subtypes, String options) throws Exception {
		this.classifier = createStack(type, subtypes, options);
	}
	
	/**
	 * Internal construction of the stacking classifier and its level 1 and level 0 algorithms.
	 * 
	 * @param type A meta machine learning algorithm for level 1.
	 * @param subtypes Multiple machine learning algorithms for level 0.
	 * @param options Options for the classifiers.
	 * @return The stacking classifier.
	 */
	private Stacking createStack(String type, String[] subtypes, String options) throws Exception {
		StringBuffer sb = new StringBuffer();
		Stacking stack = new Stacking();
		
		sb.append("-M " + getClass(Type.valueOf(type.toUpperCase())));
		
		for (String s : subtypes) {
			sb.append(" -B " + getClass(Type.valueOf(s.toUpperCase())));
		}
		sb.append(" " + options);
		
		Logging.getInstance().getLogger().info("Building " + subtypes.length +
				" subclassifiers... this might take some time");
		stack.setOptions(Utils.splitOptions(sb.toString()));
		stack.buildClassifier(train);
		
		return stack;
	}
	
	/**
	 * Builds a classifier.
	 * 
	 * @param name Classname of the specific classifier.
	 * @param options Options for the specific classifier.
	 */
	private void buildClassifier(String name, String[] options) throws Exception {
		classifier = (Classifier)Utils.forName(Classifier.class, name, options);
		classifier.buildClassifier(train);
	}

	/**
	 * Evaluates our classifier with a test set.
	 * <br>
	 * Not used yet.
	 *
	 * @param testArff ARFF file to evaluate against.
	 * @throws If the evaluation couldn't be initialized.
	 */
	public void buildEvaluation(String testArff) throws Exception {
		Instances evalIns = new Instances(new BufferedReader(new FileReader(testArff)));
		evalIns.setClassIndex(evalIns.numAttributes() - 1);
		evaluation = new Evaluation(train);
	}

	/**
	 * Cross-evaluates our classifier.
	 * <br>
	 * Not used yet.
	 * 
	 * @throws If the evaluation couldn't be initialized.
	 */
	public void buildEvaluation() throws Exception {
		evaluation = new Evaluation(train);
		evaluation.crossValidateModel(classifier, train, 10, new Random(1));
	}

	/**
	 * Getter for returning the evaluation of the classifier.
	 * <br>
	 * Not used yet.
	 *
	 * @return Evaluation of the classifier.
	 */
	public Evaluation getEvaluation() {
		return evaluation;
	}

	/**
	 * Predicts unknown labels of an Instances.
	 * 
	 * @param unkIns Instances with unknown attributes.
	 * @return Instances with the formerly unknown instances, now labeled.
	 * @throws If the Instances couldn't be labeled.
	 */
	public Instances labelUnknownInstances(Instances unkIns) throws Exception {
		Instances testcpy = new Instances(unkIns);

		for (int i = 0; i < unkIns.numInstances(); i++) {
			double clsLabel = classifier.classifyInstance(unkIns.instance(i));
			testcpy.instance(i).setClassValue(clsLabel);
		}
		return testcpy;
	}

	/**
	 * Classifies a single Instance.
	 * <br>
	 * The class attribute, which is a numeral representation (representing the position) of the
	 * label, must be resolved by the caller.
	 * 
	 * @param instance Instance to be classified.
	 * @return A numeral representation of the class attribute.
	 * @throws If the Instance couldn't be labeled.
	 */
	public double labelUnknownInstance(Instance instance) throws Exception {
		return classifier.classifyInstance(instance);
	}

	/**
	 * Returns the output possibility of each possible label of an instance.
	 * <br>
	 * Not used yet.
	 * 
	 * @param instance Instance for the distribution analysis.
	 * @return Array of possibilities with values for each attribute.
	 */
	public double[] getDistribution(Instance instance) throws Exception {
		return classifier.distributionForInstance(instance);
	}

	/**
	 * Writes an Instances to an ARFF file.
	 * 
	 * @param instances Instances to be written to an ARFF file.
	 * @param savedArffPath Filename and -path of the Instances to be saved.
	 * @throws If the file couldn't be written.
	 */
	public static void writeInstancesToArff(Instances instances,
			String savedArffPath) throws IOException {
		ArffSaver saver = new ArffSaver();
		saver.setInstances(instances);
		saver.setFile(new File(savedArffPath));
		saver.writeBatch();
	}
}
