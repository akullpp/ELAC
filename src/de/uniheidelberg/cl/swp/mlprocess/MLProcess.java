/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.mlprocess
 * class: MLProcess
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import weka.core.Instance;
import weka.core.Instances;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.featureExtraction.FeatureExtractionProcess;
import de.uniheidelberg.cl.swp.io.ACRResultWriter;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.testacr.Evaluation;
import de.uniheidelberg.cl.swp.testacr.PerformanceMeasurementMain;
import de.uniheidelberg.cl.swp.testacr.Runner;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * Entry point for the testing process.
 * <br>
 * <p>Extracts the results of all ACR-Systems, builds an classifier based on the training and save
 * the positive classifications of the classifier to a list. Afterwards the classifier will be
 * evaluated against the gold standard.</p>
 */
public class MLProcess {
	private WEKARunner wr;
	private Configuration config;
	private InstanceContainer ic;
	private ACRResultWriter writer;
	private Evaluation eval;
	private FeatureExtractionProcess fep;
	private String options = "";

	/**
	 * The constructor initializes the necessary data structures and reads the config options.
	 * <br>
	 * Uses default ARFF file which is created during the training process.
	 * 
	 * @throws If something did go wrong with the testing process.
	 */
	public MLProcess() throws Exception {
		config = Configuration.getInstance();
		wr = new WEKARunner(this.config.getProperties().getProperty("ResultOutputDir") +
		"results.arff");
		
		initWEKA();
	
		writer = new ACRResultWriter("MLProcess", config);
		eval = new Evaluation();
		fep = new FeatureExtractionProcess();
	}
	
	/**
	 * The constructor initializes the necessary data structures and reads the config options.
	 * <br>
	 * Used for external ARFF files.
	 * 
	 * @param ARFF External ARFF file.
	 * @throws If something did go wrong with the testing process.
	 */
	public MLProcess(String ARFF) throws Exception {
		config = Configuration.getInstance();
		wr = new WEKARunner(ARFF);
		
		initWEKA();

		writer = new ACRResultWriter("MLProcess", config);
		eval = new Evaluation();
		fep = new FeatureExtractionProcess();
	}
	
	/**
	 * The constructor initializes the necessary data structures and reads the config options.
	 * <br>
	 * Used for internal purposes.
	 * 
	 * @param ins Instances object.
	 * @throws If something did go wrong with the testing process.
	 */
	public MLProcess(Instances ins) throws Exception {
		config = Configuration.getInstance();
		wr = new WEKARunner(ins);
		
		initWEKA();
		
		writer = new ACRResultWriter("MLProcess", config);
		eval = new Evaluation();
		fep = new FeatureExtractionProcess();
	}
	
	/**
	 * Sets the options for classifiers from the configuration and initializes them.
	 * <br>
	 * The related config options are: <br>
	 * classifier: e.g. ADABOOST <br>
	 * subclassifier: e.g. J48 <br>
	 * stacking: true <br>
	 * options: e.g. -N 4 -R<br>
	 * <br>
	 * Please consider the documentation for further information.
	 * 
	 * @throws If WEKA couldn't be initialized with the classifier.
	 **/
	private void initWEKA() throws Exception {
		/* do we have options for our classifiers? */
		if (this.config.getProperties().getProperty("options") != null &&
				this.config.getProperties().getProperty("options") != "") {
			options = this.config.getProperties().getProperty("options");
		}

		/* do we have a subclassifier? */
		if (config.getProperties().getProperty("subclassifier") != null &&
				config.getProperties().getProperty("subclassifier") != "") {
			
			/* do we have stacking enabled? */
			if (config.getProperties().getProperty("stacking").equals("true")) {
				wr.run(config.getProperties().getProperty("classifier"),
						config.getStackingClassifiers(), options);
			}
			else {
				wr.run(config.getProperties().getProperty("classifier"),
						config.getProperties().getProperty("subclassifier"), options);
			}
		}
		else {
			wr.run(config.getProperties().getProperty("classifier"), options);
		}
	}

	/**
	 * Runs the ACR-Systems on the test corpus and extracts the {@link CoreferencePair}s.
	 * 
	 * @param mmaxParser A parser object to parse the MMAX files in the test corpus.
	 * @return Mapping with the testcoreferences and the related ACR-System.
	 */
	private Map<String,List<CoreferencePair>> extractTestCorefs(
			MMAXParser mmaxParser) throws Exception {
		Map<String,List<CoreferencePair>> testCorefs = new HashMap<String,List<CoreferencePair>>();
		
		/* temporary container to convert our coreferences to a WEKA-compatible format */
		ic = new InstanceContainer();
		ic.createAttributeStructure(fep.getFeatureExtractorList());

		for (Runner acrsys : config.getAcRRunner()) {
			acrsys.init(mmaxParser);
			acrsys.run();
			
			List<CoreferencePair> predictionList =
				MMAXParser.splitCorefChainsToPairs(acrsys.getCoreferents());

			fep.extractFeatures(predictionList, mmaxParser);

			if (testCorefs.containsKey(acrsys.getClass().getCanonicalName())) {
				testCorefs.get(acrsys.getClass().getCanonicalName()).addAll(predictionList);
			}
			else {
				testCorefs.put(acrsys.getClass().getCanonicalName(), predictionList);
			}

		}
		return testCorefs;
	}

	/**
	 * Creates the classifications of the test-{@link CoreferencePair}s by using the classifier
	 * trained on the test-{@link CoreferencePair}s.
	 * 
	 * @param testCorefs {@link CoreferencePair}s extraced from the test corpus by the ACR-Systems.
	 * @return {@link CoreferencePair} which are predicted by our classifier to be correct.
	 */
	private List<CoreferencePair> createPrediction(
			Map<String,List<CoreferencePair>> testCorefs) throws Exception {
		List<CoreferencePair> predictions = new ArrayList<CoreferencePair>();
		for (String s : testCorefs.keySet()) {
			for (final CoreferencePair cp : testCorefs.get(s)) {
				Instance ini = ic.addCorefInstance(cp, s);
				ini.setDataset(ic.getInstances());

				/* use the classifier to select a label */
				if (wr.labelUnknownInstance(ini) == 0.0) {
					cp.setAcrSystem(ini.stringValue(ini.numAttributes()-2));
					predictions.add(cp);
				}
			}
		}
		predictions = removeDuplicates(predictions);
		return predictions;
	}

	/**
	 * Evaluates the predicted {@link CoreferencePair}s from our classifier against the gold
	 * standard.
	 * 
	 * @param mlPredictions Predictions from our classifier.
	 * @param goldCorefPairs Gold standard.
	 * @param fileName Results of the evaluation.
	 */
	private void doEvaluation(List<CoreferencePair> mlPredictions,
			List<CoreferencePair> goldCorefPairs, String fileName) {
		eval.evaluateCorefPairs(mlPredictions, goldCorefPairs);

		try {
			writer.writeCorefResults(fileName, eval.getTruePositiveList(),
					eval.getFalseNegativeList(), eval.getFalsePositiveList());
		} catch (IOException e) {
			System.err.println("Couldn't write output file.");
			e.printStackTrace();
		}
	}

	/**
	 * Removes duplicates in our list.
	 * 
	 * @param corefList list of our predicted {@link CoreferencePair}s with duplicates.
	 * @return list of our predicted {@link CoreferencePair}s without duplicates.
	 */
	private List<CoreferencePair> removeDuplicates(List<CoreferencePair> corefList) {
		int count = 0;
		List<CoreferencePair> targetList = new ArrayList<CoreferencePair>();

		for (CoreferencePair coreferencePair : corefList) {
			count = 0;
			for (CoreferencePair coreferencePair2 : corefList) {
				if (coreferencePair2.equals(coreferencePair)) {
					count +=1;
					continue;
				}
			}
			if (count < 2) {
				targetList.add(coreferencePair);
			}
		}
		return targetList;
	}

	/**
	 * Entry point for external callers.
	 * 
	 * <p>First the test corpus will be processed by the ACR-Systems. Afterwards the gold standard
	 * and features will be extracted. A Instances container will be created and the
	 * classifications of our classifier will be made. The predictions of our classifier will be
	 * evaluated against the gold standard and important statistical data will be saved to a
	 * file.</p>
	 * 
	 * @throws Exception If an error occurred during the MLProcess. 
	 */
	public void run() throws Exception {
		List<String> mmaxTestFiles = PerformanceMeasurementMain.getMMAxFiles("Test");

		for (String mmaxTestFile : mmaxTestFiles) {
			MMAXParser mmaxParser = MMAXParser.processMMAXFile(mmaxTestFile);
			
			/* gold standard */
			List<CoreferencePair> goldStandard = mmaxParser.getCorefPairs();

			/* feature extraction for the attribute structure */
			fep.extractFeatures(goldStandard, mmaxParser);

			ic = new InstanceContainer();
			ic.createAttributeStructure(fep.getFeatureExtractorList());

			/* test coreferences */
			Map<String,List<CoreferencePair>> testList = extractTestCorefs(mmaxParser);
			
			/* positive predictions */
			List<CoreferencePair> predictions = createPrediction(testList);
			
			//predictions = removeDuplicates(predictions);
			doEvaluation(predictions, goldStandard, mmaxTestFile);
		}
		this.writer.writeOverallResult(eval);
		
		String ResultOutputDir =
			Configuration.getInstance().getProperties().getProperty("ResultOutputDir");
		
		System.out.println("\n\n######\nTesting process finished.\nResults written to " +
				ResultOutputDir + "\n######\n");
	}
	
	/**
	 * Entry point for external callers with the possibility to specify FeatureSets and results.
	 * This method is useful to save runtime, if the MLProcess has to be executed several times. 
	 * 
	 * @param fep The {@link FeatureExtractionProcess} containing the desired list of features.
	 * @param gold The {@link MMAXParser} containing the gold standard.
	 * @param testList Predictions of ACR systems for the current file.
	 * @throws Exception If an error occurred during the MLProcess. 
	 */
	public void run(FeatureExtractionProcess fep, MMAXParser gold,
			Map<String,List<CoreferencePair>> testList) throws Exception {
		
		/* gold standard */
		List<CoreferencePair> goldStandard = gold.getCorefPairs();
		
		/* feature extraction for the attribute structure */
		fep.extractFeatures(goldStandard, gold);
		ic = new InstanceContainer();
		ic.createAttributeStructure(fep.getFeatureExtractorList());
		
		for(List<CoreferencePair> corefPairs : testList.values()) {
			fep.extractFeatures(corefPairs, gold);
		}
		
		/* positive predictions */
		List<CoreferencePair> predictions = createPrediction(testList);
		//predictions = removeDuplicates(predictions);
		doEvaluation(predictions, goldStandard, gold.getDiscourse().getCommonBasedataPath());
		//this.writer.writeOverallResult(eval);
	}
	
	/**
	 * Setter to manually set the features which should be extracted.
	 * @param fep The {@link FeatureExtractionProcess} containing the desired features.
	 */
	public void setFep(FeatureExtractionProcess fep) {this.fep = fep;}

	/** 
	 * Getter for the {@link Evaluation} results (to compare multiple MLProcesses). 
	 * @return The corresponding {@link Evaluation} object.
	 */
	public Evaluation getEval() {return eval;}
}
