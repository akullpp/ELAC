/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.testacr
 * class: BARTRunner
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
 * This class extracts the {@link CoreferencePair}s extracted by BART.<br>
 * Note that BART has to be executed in the Preprocessing step as this class
 * relies on the existence of the response level created by BART.
 *  
 */
public class BARTRunner extends Runner {
	private List<Entity> coreferents;

	/**
	 * Empty stub to work with the other ACRs.
	 */
	public BARTRunner() {}

	/**
	 * Sets the MMAXParser for BART.
	 */
	public void init(MMAXParser mmax) { 
		this.mmax = mmax;
		if (coreferents!=null)  {
			this.coreferents = null;
		}
	}
	
	/**
	 * Empty stub to work with the other ACRs.
	 * Note that results from BART already have to exist.
	 */
	public void run() {}
	
	/**
	 * Extracts the coreferences from the response level.
	 */
	private void createCoreferents() {
		mmax.extractCorefs("response");
		coreferents = mmax.getEntityList();
	}
	
	/**
	 * Getter for coreferents.
	 * If not available, they'll be extracted first.
	 */
	public List<Entity> getCoreferents() {
		if (coreferents == null) { createCoreferents(); }
		return coreferents;
	}
}
