/* * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems * package: de.uniheidelberg.cl.swp.datastructure * class: Feature *  * Authors: E-Mail * Thomas Boegel: boegel@cl.uni-heidelberg.de * Lukas Funk: funk@cl.uni-heidelberg.de * Andreas Kull: kull@cl.uni-heidelberg.de *  * Please find a detailed explanation of this particular class/package and its role and usage at * the first JavaDoc following this comment. *  * Copyright 2010 Thomas Boegel & Lukas Funk & Andreas Kull * * Licensed under the Apache License, Version 2.0 (the "License"); * you may not use this file except in compliance with the License. * You may obtain a copy of the License at * * http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, software * distributed under the License is distributed on an "AS IS" BASIS, * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. * See the License for the specific language governing permissions and * limitations under the License. */package de.uniheidelberg.cl.swp.datastructure;import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;import de.uniheidelberg.cl.swp.featureExtraction.features.FE_distance;import de.uniheidelberg.cl.swp.featureExtraction.features.FeatureType;/** * A generic feature which is extracet by {@link AbstractFeatureExtractor}. * The implementing feature extractor (such as {@link FE_distance}) knows the  * generic type of the feature and deals with it appropriately. * * @param <e> The type of a feature (e.g. Boolean, Integer, String, ...). */public class Feature<e> {	private String name;		private FeatureType ftype;		private e value;		/**	 * A string representation of the actual {@link Feature} value.	 */	@SuppressWarnings("unused")	private String stringValue;		/**	 * Create a new Feature.	 * 	 * @param name Name of the feature (e.g. "distance").	 */	public Feature(String name) { this.name = name;	}		/**	 * Set the actual feature value.	 * 	 * @param value The feature value (of type e).	 */	public void setValue(e value) {		this.value = value;		setStringValue(value.toString());	}		/**	 * Getter for the feature value.	 * 	 * @return the actual value for this feature.	 */	public e getValue() { return this.value; }		/**	 * Getter for the string representation of the value.	 * 	 * @return A string representation of the value.	 */	public String getStringValue() { return this.value.toString();	}		/**	 * Setter for the string representation of the value.	 * 	 * @param stringValue The String representation.	 */	public void setStringValue(String stringValue) { this.stringValue = stringValue; }	/**	 * Getter for the feature name.	 * @return The short name of the feature.	 */	public String getName() { return name;	}		/**	 * Setter for the {@link FeatureType}.	 * @param ftype The corresponding {@link FeatureType}.	 */	public void setFtype(FeatureType ftype) { this.ftype = ftype; }		/**	 * Getter for the {@link FeatureType}.	 * @return The corresponding {@link FeatureType}.	 */	public FeatureType getFtype() {	return ftype; }	}