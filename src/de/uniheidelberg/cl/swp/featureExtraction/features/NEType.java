/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.featureExtraction.features
 * class: NEType
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
 * Contains types of Named Entities.
 */
public enum NEType {
	/**
	 * Organization.
	 */
	ORGANIZATION ("organization"),
	
	/**
	 * Location.
	 */
	LOCATION ("location"),
	
	/**
	 * Person.
	 */
	PERSON ("person"),
	
	/**
	 * Unknown.
	 */
	NONE ("none"),
	
	/**
	 * Undefined.
	 */
	OTHER ("other");
	
	/**
	 * A string representation of the named entity type used for comparison etc.
	 */
	private final String stringName;

	/**
	 * Initializes and converts a string to a NEType.
	 * 
	 * @param neType
	 */
	private NEType(String neType) {	this.stringName = neType; }
	
	/**
	 * Gets a Named Entity.
	 * 
	 * @param newType String representation of the NEType.
	 * @return Named Entity type.
	 */
	public static NEType getMatchingNEType(String newType) {
		for (NEType type : NEType.values()) {
			if (newType.equals(type.stringName))
				return type;
		}
		return OTHER;
	}
	
	@Override
	public String toString() { return this.stringName; }
}