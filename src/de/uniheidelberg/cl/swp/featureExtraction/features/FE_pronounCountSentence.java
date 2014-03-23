/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction.features
 * class: FE_pronounCountSentence
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

import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import weka.core.Attribute;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * This Feature Extractor extracts the number of pronouns occurring 
 * in the same sentence as the anaphora using the POS tags "prp"
 * and "prp$". 
 *
 */
public class FE_pronounCountSentence extends AbstractFeatureExtractor {

	/**
	 * Initializes the Feature Extractor and sets its name. 
	 */
	public FE_pronounCountSentence() { this.name = "pronounCountSentence"; }
	
	@Override
	public void extractFeatures(CoreferencePair corefPair, MMAXParser mmax) {
		int prpCount = 0;
		
		String id = corefPair.getAnaphor().getWordList().get(0).getXmlId();
		
		/* Acquire the sentence attribute for the current discourse entity and its word's IDs. */
		MarkableLevel sentenceLevel =
			mmax.getDiscourse().getMarkableLevelByName("sentence", false);		
		Markable sentenceMarkable =
			(Markable) sentenceLevel.getMarkablesAtDiscourseElementID(id, null).get(0);
		
		String[] sentenceSpan = sentenceMarkable.getDiscourseElementIDs();
		
		/* check extracted word numbers' POS tags, increment counter if pronoun. */
		MarkableLevel posLevel = mmax.getDiscourse().getMarkableLevelByName("pos", false);
		
		for (String word : sentenceSpan){
			Markable posMarkable =
				(Markable) posLevel.getMarkablesAtDiscourseElementID(word, null).get(0);
			String pos = posMarkable.getAttributeValue("tag");
			if (pos.equals("prp")){	prpCount += 1; }
			else if (pos.equals("prp$")){ prpCount += 1; }
			else{ continue;	}
		}
		
		Feature<Integer> feature = new Feature<Integer>(this.name);
		feature.setFtype(FeatureType.NUMERIC);
		feature.setValue(prpCount);
		addFeatureToCoref(feature, corefPair); 		
	}
	
	@Override
	public Attribute getWekaAttribute() {
		Attribute pronCount = new Attribute(this.name);
		return pronCount;
	}	
}