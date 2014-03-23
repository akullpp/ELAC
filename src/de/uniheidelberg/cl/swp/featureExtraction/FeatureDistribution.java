/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction
 * class: FeatureDistribution
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
package de.uniheidelberg.cl.swp.featureExtraction;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.io.MMAXFileFilter;
import de.uniheidelberg.cl.swp.util.MMAXParser;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.featureExtraction.FeatureExtractionProcess;


/**
 * This class provides methods to output the distribution of different features in a given corpus. 
 * <br>
 * <p>It is not part of the main routine, but is implemented for testing/development purposes.
 * Features to be examined have to be implemented as a class and provided with a type from the 
 * {@link de.uniheidelberg.cl.swp.featureExtraction.features.FeatureType} class. <br>
 * The main method gives as argument the name/type of the corpus, which itself has to be linked
 * and labeled in the config file.<br>
 * Class can be accessed the same way by other classes using the run() method.</p>
 */
public class FeatureDistribution {
	private File file;
	private String directory;
	private String outputDir;
	
	/**
	 * Initializes the feature-extraction.
	 * 
	 * @param type Either "Test" or "Training" corpus is used.
	 * @param outputFile Path to the output file.
	 */
	public FeatureDistribution(String type, String directory) {
		this.directory = directory;
		
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy-HH-mm");
        Date date = new Date();
        String timeStamp = dateFormat.format(date);
		
        this.outputDir =
        	Configuration.getInstance().getProperties().getProperty("ResultOutputDir") + "Distribution_" + type;
        
        file = new File(this.outputDir + "_" + timeStamp + ".txt");
		
        getGoldFeatures(type);
		
	}
	
	/**
	 * Method to get all the MMAX files of which a corpus consists. Code copied and adapted
	 * from {@link de.uniheidelberg.cl.swp.testacr.PerformanceMeasurementMain} class.
	 * 
	 * @param directory Parameter to decide from where to read MMAX files. Directory 
	 * 		paths can be specified in the config file (also refer to {@link Configuration} class).
	 * @return A list of strings representing the paths to MMAX files. 
	 */
	public static List<String> getMMAxFiles(String directory) {
		List<String> fileList = new ArrayList<String>();
		File[] mmaxFiles = new File(directory).listFiles(new MMAXFileFilter());
		
		/* Throw an exception, if no MMAX-files could be found there. */
		if (mmaxFiles == null)
			throw new IllegalArgumentException
			("No mmax files found at the specified directory: " + directory);
		
		for (int i = 0; i < mmaxFiles.length; i++) {
			fileList.add(mmaxFiles[i].getAbsolutePath());
		}
		return fileList;
	}
	
	/**
	 * Method first extracts the features of the gold standard using an {@link MMAXParser},
	 * checks the features in their feature vectors, counts them and writes them into a
	 * file together with their count. 
	 * For numeric features the average of their value is calculated in addition. 
	 * 
	 * @param type Parameter to decide whether to use training or testing set (configurable in
	 * 				config.xml file).
	 */
	public void getGoldFeatures(String type) {		
		List<String> mmaxFiles =  getMMAxFiles(this.directory);
		FeatureExtractionProcess featureExtraction = new FeatureExtractionProcess();
		
		List<CoreferencePair> corefPairs = new ArrayList<CoreferencePair>();
		Map<String,Map<String,Double>> numFeatures = new HashMap<String,Map<String,Double>>();
		Map<String,Double> numFeaturesAvg = new HashMap<String,Double>();
		Map<String,Map<String,Integer>> otherFeatures = new HashMap<String,Map<String,Integer>>();

		BufferedWriter output = null;
		
		try{
			FileWriter fw = new FileWriter(file);
			output = new BufferedWriter(fw);
		}
		catch (FileNotFoundException e) {
			System.err.println("Could not write output file, check permissions!");
			e.printStackTrace();
		}
		catch (IOException e) {
			System.err.println("Could not write output file, check permissions!");
			e.printStackTrace();
		}	
		
		for(String mmax : mmaxFiles){
			MMAXParser mmaxParser = MMAXParser.processMMAXFile(mmax);
			List<CoreferencePair> corefPairsGold = mmaxParser.getCorefPairs();					
			
			try{
				featureExtraction.extractFeatures(corefPairsGold, mmaxParser);				
			}
			catch(IOException io){
				io.printStackTrace();
			}
			
			for (CoreferencePair coref : corefPairsGold){
				corefPairs.add(coref);
				
				for (Feature<?> f : coref.getFeatuerVector() ) {			
					if (f.getFtype().toString() == "NUMERIC") {
						
						/* This handles total numeric feature distribution. */
						if (numFeatures.get(f.getName()) == null) {
							numFeatures.put(f.getName(), new HashMap<String, Double>());
						}
						if (numFeatures.get(f.getName()).get(f.getStringValue()) == null) {
							numFeatures.get(f.getName()).put(f.getStringValue(), 1.);
						}else{
							double currentVal =
								numFeatures.get(f.getName()).get(f.getStringValue());
							numFeatures.get(f.getName()).put(f.getStringValue(), currentVal+1);							
						}
						
						/* This handles average numeric feature distribution. */
						if (numFeaturesAvg.get(f.getName()) == null) {
							numFeaturesAvg.put(f.getName(), 0.);
						}
						double currentVal = numFeaturesAvg.get(f.getName());
						numFeaturesAvg.put(f.getName(), currentVal +
								Double.parseDouble(f.getStringValue()));	
											
					}
					
					/* This handles distribution of all features of other types. */
					else {
						if (otherFeatures.get(f.getName()) == null) {
							otherFeatures.put(f.getName(), new HashMap<String, Integer>());
						}
						if (otherFeatures.get(f.getName()).get(f.getStringValue()) == null) {
							otherFeatures.get(f.getName()).put(f.getStringValue(), 1);
						} else {
							int currentVal =
								otherFeatures.get(f.getName()).get(f.getStringValue());
							otherFeatures.get(f.getName()).put(f.getStringValue(), currentVal+1);
						}						
					}
				}			
			}			
		}
		try {
			output.write("Processing " + this.directory + "\n\n");
			
			/* Different handling depending on feature type (numeric/other). Numeric output 
			 * both as average and total values. */
			for (Feature<?> f : corefPairs.get(0).getFeatuerVector()) {
				if (f.getFtype().toString() == "NUMERIC") {
					double count = numFeaturesAvg.get(f.getName());
					
					output.write("\n\nAverage distribution of FE_" + f.getName() + ": " +
							(count/corefPairs.size()));
					output.write("\nTotal distribution of FE_" + f.getName() + ":\n");

					for (String possibleValue : numFeatures.get(f.getName()).keySet()) {
						output.write(possibleValue + ": " + 
								numFeatures.get(f.getName()).get(possibleValue) + "\n");
					}					
				}
				else {
					output.write("\nTotal distribution of FE_" + f.getName() + ":\n"); 
					
					for (String possibleValue : otherFeatures.get(f.getName()).keySet()) {
						output.write(possibleValue + ": " + 
								otherFeatures.get(f.getName()).get(possibleValue) + "\n");
					}					
				}
			}
			output.close();
			System.out.println("\n\n######\nAnalysis of feature distribution for " + type + 
					" finished.\nPlease view " + file.toString() + "\n######\n");
		} catch(IOException io) { io.printStackTrace(); }	
	}	
	
	/**
	 * Main method for testing purposes.
	 * 
	 * @param args Type of corpus to be used and output file.
	 * @throws If there's an error with the feature-extraction.
	 */	
	public static void main(String[] args) throws IOException { 
		/* specify whether the "Training" or "Test" corpus will be used. */
		String corpusType = args[0];
		
		/* gets the path to the corpus of the specified type. */
		String corpusPath = Configuration.getInstance().getDirectory(corpusType);
		
		try {
			if (!new File(corpusPath).exists()) { throw new IOException(); }
			else { new FeatureDistribution(corpusType, corpusPath); }
		}
		catch (IOException io) {
			System.err.println("Path " + corpusPath + " not found, check configuration file!");
			io.printStackTrace();
		}
	}	
}
