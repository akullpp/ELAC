/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.datastructure
 * class: Mention
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


/**
 * Mentions are actually coreferents referring to {@link Entity}s.
 *
 */
public class Mention {	
	
	/**
	 * The words which represents a mention. (One mention can consist of several words)
	 */
	private List<Word> wordList;
	
	/**
	 * Mention type (pronominal vs. nominal).
	 */
	private String type;
	
	/**
	 * Initializes a new Mention.
	 */
	public Mention() { this.wordList = new ArrayList<Word>(); }

	/**
	 * Getter for the list of words.	
	 * @return a List of Words which constitue the mention.
	 */
	public List<Word> getWordList() { return wordList; }
	
	/**
	 * Setter for the wordList.
	 * @param wordList List of Words which constitue the mention.
	 */
	public void setWordList(List<Word> wordList) { this.wordList = wordList; }
	
	/**
	 * Adds a single new word to the wordList
	 * @param word A single new word to add to the wordList.
	 */
	public void addWord(Word word) { this.wordList.add(word); }
	
	/**
	 * Getter for the "type" feature.
	 * @return the type as a String.
	 */
	public String getType() { return type; }
	
	/**
	 * Setter for the "type" of mention.
	 * @param type Pronominal or nominal.
	 */
	public void setType(String type) { this.type = type; }	
}