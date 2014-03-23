/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction.features
 * class: FE_distance
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

import weka.core.Attribute;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * This feature extractor extracts the feature "distance" for a coreference pair as the distance 
 * measured in tokens.
 *
 */
public class FE_distance extends AbstractFeatureExtractor {
	
	/**
	 * Initializes the Feature Extractor and sets its name.
	 */
	public FE_distance() { this.name = "distance"; }
	
	@Override
	public void extractFeatures(CoreferencePair corefPair, MMAXParser mmax) {
		String antec = corefPair.getAnaphor().getWordList().get(0).getXmlId();
		String anaph = corefPair.getAntecedent().getWordList().get(0).getXmlId();
		
		int antecPos = Integer.parseInt(antec.split("_")[1]);
		int anaphPos = Integer.parseInt(anaph.split("_")[1]);
		
		Feature<Integer> feature = new Feature<Integer>("distance");
		feature.setFtype(FeatureType.NUMERIC);
		int distance = Math.abs(anaphPos - antecPos);
		feature.setValue(distance);
		addFeatureToCoref(feature, corefPair);
	}
	
	@Override
	public Attribute getWekaAttribute() { return new Attribute(this.name);	}
}