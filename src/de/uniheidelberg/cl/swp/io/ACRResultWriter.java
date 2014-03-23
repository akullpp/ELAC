/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.io
 * class: ACRResultWriter
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
package de.uniheidelberg.cl.swp.io;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;
import de.uniheidelberg.cl.swp.featureExtraction.FeatureExtractionProcess;
import de.uniheidelberg.cl.swp.testacr.Evaluation;


/**
 * This class is used to write the feature dependent performance results for one specific
 * classifier to an output file.
 * <br>
 * <p>The output file contains the features and the values which must been matched by the 
 * {@link CoreferencePair}s to be considered for performance testing. Furthermore the results for 
 * each test file are printed as well as the overall result.
 */
public class ACRResultWriter {
	private BufferedWriter outputFile;
	
	/**
	 * Creates a new output file for the current ACR-System.
	 * <br>
	 * The file will be named as follows: [ClassName]-[timeStamp].txt. <br>
	 * The path to the output file is specified by {@link Configuration} via the attribute
	 * "ResultOutputDir".
	 * 
	 * @param runner The current ACR-System being evaluated.
	 * @param config The {@link Configuration} file which provides the output path.
	 */
	public ACRResultWriter(String runner, Configuration config) {
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy-HH-mm");
        Date date = new Date();
        String timeStamp = dateFormat.format(date);

		File f = new File(config.getProperties().getProperty("ResultOutputDir") + runner + "_" +
				timeStamp  + ".txt");
		try {
			FileWriter fr = new FileWriter(f);
			this.outputFile = new BufferedWriter(fr);

			outputFile.write("Output for Evaluation\n\n");
			outputFile.write("Evaluating '" + runner + "'\n");
			
			if (runner.equals("MLProcess")) {
				if (config.getProperties().getProperty("stacking") != null &&
						config.getProperties().getProperty("stacking") != "") {
					outputFile.write("Stacking enabled\n");
				}
				
				outputFile.write("Classifier: " +
						config.getProperties().getProperty("classifier") + "\n");
				
				if (config.getProperties().getProperty("options") != null &&
						config.getProperties().getProperty("options") != "") {
					outputFile.write("Options: " + config.getProperties().getProperty("options") +
							"\n");
				}
				
				if (config.getProperties().getProperty("subclassifier") != null &&
						config.getProperties().getProperty("subclassifier") != "") {
					outputFile.write("Subclassifier: " +
							config.getProperties().getProperty("subclassifier") + "\n");
				}
			}
			this.writeFeatureVectors();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Writes the currently set feature selectors (determined by {@link Configuration}) and the
	 * desired values for the features to the file.
	 * 
	 * @param config Config file which is used to get the feature selectors and their values.
	 * @throws If the feature selectors couldn't be written.
	 */
	public void writeFeatureFilters(Configuration config) throws IOException {
		outputFile.write("\n\n\n************** FEATURE SELECTORS **************\n\n\n");
		
		if (config.getFeatureFilter() == null) {
			outputFile.write("No feature filter set. Evaluating all features.\n");
			
			return;
		}
		
		/* write feature values */
		for (String fe : config.getFeatureFilter()) {
			outputFile.write("\nFiltering feature : "+ fe);
			outputFile.write("\t Only select Corefer.pairs with this feature value: " +
					config.getFeatureSelector(fe));
		}
	}
	
	/**
	 * Writes which features in which order will be extracted.
	 * 
	 * @throws If the features couldn't be written.
	 */
	private void writeFeatureVectors() throws IOException {
		outputFile.write("\n\nThe following features are extracted for each Coreference Pair: \n");
		
		FeatureExtractionProcess fep = new FeatureExtractionProcess();
		
		for (AbstractFeatureExtractor featureExtractor : fep.getFeatureExtractorList()) {
			outputFile.write(featureExtractor.getName() + "; ");
		}
	}
	
	/**
	 * Writes the results for one specific MMAX-file.
	 * <br>
	 * Looks up the {@link Configuration} value for the key "PrintCorrect/WrongCorefPairs" to
	 * determine whether to also print each instance which has been determined correctly/which
	 * hasn't been classified by the ACR-System. 
	 * 
	 * @param tpList The list of true positive {@link CoreferencePair}s.
	 * @param fnList The list of false negative {@link CoreferencePair}s.
	 * @param fpList The list of false positive {@link CoreferencePair}s.
	 * @throws If the results couldn't be written.
	 */
	public void writeCorefResults(String fileName, List<CoreferencePair> tpList,
			List<CoreferencePair> fnList, List<CoreferencePair> fpList) throws IOException {
		Configuration config = Configuration.getInstance();
		
		outputFile.flush();
		outputFile.write("\n\n\n*************** NEW FILE ***************\n");
		outputFile.write(fileName + "\n");
		
		if (Boolean.parseBoolean(config.getProperties().getProperty("PrintCorrectCorefPairs"))) {
			/*iterate over all true positive corefPairs */
			for (CoreferencePair coreferencePair : tpList) {
				outputFile.write("[TP] ");
				writeCorefPair(coreferencePair);
			}
		}
		
		if (Boolean.parseBoolean(config.getProperties().getProperty("PrintWrongCorefPairs"))) {
			/* iterate over all false negatives */
			for (CoreferencePair coreferencePair : fnList) {
				outputFile.write("[FN] ");
				writeCorefPair(coreferencePair);
			}
			/* iterate over all false positives */
			for (CoreferencePair coreferencePair : fpList) {
				outputFile.write("[FP] ");
				writeCorefPair(coreferencePair);
			}
		}
		outputFile.write("\n\n\n$$$$$$$$ File result $$$$$$$$\n");
		outputFile.write("Coreference Pairs: " + (tpList.size() + fnList.size()) + "\n");
		outputFile.write("True positives: " + tpList.size()+ "\n");
		outputFile.write("False negatives (not recognized): " + fnList.size()+ "\n");
		outputFile.write("False positives (erroneously predicted): " + fpList.size()+ "\n");
	}
	
	/**
	 * Writes a single {@link CoreferencePair} with all it's features to the output file.
	 * 
	 * @param crp The {@link CoreferencePair} which should be written.
	 * @throws If the coreference pair couldn't be written.
	 */
	private void writeCorefPair(CoreferencePair crp) throws IOException {
		for (Feature<?> feature : crp.getFeatuerVector()) {
			outputFile.write(feature.getStringValue() + "; ");
		}
		if (crp.getAcrSystem() != null) {
			outputFile.write(crp.getAcrSystem() + " |");
		}
		outputFile.write(" " + crp.getAntecedent().getWordList().get(0).getToken() + " (" +
				crp.getAntecedent().getWordList().get(0).getXmlId() + ") ... " +
				crp.getAnaphor().getWordList().get(0).getToken() + " (" +
				crp.getAnaphor().getWordList().get(0).getXmlId() + ")\n");
	}
	
	/**
	 * Writes the overall performance result.
	 * 
	 * @param eval An {@link Evaluation} object which contains the performance data.
	 * @throws If the overall result couldn't be written.
	 */
	public void writeOverallResult(Evaluation eval) throws IOException {
		outputFile.write("\n\n\n$$$$$$$$$$$$$$$$ OVERALL RESULTS $$$$$$$$$$$$$$$$\n\n");
		outputFile.write("Coreference Pairs: " + (eval.getTruePositives() +
				eval.getFalseNegatives()) + "\n");
		outputFile.write("True positives: " + eval.getTruePositives() + "\n");
		outputFile.write("False negatives: " + eval.getFalseNegatives() + "\n");
		outputFile.write("False positives: " + eval.getFalsePositives() + "\n");
		
		double recall = eval.getTruePositives() / (eval.getTruePositives() +
				eval.getFalseNegatives());
		double precision = eval.getTruePositives() / (eval.getTruePositives() +
				eval.getFalsePositives());
		double fscore = 2 * ((precision * recall) / (precision + recall));
		
		outputFile.write("###\n");
		outputFile.write("#Precision: " + precision + "\n");
		outputFile.write("#Recall: " + recall + "\n");
		outputFile.write("#F-Score: " + fscore + "\n");
		outputFile.write("###\n");

		outputFile.close();
	}
}
