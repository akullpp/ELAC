/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.io
 * class: Configuration
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
package de.uniheidelberg.cl.swp.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.testacr.Runner;


/**
 * Loads a {@link Properties} object for a specified property file.
 * <br>
 * Implemented as a singleton.
 */
public class Configuration {
	private static Configuration instance = null;
	private final static String defaultTrainDataPath = "./data/training/";
	private Properties properties;
	
	/**
	 * The default path to the configuration file if it isn't explicitly specified.
	 */
	public final static String defaultPropPath = "./config/config.xml";
	
	/**
	 * Constructor, if the path to the configuration file is specified explicitly.
	 * 
	 * @param fileName Path to the manually set config file.
	 */
	private Configuration(String fileName) {
		this.properties = new Properties();

		try {
			FileInputStream fs = new FileInputStream(fileName);
			properties.loadFromXML(fs); }
		catch (IOException e) {
			System.err.println("Couldn't read property file at " + fileName);
			e.printStackTrace();
		}
	}
	
	/**
	 * Returns an instance of {@link Configuration} for the specified file name.
	 * <br>
	 * Singleton implementation to make sure that the logfile isn't opened multiple times.
	 *  
	 * @param configFileName Path to the config file.
	 * @return Instance of {@link Configuration}.
	 */
	public static Configuration getInstance(String configFileName) {
		if (instance == null) {
			instance = new Configuration(configFileName);
		}
		return instance;
	}

	/**
	 * Returns a instance of {@link Configuration} for the default path.
	 * 
	 * @return Instance of {@link Configuration}. 
	 */
	public static Configuration getInstance() {
		return getInstance(defaultPropPath);
	}
	
	/**
	 * Getter for the embedded {@link Properties} object.
	 * 
	 * @return Embedded {@link Properties} object.
	 */
	public Properties getProperties() {
		return properties;
	}
	
	/**
	 * Returns the path to the training or test data.
	 * 
	 * @param directory Specification of the directory, either Training or Test.
	 * @return String containing the path to the training files.
	 */
	public String getDirectory(String directory) {
		return this.properties.getProperty("PathTo" + directory + "Files", defaultTrainDataPath);
	}
	
	/**
	 * Get a value for a feature selector.
	 * 
	 * @param featureName Name of the feature.
	 * @return Value of the feature as String.
	 */
	public String getFeatureSelector(String featureName) {
		return this.properties.getProperty(featureName);
	}
		
	/**
	 * Loads the list of ACR-Systems which should be tested and returns this list as a list of
	 * {@link Runner}-Classes.
	 * 
	 * @return List of {@link Runner} classes, ready to be executed.
	 */
	public List<Runner> getAcRRunner() {
		List<Runner> acrSystems = new ArrayList<Runner>();
		
		/* get an array of Runner classes defined by the user */
		String[] userValues = new String[properties.getProperty("Runner").split(";").length];
		userValues = properties.getProperty("Runner").split(";");
		
		/* create a instance of each runner and add it to ACR-Systems */
		for (int i = 0; i < userValues.length; i++) {
			String currentSystem = userValues[i];
			
			if (currentSystem == null)
				continue;
			
			try {
				/* load the runner */
				String result = "de.uniheidelberg.cl.swp.testacr." + currentSystem;
				
				/* create a new instance and add it to the runner list */
				acrSystems.add((Runner) Class.forName(result).newInstance());
			} catch (Exception e) {	
				System.err.println("Couldn't load acr system runner " + currentSystem);
				e.printStackTrace();} 
		}
		System.out.println(System.nanoTime() +  " \t Loaded " + acrSystems.size() +
				" ACR systems from config file");
		return acrSystems;
	}
	
	/**
	 * Gets the multiple classifiers for stacking.
	 * 
	 * @return Array of classifiers in string format.
	 */
	public String[] getStackingClassifiers() {
		String[] classifierList =
			new String[properties.getProperty("subclassifier").split(";").length];
		classifierList = properties.getProperty("subclassifier").split(";");
		
		return classifierList;
	}
	
	/**
	 * Returns a list of FeatureExtractors containing the features which will be used to sort out
	 * irrelevant {@link CoreferencePair}s.
	 * 
	 * @return List of Strings representing the features by which the {@link CoreferencePair}s are
	 * 		   filtered.
	 */
	public List<String> getFeatureFilter() {
		/* return null, if no feature filters have been specified */
		if (properties.getProperty("FeatureFilter") == null) {
			return null;
		}

		/*  get an array of feature filters defined by the user */
		String[] userValues = new String[properties.getProperty("FeatureFilter").split(";").length];
		userValues = properties.getProperty("FeatureFilter").split(";");
		
		return Arrays.asList(userValues);	
	}
}
