/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction
 * class: AbstractFeatureExtractor
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

import weka.core.Attribute;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/** Common methods/fields for every feature extractor.
 * <br>
 * A Feature Extractor handles feature extraction and distribution in respect to 
 * a {@link CoreferencePair}.<br>
 * The Feature Extractor is a unique object and adds a corresponding instance 
 * of {@link Feature} + the correct value for this feature.
 */
public abstract class AbstractFeatureExtractor {

	protected  String name;	
	
	/**
	 * Getter for the name of the FeatureExtractor.
	 * This name is also used in the {@link Configuration} file.
	 * @return The name for the FeatureExtractor.
	 */
	public String getName() { return name; }	
	
	/**
	 * Returns the {@link Attribute} which is used for Weka attributes. 
	 * 
	 * @return a Weka {@link Attribute} representing the feature as an Attribute.
	 */
	public abstract Attribute getWekaAttribute();	
	
	/**
	 * Extracts the features for a coreference pair.
	 * @param corefPair The corefPair for which the feature should be extracted.
	 * @param mmax The MMAX file for the {@link CoreferencePair}. This serves as 
	 *   source for the feature values.
	 */
	public abstract void extractFeatures(CoreferencePair corefPair, MMAXParser mmax);	
	
	/**
	 * Adds the extracted {@link Feature} value to the corresponding {@link CoreferencePair}.
	 * @param feature The {@link Feature} which has been extracted.
	 * @param corefPair The {@link CoreferencePair} to which the {@link Feature} value should
	 *   be added.
	 */
	public void addFeatureToCoref(Feature<?> feature, CoreferencePair corefPair) {
		corefPair.addFeature(feature);
	}
}
