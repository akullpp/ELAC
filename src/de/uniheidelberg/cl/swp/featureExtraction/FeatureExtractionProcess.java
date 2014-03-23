/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction
 * class: FeatureExtractionProcess
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
package de.uniheidelberg.cl.swp.featureExtraction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.io.Logging;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * This class encapsulates the feature extraction process.
 */
public class FeatureExtractionProcess {
	List<AbstractFeatureExtractor> featureExtractorList;

	/**
	 * Initializes the necessary data structures.
	 * <br>
	 * A new feature-extractor list will be created.
	 */
	public FeatureExtractionProcess() {
		this.featureExtractorList = new ArrayList<AbstractFeatureExtractor>();
		
		/* Iterate over feature extractors in Enum FeatureExtractors. */
		for (FeatureExtractors fe : FeatureExtractors.values()) {
			featureExtractorList.add(fe.getFe());
		}
	}
	
	/**
	 * Initializes the necessary data structures.
	 * <br>
	 * An existing feature-extractor list can be specified.
	 * 
	 * @param featureList A custom feature list.
	 */
	public FeatureExtractionProcess(List<AbstractFeatureExtractor> featureList) {
		this.featureExtractorList = featureList;
	}	
	
	/**
	 * Extract features for the given coreference pairs.
	 * 
	 * @param corefPairs The {@link CoreferencePair}s for which features will be extracted.
	 * @param mmax The MMAX file (serves as source for feature values)
	 * @throws If the features couldn't be extracted.
	 */
	public void extractFeatures(List<CoreferencePair> corefPairs, MMAXParser mmax) 
		throws IOException {
		/* Iterate over all coreference pairs in the gold standard.
		 * Let each feature extractor run on all the coreference pairs.
		 * Extract ALL features (not only those used for evaluation). */
		Logging.getInstance().getLogger().info("Extracting features");
		
		/* Extract the feature and add it to the feature vector. */
		for (CoreferencePair corefPair : corefPairs) {	
			/* If a feature vector already exists: delete it and start from scratch */
			corefPair.getFeatuerVector().clear();
			for (AbstractFeatureExtractor fe : this.featureExtractorList ) {
				try {
					fe.extractFeatures(corefPair, mmax);
				} catch (Exception e) {
					System.err.println("Couldn't extract feature " + fe.getName() + 
					"- See the log file for error information.");
					Logging.getInstance().getLogger().logrb(Level.SEVERE, "FeatureExtraction", 
													"extractFeatures", null, fe.getName(), e);
				}
				
			}			
		}
	}
	
	/**
	 * Setter for the feature list.
	 * 
	 * @param featureExtractorList List of the features.
	 */
	public void setFeatureExtractorList(List<AbstractFeatureExtractor> featureExtractorList) {
		this.featureExtractorList = featureExtractorList;
	}
	
	/**
	 * Getter for the feature list.
	 * 
	 * @return Feature list.
	 */
	public List<AbstractFeatureExtractor> getFeatureExtractorList() {
		return featureExtractorList;
	}
}
