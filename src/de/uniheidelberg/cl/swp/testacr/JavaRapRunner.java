/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.testacr
 * class: JavaRapRunner
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;


import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Entity;
import de.uniheidelberg.cl.swp.datastructure.Mention;
import de.uniheidelberg.cl.swp.datastructure.Word;
import de.uniheidelberg.cl.swp.preproc.JavaRapPreProcessing;
import de.uniheidelberg.cl.swp.util.MMAXParser;



/**
 * This class parses JavaRap results gathered during preprocessing and converts the 
 * results into {@link CoreferencePair}s. 
 * <br>
 * Note that the JavaRap results are expected under ./Basedata/javarap for the 
 * current {@link MMAXParser}. This is achieved through {@link JavaRapPreProcessing}.
 * 
 *
 */
public class JavaRapRunner extends Runner {
	
	/**
	 * MMAx Source file.
	 */
	private MMAXParser mmax;
	
	/**
	 * List of coreferences predicted by JavaRap.
	 */
	private List<Entity> coreferents;
	
	
	/**
	 * Default constructor, initializes coreferents list.
	 */
	public JavaRapRunner() {this.coreferents = new ArrayList<Entity>();}
	
	
	@Override
	public List<Entity> getCoreferents() {return this.coreferents;}

	
	/**
	 * Parse the results file which has been created by {@link JavaRapPreProcessing}.
	 * <br>The results file is expected unter ./Basedata/javarap for the current MMAX corpus.<br>
	 * The results are converted into {@link CoreferencePair}s. 
	 * 
	 */
	public void run() throws Exception {
		this.coreferents.clear();
		String path = this.mmax.getDiscourse().getCommonBasedataPath(); 
		File jrPath = new File(path + "/javarap/");
		File wordFile = new File(mmax.getDiscourse().getWordFileName());
		
		try {
			Reader r = new FileReader(new File(jrPath.getCanonicalPath() + "/" +
					wordFile.getName()));
			BufferedReader br = new BufferedReader(r);
			
		
			while (true)
			{
				String line = br.readLine();
				if (line == null) break;
	
				
				Entity en = new Entity("null");
				Mention m1 = new Mention();
				m1.addWord(new Word("",0,0,line.split("~")[0]));
				en.addMention(m1);
				
				Mention m2 = new Mention();
				m2.addWord(new Word("",0,0,line.split("~")[1]));
				en.addMention(m2);
				
				this.coreferents.add(en);
			}
		} catch (Exception e) {
			System.err.println("\nCouldn't find files. Has JavaRap already been executed?\n");
			throw e;
		}
		
	}

	@Override
	public void init(MMAXParser mmax) {this.mmax = mmax;}
}

