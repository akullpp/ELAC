/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction.features
 * class: FE_stringMatch
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
package de.uniheidelberg.cl.swp.featureExtraction.features;


import java.util.ArrayList;
import java.util.List;
import weka.core.Attribute;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;
import de.uniheidelberg.cl.swp.util.MMAXParser;

/**
 * This Feature Extractor checks whether anaphora & antecedent match in their string representation.
 *
 */
public class FE_stringMatch extends AbstractFeatureExtractor {
	
	/**
	 * Initializes the Feature Extractor and sets its name. 
	 */
	public FE_stringMatch() { this.name = "stringMatch"; }

	@Override
	public void extractFeatures(CoreferencePair corefPair, MMAXParser mmax) {
		String word1 = corefPair.getAnaphor().getWordList().get(0).getToken();
		String word2 = corefPair.getAntecedent().getWordList().get(0).getToken();
		
		Feature<Boolean> feature = new Feature<Boolean>(this.name);
		feature.setValue(word1.equals(word2));
		feature.setFtype(FeatureType.BOOLEAN);
		
		addFeatureToCoref(feature, corefPair);
	}	

	@Override
	public Attribute getWekaAttribute() {
		List<String> boolVector = new ArrayList<String>();
		boolVector.add("true");
		boolVector.add("false");
		return new Attribute(this.name, boolVector);
	}
}