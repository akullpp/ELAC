/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.testacr
 * class: PerformanceMeasurementMain
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
package de.uniheidelberg.cl.swp.testacr;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import org.eml.MMAX2.core.MMAX2;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.featureExtraction.FeatureExtractionProcess;
import de.uniheidelberg.cl.swp.io.ACRResultWriter;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.io.Logging;
import de.uniheidelberg.cl.swp.io.MMAXFileFilter;
import de.uniheidelberg.cl.swp.mlprocess.InstanceContainer;
import de.uniheidelberg.cl.swp.mlprocess.WEKARunner;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * This class handles the whole process of Feature Dependent 
 * Performance Measurement.
 * 
 * The following steps are done:
 * <ul>
 * <li>Reads the {@link Configuration} and loads all the MMAX files in the specified
 * directories.</li>
 * <li>Processes the MMAX files and extracts {@link CoreferencePair}s and Features.</li> 
 * <li>Runs each ACR on each file</li>
 * <li>Performs the main {@link Evaluation}.</li>
 * </ul>s
 */
public class PerformanceMeasurementMain {

	/**
	 * Configuration object, always referring to the same config file,
	 * see {@link Configuration}.
	 */
	Configuration config;
	
	/**
	 * The instance container is a bucket for Weka Instances.
	 */
	InstanceContainer instances;
	
	/**
	 * LoggerInstance to log results/warnings. 
	 * See {@link Logging} for more details.
	 */
	Logger logger = Logging.getInstance().getLogger();
	
	
	
	/**
	 * Start the evaluation process with a user defined config file.
	 * @param configFile Path to a specific config file which should be used.
	 */
	public PerformanceMeasurementMain(String configFile) {
		// load the configuration file for current evaluation process
		this.config = Configuration.getInstance(configFile);
	}
	
	/**
	 * Default constructor: Start the evaluation process with the
	 * standard configuration file.
	 */
	public PerformanceMeasurementMain() {this(Configuration.defaultPropPath);}
	
	
	/**
	 * Runs the whole evaluation pipeline.<br>
	 * All the parameters are set by configFile.<br>
	 * <br>
	 * 1. Get all ACR system which should be evaluated.<br>
	 * 2. For each ACR system<br>
	 * 		- Get all the training files in the specified directory (see {@link Configuration}.<br>
	 * 		- Extract the basedata (raw words) and correct coreferences (gold standard)<br>
	 * 		- Split gold standard into CorefPairs<br>
	 * 		- Extract all features from CorefPairs (gold standard)<br>
	 * 	  2.1 Run each acr systems in acrRunner.run()<br>
	 * 	  2.2 Get predicted corefs by ACR-System<br>
	 * 	  2.3 Measure performance.<br>
	 * 3. Write the results to an .arff-File.<br>
	 */
	public void run() throws Exception {
		this.logger.info("--> Starting Performance Measurement <--");
		
		// set up a container to store the instances for weka
		this.instances = new InstanceContainer();
		FeatureExtractionProcess featureExtraction = new FeatureExtractionProcess();
		
		/* The files which will be used to measure the performance of each acr */
		List<String> mmaxTestFiles =  getMMAxFiles("Training");
		
		// iterate over all runner systems
		for (Runner curAcr: this.config.getAcRRunner()) {
			
			// create a Evaluation instance for the current runner
			Evaluation eval = new Evaluation();
			
			this.logger.info("[*** Loading ACR-system " + 
							  curAcr.getClass().getCanonicalName() + " ***]");

			// Create an output file
			ACRResultWriter outputFile = new ACRResultWriter(curAcr.getClass().getCanonicalName(), 
														     this.config);
			
			// write the feature selectors to the output file
			outputFile.writeFeatureFilters(config);
			

			// run the acr system for each mmax file 
			for (String mmaxFile : mmaxTestFiles) {
				this.logger.info("\n\n*** Loading " + mmaxFile);
				
				// processing an mmax-file
				MMAXParser mmaxParser = MMAXParser.processMMAXFile(mmaxFile);
				List<CoreferencePair> corefPairsGold = mmaxParser.getCorefPairs();
				
				
				this.logger.info("Gold standard contains " + corefPairsGold.size() +
						" coref pairs");
				// extract the coreference pair features
				this.logger.info("Extracting features for coreference pairs");
				
				featureExtraction.extractFeatures(corefPairsGold, mmaxParser);
				instances.createAttributeStructure(featureExtraction.getFeatureExtractorList());
				
				
				List<CoreferencePair> hypothesis = new ArrayList<CoreferencePair>();
				
				this.logger.info(" \t\t [*** Running ACR-system " + 
								   curAcr.getClass().getCanonicalName() + " ***]");

				/* in case anything goes wrong: skip the whole process as the results would be
				 * wrong otherwise.
				 */ 
				try {
					// run the acr-systems and get their hypothesis
					curAcr.init(mmaxParser);
					curAcr.run();
					// split coreference chains into pairs
					hypothesis = MMAXParser.splitCorefChainsToPairs(curAcr.getCoreferents());
					 
					featureExtraction.extractFeatures(hypothesis, mmaxParser);
					
					this.logger.info("ACR " + curAcr.getClass().getCanonicalName()  + 
								    	" predicted " + hypothesis.size() + " entities.");
					
					eval.evaluateFeatureDep(hypothesis, corefPairsGold);
					outputFile.writeCorefResults(mmaxFile, eval.getTruePositiveList() , 
												eval.getFalseNegativeList(),
												eval.getFalsePositiveList());
					
					hypothesis.clear();
					this.logger.info("\nCurrent average recall = " + 
									(eval.getTruePositives() / (eval.getTruePositives() + 
									 eval.getFalseNegatives())) + "\n\n\n");
					
					// store true positives in a weka file
					this.instances.addCorefInstances(eval.getTruePositiveList(), 
													 curAcr.getClass().getCanonicalName(), "+");

					// store a balanced list of false positives
					List<CoreferencePair> balancedList = 
												balanceNegativeList(eval.getFalsePositiveList());
					this.instances.addCorefInstances(balancedList, 
													 curAcr.getClass().getCanonicalName(), "-");
				}	
				catch (Exception e) {
					this.logger.warning("Couldn't run ACR-system " + 
										curAcr.getClass().getCanonicalName() + 
										" with file " + mmaxFile);
					e.printStackTrace();
					throw e;
				}
				
			}
			this.logger.info("Overall recall: " + 
								(eval.getTruePositives() / 
										(eval.getTruePositives() + eval.getFalseNegatives())));
			
			outputFile.writeOverallResult(eval);
			
			
			
			String arffPath = Configuration.getInstance().getProperties().
												  getProperty("ResultOutputDir") +
												  "results.arff";
			
			WEKARunner.writeInstancesToArff(this.instances.getInstances(), arffPath);
			
			System.out.println("\n\n######\nTraining process finished.\nARFF file written to " +
					arffPath + "\n######\n");
		}
	}
	
	/**
	 * As too many negative examples heavily decrease the precision of all our ml classifiers, 
	 * we balance the amount of negative examples. Therefore we just take half of the amount of 
	 * false positives.
	 *    
	 * @param falsePositives The list of False positives which has to be balanced.
	 * @return A balanced list containing the half of the examples of the original list. 
	 */
	private List<CoreferencePair> balanceNegativeList(List<CoreferencePair> falsePositives) {
		List<CoreferencePair> balancedList = new ArrayList<CoreferencePair>();
		
		for (int i = 0; i < falsePositives.size() -2; i+=2) {
			balancedList.add(falsePositives.get(i));
		}
		
		return balancedList;
	}
	
	/**
	 * Returns the mmaxFiles in the specified directory (either "Training" or
	 * "Test"). The value for the path is looked up via {@link Configuration}.
	 * 
	 * @param directory Directory with MMAX files.
	 * @return  A list of {@link MMAX2} fileNames.
	 */
	public static  List<String> getMMAxFiles(String directory) {
		String path = Configuration.getInstance().getDirectory(directory);
		List<String> fileList = new ArrayList<String>();
		File[] mmaxFiles = new File(path).listFiles(new MMAXFileFilter());
		
		// throw an exception, if there now mmax-files could be found
		if (mmaxFiles == null)
			throw new IllegalArgumentException("No mmax files found at the specified directory: " + 
												directory);
		
		Logging.getInstance().getLogger().info(mmaxFiles.length + " mmax files will be tested.");
		
		for (int i = 0; i < mmaxFiles.length; i++) {fileList.add(mmaxFiles[i].getAbsolutePath());}
		
		return fileList;
	}
}
