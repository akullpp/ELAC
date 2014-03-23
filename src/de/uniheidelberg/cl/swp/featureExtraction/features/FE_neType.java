/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction.features
 * class: FE_neType
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

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;

import weka.core.Attribute;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.datastructure.Word;
import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * This feature extractor extracts the feature "NEType" for a coreference pair. Refers to 
 * {@link NEType} for the possible types of named entities. 
 *
 */
public class FE_neType extends AbstractFeatureExtractor{
	
	/**
	 * Initializes the Feature Extractor and sets its name.
	 */
	public FE_neType() { this.name = "neType";	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void extractFeatures(CoreferencePair corefPair, MMAXParser mmax) {
		/* see whether this corefPair denotes an entity. */
		MarkableLevel enamex = mmax.getDiscourse().getMarkableLevelByName("enamex", false);
		String enamexTag = "none";
		Feature<NEType> feature = new Feature<NEType>(this.getName());
		feature.setFtype(FeatureType.NETYPE);

		/* Try to find the NEType for the antecedent. */
		for (Word word : corefPair.getAntecedent().getWordList()) {
			String id = word.getXmlId();
			List<Markable> crpMarkables = enamex.getMarkablesAtDiscourseElementID(id, null);
			
			if (crpMarkables.size() == 0) {	continue; }
			else {
				enamexTag = crpMarkables.get(0).getAttributeValue("tag");
				feature.setValue(NEType.getMatchingNEType(enamexTag));
				addFeatureToCoref(feature, corefPair);
				return;
			}
		}
		
		/* Try to find the NEType for the anaphora. */
		for (Word word : corefPair.getAnaphor().getWordList()) {
			String id = word.getXmlId();
			List<Markable> crpMarkables =  enamex.getMarkablesAtDiscourseElementID(id, null);
			
			if (crpMarkables.size() == 0) {	continue; }
			else {
				enamexTag = crpMarkables.get(0).getAttributeValue("tag");
				feature.setValue(NEType.getMatchingNEType(enamexTag));
				addFeatureToCoref(feature, corefPair);
				return;
			}
		}		
		feature.setValue(NEType.getMatchingNEType(enamexTag));
		addFeatureToCoref(feature, corefPair);	
	}

	@Override
	public Attribute getWekaAttribute() {
		List<String> neType = new ArrayList<String>();
		for(NEType type : NEType.values()) {
			neType.add(type.toString());
		}
		return new Attribute(this.name, neType);
	}
}