/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.testacr
 * class: Runner
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
package de.uniheidelberg.cl.swp.testacr;

import java.util.List;

import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Entity;
import de.uniheidelberg.cl.swp.util.MMAXParser;


/**
 * <p>Blueprint for classes which run an acr system.</p>
 * If you want to add your own acr system, you have to implement this 
 * interface.
 *
 */
public abstract class Runner {
	
	MMAXParser mmax;
	
	/**
	 * Default constructor without any parameters.
	 * Used to initialize the system itself. 
	 */
	public Runner(){};
	
	/**
	 * Init the acr system on the specified {@link MMAXParser} file.
	 * This method could be used to check for special annotation levels in the MMAX file.
	 * If you don't need it, just store the fileName for the run-method.
	 * 
	 * @param mmax The input file for the system.
	 */
	public abstract void init(MMAXParser mmax);
	
	/**
	 * Run the acr system for the specified MMAX-Parser and process the extracted 
	 * Coreferences to transform them to {@link CoreferencePair}s which are used 
	 * by our system.
	 * 
	 * @throws Exception If anything goes wrong during the process, the runner should throw 
	 * 			an Exception. 
	 */
	public abstract void run() throws Exception;
	
	/**
	 * Get the actual results from the system as {@link CoreferencePair}s.
	 * @return List of {@link CoreferencePair}s.
	 */
	public abstract List<Entity> getCoreferents();
	
}
