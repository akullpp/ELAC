/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction
 * class: FeatureExtractors
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


/**
 * An enumeration containing all the feature extractors.
 */
public enum FeatureExtractors {
	
	/**
	 * See {@link features.FE_anaphoraPos}.
	 */
	FE_ANAPHORAPOS ("features.FE_anaphoraPos"),
	
	/**
	 * See {@link features.FE_isSubj}.
	 */
	FE_ISSUBJ("features.FE_isSubj"),
	
	/**
	 * See {@link features.FE_distance}.
	 */
	FE_DISTANCE ("features.FE_distance"),
	
	/**
	 * See {@link features.FE_stringMatch}.
	 */
	FE_STRINGMATCH ("features.FE_stringMatch"),
	
	/**
	 * See {@link features.FE_antecedentPos}.
	 */
	FE_ANTECEDENTPOS ("features.FE_antecedentPos"),
	
	/**
	 * See {@link features.FE_sentenceOffset}.
	 */
	FE_SENTENCEOFFSET ("features.FE_sentenceOffset"),
	
	/**
	 * See {@link features.FE_pronounCountSentence}.
	 */
	FE_PRONOUNCOUNTSENTENCE ("features.FE_pronounCountSentence"),
	
	/**
	 * See {@link features.FE_neType}.
	 */
	FE_NETYPE ("features.FE_neType");
			
	private final String fe;
	
	/**
	 * Initializes the FeatureExtractor by setting the class name string.
	 * 
	 * @param fe String for class name.
	 */
	private FeatureExtractors(String fe) { this.fe = fe; }
	
	/**
	 * This method looks up the class name for the corresponding {@link FeatureExtractors} and 
	 * returns a new instance of it.
	 * 
	 * @return an instance of the corresponding {@link AbstractFeatureExtractor}.
	 */
	public AbstractFeatureExtractor getFe() {
		String feName = "de.uniheidelberg.cl.swp.featureExtraction." + this.fe; 
		try {
			return (AbstractFeatureExtractor) Class.forName(feName).newInstance();
		} catch (InstantiationException e) {
			System.err.println("Cannot instanciate " +feName +"!");
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			System.err.println("Cannot create instace for " +feName +"!");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("Class " +feName +"not found!");
			e.printStackTrace();
		}
		return null;
	}
}
