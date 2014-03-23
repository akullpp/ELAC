/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction.features
 * class: POSTag
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

/**
 * An enumeration for all the useful PosTags. Each postag consists of a description and a string 
 * name (used to mach pos-information in string content).
 *
 */
public enum POSTag {
	
	/**
	 * Personal Pronoun.
	 */
	PRP ("Personal pronoun", "prp"),
	
	/**
	 * Possessive Pronoun.
	 */
	PRP$ ("Possessive pronoun", "prp$"),
	
	/**
	 * Proper Noun singular.
	 */
	NNP ("Proper Noun", "nnp"),
	
	/**
	 * Noun, singular or mass.
	 */
	NN ("Noun singular", "nn"),
	
	/**
	 * Noun, plural.
	 */
	NNS ("Noun plural", "nns"),
	
	/**
	 * Determiner.
	 */
	DT ("Determiner", "dt"),
	
	/**
	 * Everything else.
	 */
	ELSE ("Else", "else");
	
	private final String description;
	private final String stringName;
	
	/**
	 * Generates a new POS-Tag.
	 * 
	 * @param description Description of the POS-Tag.
	 * @param stringName String representation of the POS-Tag.
	 */
	private POSTag(String description, String stringName) {
		this.description = description;
		this.stringName = stringName;
	}
	
	/**
	 * Gets the POS-Tag object for the string representation.
	 * 
	 * @param pos String representation of the POS-Tag.
	 * @return POS-Tag object.
	 */
	public static POSTag getMatchingPosType(String pos) {
		for (POSTag postag : POSTag.values()) {
			if (pos.equals(postag.stringName))
				return postag;
		}
		return ELSE;
	}

	/**
	 * Getter for the POS-Tag description.
	 * 
	 * @return POS-Tag description.
	 */
	public String getDescription() { return description; }
	
	/**
	 * Getter for the String representation of an POS-Tag object.
	 * 
	 * @return String representation.
	 */
	public String getStringName() {	return stringName;	}
	
	@Override
	public String toString() {	return this.stringName;	}	
}
