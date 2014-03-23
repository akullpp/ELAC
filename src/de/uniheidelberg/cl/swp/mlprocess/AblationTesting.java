/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.mlprocess
 * class: AblationTesting
 * 
 * Authors: E-Mail
 * Thomas B�gel: boegel@cl.uni-heidelberg.de
 * Lukas Funk: funk@cl.uni-heidelberg.de
 * Andreas Kull: kull@cl.uni-heidelberg.de
 * 
 * Please find a detailed explanation of this particular class/package and its role and usage at
 * the first JavaDoc following this comment.
 * 
 * Copyright 2010 Thomas B�gel & Lukas Funk & Andreas Kull
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;
import de.uniheidelberg.cl.swp.featureExtraction.FeatureExtractionProcess;
import de.uniheidelberg.cl.swp.featureExtraction.FeatureExtractors;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.io.Logging;
import de.uniheidelberg.cl.swp.testacr.PerformanceMeasurementMain;
import de.uniheidelberg.cl.swp.testacr.Runner;
import de.uniheidelberg.cl.swp.util.CombinationGenerator;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * This standalone class tests all the possible feature combinations to determine the best feature
 * combination for machine learning.
 */
public class AblationTesting {
	private Map<MMAXParser, Map<String,List<CoreferencePair>>> allResults;
	private BufferedWriter bw;
	private List<AbstractFeatureExtractor> bestFeatureList;
	private double bestFScore;
	
	/**
	 * Constructor to initialize ablation testing.
	 */
	public AblationTesting() {}
	
	/**
	 * Runs the whole ablation testing process and collects the results for all possible
	 * combinations.
	 * 	
	 * @param fileName Path to the file where the results should be stored.
	 * @throws If the file couldn't be read.
	 */
	public void run(String fileName) throws Exception {
		processAllFiles();
		String output =
			Configuration.getInstance().getProperties().getProperty("ResultOutputDir") +
			fileName;
		
		File outputFile = new File(output);
		Writer w = new FileWriter(outputFile);
		bw = new BufferedWriter(w);
		
		/* go through all the possible combinations */
		for(int i = 1; i <= FeatureExtractors.values().length; i++) {
			System.out.println("\n** combining " + i + " features **\n");
			combineMultipleFeatures(i);
		}
    }
	
	/**
	 * Lets each system run on all data and collect their results to save runtime.
	 * 
	 * @throws Exception If the system couldn't be executed.
	 */
	private void processAllFiles() throws Exception {
		allResults = new HashMap<MMAXParser, Map<String,List<CoreferencePair>>>();
		List<String> mmaxTestFiles = PerformanceMeasurementMain.getMMAxFiles("Test");

		for (String mmaxTestFile : mmaxTestFiles) {
			MMAXParser mmaxParser = MMAXParser.processMMAXFile(mmaxTestFile);

			/* ACR-predictions */
			Map<String,List<CoreferencePair>> testCorefs =
				new HashMap<String,List<CoreferencePair>>();
			
			for (Runner acrsys : Configuration.getInstance().getAcRRunner()) {
				acrsys.init(mmaxParser);
				acrsys.run();
				List<CoreferencePair> predictionList =
					MMAXParser.splitCorefChainsToPairs(acrsys.getCoreferents());

				if (testCorefs.containsKey(acrsys.getClass().getCanonicalName()))
					testCorefs.get(acrsys.getClass().getCanonicalName()).addAll(predictionList);
				else 
					testCorefs.put(acrsys.getClass().getCanonicalName(), predictionList);
			}
			allResults.put(mmaxParser, testCorefs);
		}
	}
	
	/**
	 * Determines all possible combinations to choose <code>r</code> features out of
	 * <code>n</code>.
	 * <br>
	 * This is done by {@link CombinationGenerator}.
	 * 
	 * @param n Number of feature extractors.
	 * @param r Number of features to be combined.
	 * @return List of possible combinations.
	 */
	private List<List<Integer>> getAllPossibleCombinations(int n, int r) {
		int[] indices;
		CombinationGenerator gen = new CombinationGenerator (n, r);
		List<List<Integer>> combinations = new ArrayList<List<Integer>>();
		List<Integer> combination;
    
		while (gen.hasMore ()) {
			combination = new ArrayList<Integer>();
			indices = gen.getNext ();
			
			for (int i = 0; i < indices.length; i++) {combination.add(indices[i]);}
			
			combinations.add(combination);
        }
		return combinations;
	}

	/**
	 * Tests all possible combinations of <code>num</code> features.
	 * 
	 * @param num Number of features to be tested.
	 * @throws Exception Error occurred during the process.
	 */
	private void combineMultipleFeatures(int num) throws Exception {
		FeatureExtractors[] f = FeatureExtractors.values();
		int counter = 1;
		
		/* iterate over all possible combinations of num features */ 
		List<List<Integer>> featureCombinations = getAllPossibleCombinations(f.length, num);
		for (List<Integer> featureSet : featureCombinations) {
			System.out.println("## using combination " + counter + " of " + 
									featureCombinations.size() + " ##");
			
			List<AbstractFeatureExtractor> featureList = new ArrayList<AbstractFeatureExtractor>();
			
			for (Integer featureNum : featureSet) { featureList.add(f[featureNum].getFe()); }
			
			runMLProcess(featureList);
			counter += 1;
		}
	}
		
	/**
	 * Starts the whole mlprocess and writes the results for the current feature list.
	 *
	 * @param featureList The feature List which should be used for the classifier.
	 * @throws Exception If the Machine Learning process couldn't be executed.
	 */
	private void runMLProcess(List<AbstractFeatureExtractor> featureList) throws Exception {
		MLProcess mlp = new MLProcess(createInstances(
				Configuration.getInstance().getProperties().getProperty("ResultOutputDir") +
				 "results.arff", featureList));

		StringBuffer sb = new StringBuffer();
		for (AbstractFeatureExtractor afe : featureList) {
			sb.append(afe.getName() + " ");
		}
		
		/* Run MLProcess for each MMAX file with the current featureList */
		for (MMAXParser goldStandard : this.allResults.keySet()) {
			Map<String,List<CoreferencePair>> acrPredictions = allResults.get(goldStandard);
			FeatureExtractionProcess fep = new FeatureExtractionProcess(featureList);
			mlp.setFep(fep);
			
			try{
				mlp.run(fep, goldStandard, acrPredictions);
			} catch (Exception e) {
				System.err.println("Couldn't run ML Process with feature vector " + sb + 
						"- See the log file for error information.");
				Logging.getInstance().getLogger().logrb(Level.SEVERE, "AblationTesting", 
														"runMLProcess", null, sb.toString(), e);
				return;
			}
			
		}
		
		
		
		
		
		double precision =  mlp.getEval().getTruePositives() / (mlp.getEval().getTruePositives() + 
				mlp.getEval().getFalseNegatives());
		
		double recall = (mlp.getEval().getTruePositives() / (mlp.getEval().getTruePositives() + 
				mlp.getEval().getFalsePositives())); 
		
		double fscore = 2 * ((precision * recall) / (precision + recall));
		
		if (2 * ((precision * recall) / (precision + recall)) > bestFScore) {
			bestFeatureList = featureList;
			bestFScore = 2 * ((precision * recall) / (precision + recall)); 
		}
		
		bw.write("\n\nFeature combination: " + sb.toString() + "\nRecall: " + recall +
				"\nPrecision: " + precision + "\nF-Score: " + fscore);
		
		bw.flush();
	}
	
	
	/**
	 * Writes the feature combination leading to the best results (using f-score). 
	 * 
	 * @throws If the file couldn't be written.
	 */
	public void writeFinalResults() throws IOException {
		StringBuffer sb = new StringBuffer();
		
		for (AbstractFeatureExtractor afe : bestFeatureList) {
			sb.append(afe.getName() + " ");
		}
		
		System.out.println("\n\n######\nBest feature combination: " + sb.toString() +
				"\nF-Score: " + bestFScore + "\n######\n");
		
		bw.write("\n\n######\nBest feature combination: " + sb.toString() +
				"\nF-Score: " + bestFScore + "\n######\n");
		
		bw.close();
	}
	
	/**
	 * Creates an Instance object for the specified List of Features.
	 * <br>
	 * Extracts the Instance objects from a source file and suppresses all features but the ones 
	 * specified.
	 * 
	 * @param fileName File to the training results in ARFF format.
	 * @param features List of {@link AbstractFeatureExtractor}s which are currently being tested.
	 * @return Instances object consisting of the desired attribute structure.
	 * @throws Exception If the ARFF file couldn't be read, an exception is thrown.
	 */
	public Instances createInstances(String fileName,
			List<AbstractFeatureExtractor> features) throws Exception {
		final Instances train = new Instances(new BufferedReader(new FileReader(fileName)));
		ArrayList<Attribute> newAttributes = new ArrayList<Attribute>();
		
		for (int i = 0; i < train.numAttributes(); i++) {
			for (AbstractFeatureExtractor feature : features) {
				if (train.attribute(i).name().equals(feature.getName())) {
					newAttributes.add(train.attribute(i));
					
					continue;
				}
			}
		}
		
		/* 
		 * add the last two features (ACR-System + correct/false predictions) as those 
		 * are no features gathered by a FeatureExtractor.
		 */
		newAttributes.add(train.attribute(train.numAttributes()-2));
		newAttributes.add(train.attribute(train.numAttributes()-1));
		Instances trainCopy = copyInstances(train, newAttributes);
		trainCopy.setClassIndex(trainCopy.numAttributes()-1);
		
		return trainCopy;
	}
	
	/**
	 * Copies the Instances from the source Instances object to a new one, which only contains the 
	 * currently tested features.
	 * 
	 * @param source The Instances object containing all the Instance objects from the source file. 
	 * @param targetStructure The list of {@link AbstractFeatureExtractor}s which is currently 
	 * 			being tested.
	 * @return An instances object consisting of all Instance objects from the source file.  
	 */
	private Instances copyInstances(Instances source, ArrayList<Attribute> targetStructure) {
		Instances target = new Instances("ACResolution", targetStructure, 0);
		
		for (int i = 0; i < source.numInstances(); i++) {
			double[] vals = new double[targetStructure.size()];
			
			for (int z = 0; z < targetStructure.size(); z++) {
				vals[z] = getAttributeValue(source.instance(i), targetStructure.get(z).name());
			}
			Instance in = new DenseInstance(1.0, vals);
			target.add(in);
		}
		return target;
	}
	
	/**
	 * Determines the attribute value for a Instance object and the specified attribute name.
	 * 
	 * @param inst The instance object from which the value is extracted.
	 * @param featureName The name of the attribute.
	 * @return A double representation of the value used by WEKA.
	 */
	private double getAttributeValue(Instance inst, String featureName) {
		for (int i = 0; i < inst.numAttributes(); i++) {
			if (inst.attribute(i).name().equals(featureName)) return inst.value(i); 
		}
		return 0;
	}

	/**
	 * The main method to run ablation testing. 
	 * <br>
	 * A path for the output file has to be specified. 
	 * 
	 * @param args Path to the output file.
	 */
	public static void main(String[] args) {
		if (args.length<1) {
			System.out.println("\nSyntax: AblationTesting <outputFile>\n");
			System.exit(0);
		}
		try {
			AblationTesting a = new AblationTesting();
			a.run(args[0]);
			a.writeFinalResults();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
