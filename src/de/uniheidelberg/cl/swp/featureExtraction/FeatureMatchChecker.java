/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction
 * class: FeatureMatchChecker
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.io.Logging;


/**
 * This class checks whether a coreference pair matches the required features specified by user 
 * configuration. As only certain features should be reflected by the evaluation process, this 
 * filtering is used to sort out the feature values which are not of interest.
 *
 */
public class FeatureMatchChecker {

	Configuration config;
	Logger log;
	
	/**
	 * Initializes the {@link FeatureMatchChecker} by setting the correct {@link Configuration} 
	 * and {@link Logging} options.
	 */
	public FeatureMatchChecker() {
		this.config = Configuration.getInstance();
		this.log = Logging.getInstance().getLogger(); 
	}	
	
	/**
	 * Filters out coreference pairs which don't match the required features and therefore
	 * shouldn't be evaluated.
	 * 
	 * @param corefPairs The coref pairs which will be filtered
	 * @return List of {@link CoreferencePair} which should be evaluated.
	 */
	public List<CoreferencePair> filterNonMatchingFeatures(List<CoreferencePair> corefPairs) {
		this.log.info("Filtering out non-matching corefpairs");
		
		List<CoreferencePair> targetList = new ArrayList<CoreferencePair>();
		
		for (CoreferencePair crp : corefPairs) {
			if (doesMatchRequiredFeatures(crp)) {
				targetList.add(crp);
			}
		}
		this.log.info(targetList.size() + " coref pairs matched the required features");
		return targetList;		
	}
		
	/**
	 * Checks whether the current coreference pair shows feature values which have 
	 * been specified by the user.
	 * 
	 * @param crp The coreference pair which is checked.
	 * @return true if the current pair should be used for testing, false otherwise.
	 */
	public boolean doesMatchRequiredFeatures(CoreferencePair crp) {
		/* Go through all the features which are used to filter coreference pairs (specified by 
		 * configuration) */
		
		if (this.config.getFeatureFilter() == null) return true;
		
		boolean featureFilterFound = false;
		
		for (String fe : this.config.getFeatureFilter()) {
			featureFilterFound = false;
			/* Get all feature vectors and compare whether they should be regarded or not. */
			for (Feature<?> crpFe : crp.getFeatuerVector()) {
				if (crpFe.getName().equals(fe)) {
					featureFilterFound = true;
					/* If one filter criterion does not match: reject current pair. */
					if (! doesMatch(crpFe)) return false;
				}			
			}			
			if (!featureFilterFound) {
				this.log.severe("Could not find feature extractor " + fe);
				throw new IllegalArgumentException("Could not find feature " + fe);
			}			
		}
		return true;
	}	
	
	/**
     * Reads the filter value for the current feature from property file and compares, whether the 
     * value featureValue matches the required value.
     * @param fe The feature currently being examined.
     * @return true if the features match, false otherwise.
     */
	public boolean doesMatch(Feature<?> fe) {
		String filterValue = this.config.getFeatureSelector(fe.getName());
		
		switch (fe.getFtype()) {
		case NUMERIC:
			int min = Integer.parseInt(filterValue.split(";")[0]);
			int max = Integer.parseInt(filterValue.split(";")[1]);
			int featureValue = Integer.parseInt(fe.getStringValue());
			return (featureValue>= min && featureValue <= max);
		
		/* For PosTags, there is an enumeration of possible features. If one of the specified 
		 * PosTags matches, return true. */
		case POSTAG:
			String[] values = filterValue.split(";");
			for (String value : values) {
				if (fe.getStringValue().toLowerCase().equals(value.toLowerCase()))
					return true;
			}
			return false;
			
		case NETYPE:
			String[] neValues = filterValue.split(";");
			for (String value : neValues) {
				if (fe.getStringValue().toLowerCase().equals(value.toLowerCase()))
				return true;
			}
		return false;
			
		default: return (fe.getStringValue().toLowerCase().equals(filterValue.toLowerCase()));
		}		
	}	
}