/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.mlprocess
 * class: InstanceContainer
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
package de.uniheidelberg.cl.swp.mlprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Feature;
import de.uniheidelberg.cl.swp.featureExtraction.AbstractFeatureExtractor;
import de.uniheidelberg.cl.swp.featureExtraction.features.FeatureType;
import de.uniheidelberg.cl.swp.testacr.BARTRunner;
import de.uniheidelberg.cl.swp.testacr.JavaRapRunner;
import de.uniheidelberg.cl.swp.testacr.LingpipeRunner;


/**
 * A container for WEKA Instances utilized by {@link de.uniheidelberg.cl.swp.mlprocess.MLProcess}.
 * <br>
 * This is a temporary container for providing the attribute structure which WEKA requests.
 */
public class InstanceContainer {
	private Instances instances;

	/**
	 * Takes a list of {@link AbstractFeatureExtractor} and adds the corresponding feature as a
	 * WEKA Attribute to the Instances structure.
	 *  
	 * @param featureList The list of FeatureExtractors to be added.
	 */
	public void createAttributeStructure(List<AbstractFeatureExtractor> featureList) {
		if (this.instances != null) {
			return;
		}
		ArrayList<Attribute> wekaAttributes = new ArrayList<Attribute>(featureList.size());

		for (AbstractFeatureExtractor fe : featureList) {
			wekaAttributes.add(fe.getWekaAttribute());
		}

		/* Add the ACR-Systems as feature */
		List<String> acrFeature = new ArrayList<String>();

		acrFeature.add(JavaRapRunner.class.getCanonicalName());
		acrFeature.add(LingpipeRunner.class.getCanonicalName());
		acrFeature.add(BARTRunner.class.getCanonicalName());

		List<String> correctFalsePrediction = new ArrayList<String>();

		/* Add the possible prediction values */
		correctFalsePrediction.add("+");
		correctFalsePrediction.add("-");
		correctFalsePrediction.add("?");

		wekaAttributes.add(new Attribute("acrSystem", acrFeature));
		wekaAttributes.add(new Attribute("corretOutputBySystem", correctFalsePrediction));

		this.instances = new Instances("ACResolution", wekaAttributes, 0);
		this.instances.setClassIndex(this.instances.numAttributes() - 1);
	}

	/**
	 * Takes the name of a feature and returns the position of the corresponding WEKA Attribute
	 * object for the feature.
	 * <br>
	 * This has to be done to maintain the correlation between feature and attribute.
	 * 
	 * @param name Name of the feature.
	 * @return Position of the corresponding attribute.
	 */
	private int getAttributeByName(String name) {
		for (int i = 0; i < this.instances.numAttributes(); i++) {
			if (this.instances.attribute(i).name().equals(name))
				return i; 
		}
		return 0;
	}

	/**
	 * Takes a single {@link CoreferencePair} and adds it to the Instances.
	 * <br>
	 * This method is used in the training process and + or - will be added.
	 * <br>
	 * This is done by matching all the Attributes to the corresponding features of the
	 * {@link CoreferencePair}.
	 * 
	 * @param corefPair {@link CoreferencePair} to be added.
	 * @param acr ACR-System.
	 * @param result Result of the ACR-System for the {@link CoreferencePair}-candidate.
	 * @return Instance object which has been added.
	 */
	public Instance addCorefInstance(CoreferencePair corefPair, String acr, String result)  {
		double[] vals = new double[this.instances.numAttributes()];

		for (Feature<?> fe : corefPair.getFeatuerVector()) {
			int currentCorefAttribute = getAttributeByName(fe.getName());

			/* if the current feature is numeric, parse the value as integer */
			if (fe.getFtype() == FeatureType.NUMERIC) {
				vals[currentCorefAttribute] = Double.parseDouble(fe.getStringValue());
			}
			else { vals[currentCorefAttribute] = 
				this.instances.attribute(currentCorefAttribute).indexOfValue(fe.getStringValue());
			}
		}
		/* (vals.length-2) contains the acrRunner which made the prediction */
		vals[vals.length-2] = this.instances.attribute(vals.length-2).indexOfValue(acr);

		/* (vals.length-1) describes whether the system made the correct prediction */
		vals[vals.length-1] = this.instances.attribute(vals.length-1).indexOfValue(result);

		Instance inst = new DenseInstance(1.0, vals);
		this.instances.add(inst);
		return inst;
	}

	
	/**
	 * Takes a single {@link CoreferencePair} and adds it to the Instances.
	 * <br>
	 * This method is used in the test process and "?" will be added automatically.
	 * <br>
	 * This is done by matching all the Attributes to the corresponding features of the
	 * {@link CoreferencePair}.
	 * 
	 * @param corefPair {@link CoreferencePair} to be added.
	 * @param acr ACR-System.
	 * @return Instance object which has been added.
	 */
	public Instance addCorefInstance(CoreferencePair corefPair, String acr)  {
		double[] vals = new double[this.instances.numAttributes()];

		for (Feature<?> fe : corefPair.getFeatuerVector()) {
			int currentCorefAttribute = getAttributeByName(fe.getName());
			
			if (fe.getFtype() == FeatureType.NUMERIC) {
				vals[currentCorefAttribute] = Double.parseDouble(fe.getStringValue());
			}
			else { vals[currentCorefAttribute] = 
				this.instances.attribute(currentCorefAttribute).indexOfValue(fe.getStringValue());
			}
		}
		vals[vals.length-2] = this.instances.attribute(vals.length-2).indexOfValue(acr);
		vals[vals.length-1] = this.instances.attribute(vals.length-1).indexOfValue("?");

		Instance inst = new DenseInstance(1.0, vals);

		this.instances.add(inst);

		return inst;
	}

	/**
	 * Adds a List of {@link CoreferencePair}s to the list of Instances.
	 * 
	 * @param corefPairs {@link CoreferencePair}s to be added. 
	 * @param acr Related ACR-System.
	 * @param result Result of the related ACR-System.
	 * @return Mapping between {@link CoreferencePair}s and their corresponding Instance objects.
	 */
	public Map<CoreferencePair,Instance> addCorefInstances(List<CoreferencePair> corefPairs,
			String acr, String result)  {
		Map<CoreferencePair,Instance> crpInstAlignment = new HashMap<CoreferencePair,Instance>();

		for (CoreferencePair corefPair : corefPairs) {
			Instance inst = this.addCorefInstance(corefPair, acr, result);
			crpInstAlignment.put(corefPair, inst);
		}
		return crpInstAlignment;
	}		

	/**
	 * Getter for the Instances object which contains the stored Instance objects.
	 * 
	 * @return The Instance objects stored in the Instances Container.
	 */
	public Instances getInstances() {
		return instances;
	}
}
