/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.datastructure
 * class: CoreferencePair
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
package de.uniheidelberg.cl.swp.datastructure;

import java.util.ArrayList;
import java.util.List;
import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * A Coreference Pair is a pair of two {@link Mention}s which is 
 * used to collect feature values and to be able to evaluate 
 * ACR output. There's no magic involved (well, not yet): the mentions of an entity are 
 * just split in pairs of two to get the corefPairs. 
 * 
 * TODO: Make this object more generic to be able to refer also to 
 * relations between {@link Entity} and {@link Mention}.
 *
 */
public class CoreferencePair {

	/**
	 * Each CoreferencePair is uniquely assigned to one MMAX-file, so that we can get the features 
	 * for the pairs at each time.
	 */
	private MMAXParser mmax;
	
	private Mention anaphor;
	private Mention antecedent;
	
	/**
	 * Determines whether the {@link CoreferencePair} consists of two mentions being direct 
	 * neighbors (instead of transitive neighbors)
	 */
	private boolean directNeighbor;
	
	/**
	 * The ACR system which predicted this {@link CoreferencePair}.
	 */
	private String acrSystem;	
	
	/**
	 * A list containing all the features which have been extracted. The values are acquired 
	 * through FeatureExtraction.
	 */
	private List<Feature<?>> featuerVector;	
		
	/**
	 * Initialize a CoreferencePair, consisting of two Mentions.
	 * @param anaph The anaphora mention
	 * @param ante The antecedent mention
	 */
	public CoreferencePair(Mention ante, Mention anaph) {
		this.anaphor = anaph;
		this.antecedent = ante;
		this.featuerVector = new ArrayList<Feature<?>>();
	}
	
	/**
	 * Getter for the anaphora Mention.
	 * @return anaphor: anaphora Mention
	 */
	public Mention getAnaphor() { return anaphor; }
	
	/**
	 * Add a new feature extractor (equals feature value) for this
	 * coreference.
	 * @param fe The featureExtractor to be added (of type {@link AbstractFeatureExtractor}.
	 */
	public void addFeature(Feature<?> fe) {	this.featuerVector.add(fe);	}
	
	/**
	 * Returns the feature vector consisting of several {@link AbstractFeatureExtractor}s.
	 * @return The feature vector for this coreference pair.
	 */
	public List<Feature<?>> getFeatuerVector() { return featuerVector; }
	
	/**
	 * Getter for the antecedent Mention.
	 * @return antecedent: antecedent Mention
	 */
	public Mention getAntecedent() { return antecedent;	}
	
	/**
	 * Setter for the {@link MMAXParser}.
	 */
	public void setMmax(MMAXParser mmax) { this.mmax = mmax; }
	
	/**
	 * Getter for the {@link MMAXParser}.
	 */
	public MMAXParser getMmax() { return mmax; }
	
	/**
	 * A method to return a human readable representation of the anaphora and antecedent along with
	 * the count of their mentions. 
	 * @return Count and string representation of the anaphora and antecedent.
	 */
	public String toString() {
		String output = "Anaphor " + anaphor.getWordList().size() + " words, first one: " +
						anaphor.getWordList().get(0).getXmlId();
		output += " Antecedent " + antecedent.getWordList().size() + " words, first one: " +
						antecedent.getWordList().get(0).getXmlId();
		return output;
	}
	
	/**
	 * A quick check if a feature in the feature vector of another coreference pair also appears
	 * in this coreference pair. 
	 * @param o Another coreference pair.
	 * @return true if matching features are found.
	 */
	public boolean equals(Object o) {
		CoreferencePair crp = (CoreferencePair) o;
		
		Word anaph1 = this.getAnaphor().getWordList().get(0);
		Word anaph2 = crp.getAnaphor().getWordList().get(0);
		
		Word antec1 = this.getAntecedent().getWordList().get(0);
		Word antec2 = crp.getAntecedent().getWordList().get(0);
		
		if (anaph1.getXmlId().equals(anaph2.getXmlId()) &&
			antec1.getXmlId().equals(antec2.getXmlId())) 
			return true;
				
		return false;
	}
	
	
	/**
	 * Setter for the ACR system to use.
	 * @param acrSystem The acrSystem to use.
	 */
	public void setAcrSystem(String acrSystem) { this.acrSystem = acrSystem; }

	/**
	 * Getter for the ACR system used.
	 * @return The acrSystem used.
	 */
	public String getAcrSystem() {	return acrSystem; }

	/**
	 * Setter to mark a coreference pair as consisting of direct neighbors. 
	 * @param directNeighbor Boolean which tells if the coreference pair consists of direct 
	 * 			neighbors.
	 */
	public void setDirectNeighbor(boolean directNeighbor) {	this.directNeighbor = directNeighbor; }

	/**
	 * Getter to check whether the coreference pair consists of direct neighbors.
	 * @return A boolean that tells if the coreference pair consists of direct neighbors.
	 */
	public boolean isDirectNeighbor() {	return directNeighbor; }	
}