/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.util
 * class: MMAXParser
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
package de.uniheidelberg.cl.swp.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eml.MMAX2.annotation.markables.Markable;
import org.eml.MMAX2.annotation.markables.MarkableLevel;
import org.eml.MMAX2.discourse.MMAX2Discourse;
import org.eml.MMAX2.discourse.MMAX2DiscourseElement;
import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Entity;
import de.uniheidelberg.cl.swp.datastructure.Mention;
import de.uniheidelberg.cl.swp.datastructure.Word;
import de.uniheidelberg.cl.swp.io.Logging;


/**
 * This class handles MMAX annotated files and provides methods to extract the token and annotated
 * information associated with the MMAX-file.
 */
public class MMAXParser {
	/**
	 * The filename for the MMAX-file. 
	 */
	private String fileName;

	private MMAX2Discourse discourse;
	
	/**
	 * Contains all the words combined with their unique ID.
	 */
	private Map<String, Word> wordMap;
	
	private List<Word> wordList;
	
	/**
	 * List of entities extracted from coref/response level.
	 */
	private List<Entity> entityList;
	
	/**
	 * List of {@link CoreferencePair}s extracted from the corpus.
	 */
	private List<CoreferencePair> corefPairs;
	
	/**
	 * Initializes the necessary data structures.
	 *
	 * @param fileName Path to an MMAX-file.
	 */
	public MMAXParser(String fileName) {
		this.fileName = fileName;
		this.discourse  = MMAX2Discourse.buildDiscourse(this.fileName);
		this.wordMap = new HashMap<String,Word>();
		this.entityList = new ArrayList<Entity>();
		this.wordList = new ArrayList<Word>();
	}
	
	/**
	 * Getter for the MMAX-Discourse object.
	 * 
	 * @return MMAX-Discourse object.
	 */
	public MMAX2Discourse getDiscourse() {
		return this.discourse;
	}
	
	/**
	 * Getter for wordMap.
	 * 
	 * @return Mapping between ID and Word object.
	 */
	public Map<String, Word> getWords() {
		return this.wordMap;
	}
	
	/**
	 * Getter for entityList
	 * @return the list of {@link Entity}s for the current file.
	 */
	public List<Entity> getEntityList() {
		return this.entityList;
	}
	
	/**
	 * Returns a sorted list of words.
	 * 
	 * @return Sorted list of words from basedata.
	 */
	public List<Word> getWordList() {
		return this.wordList;
	}
	
	/**
	 * Getter for {@link CoreferencePair}s.
	 * 
	 * @return List of {@link CoreferencePair}s extracted from the corpus.
	 */
	public List<CoreferencePair> getCorefPairs() {
		return corefPairs;
	}
	
	/**
	 * Extracts all the words with their ID and positions from basedata and stores them in wordMap
	 * together with their ID as key.
	 */
	private void extractToken() {
		/* words in the base-data file. */
		MMAX2DiscourseElement[] discourseElements = discourse.getDiscourseElements();
		
		/* 
		 * counter to remember the current position within the document used to determine absoulte
		 * word positions within the doc.
		 */
		int absolutePosition = 0;
		
		/* try to roughly specify word and sentence position by regex sentence-splitting */
		for (int i = 0; i < discourseElements.length; i++) {
			/* remove brackets and - */
			String token = discourseElements[i].toString().replaceAll("[\\[\\]\\(\\)\\-\\_\"]",
					"");

			if (token.matches("\\p{Punct}")) {
				absolutePosition -= 1;
			}
			String id = discourseElements[i].getID().toString();

			/* create a new Word object with the relative positions */
			Word curWord = new Word(token, "", id);
			
			/* add absolute start positions within the document */
			curWord.setAbsolutePosition(absolutePosition);
			
			/* put the word's id and the corresponding object into the wordMap */
			this.wordMap.put(id, curWord);
			
			wordList.add(wordMap.get(id));
			absolutePosition += token.length() + 1;			
		}
		Collections.sort(wordList);
	}
	
	/**
	 * Extracts the coreferences from MMAX annotation and maps them to the words already extracted.
	 * 
	 * @param annotationLevel Level which is used for extraction.
	 * @return List of {@link CoreferencePair}s which have been extracted.
	 */
	@SuppressWarnings("unchecked")
	public List<CoreferencePair> extractCorefs(String annotationLevel) {
		Map<String, Entity> entityMap = new HashMap<String, Entity>();
		this.entityList.clear();
		
		/* Check whether words have already been extracted */
		if (this.wordMap.size() < 1) {
			this.extractToken();
		}
		
		/* Access the annotation level */
		MarkableLevel corefLevel = discourse.getMarkableLevelByName(annotationLevel,false);

		/* Access Markables */ 
		ArrayList<Markable> mList = corefLevel.getMarkables();

		/* extract all coref-sets */
		for (Markable corefMarkable : mList) {
			String corefSet = corefMarkable.getAttributeValue("coref_set");
			
			if (! entityMap.containsKey(corefSet)) {
				entityMap.put(corefSet, new Entity(corefSet));
			}
			/* append the current markable as a mention to the entity */
			Mention newMention = new Mention();
			
			newMention.setType(corefMarkable.getAttributeValue("mtype"));
			String[] wordIDs = corefMarkable.getDiscourseElementIDs();
			List<Word> mentionWords = new ArrayList<Word>();
			
			for (int i = 0; i < wordIDs.length; i++) {
				mentionWords.add(this.wordMap.get(wordIDs[i]));
			}
			
			/* add all the words to the mention */
			newMention.setWordList(mentionWords);
			
			/* add mentions to the entity */
			entityMap.get(corefSet).addMention(newMention);
		}
		this.entityList.addAll(entityMap.values());

		/* If we parse an MMAX file (gold standard), split the chains into pairs. This must not
		 * be done for BART. */
		if (annotationLevel.equals("coref"))
				this.corefPairs = splitCorefChainsToPairs(entityList);
		
		return corefPairs;
	}
	
	/**
	 * Splits up a list of {@link Entity}s and creates a list of {@link CoreferencePair}s out of
	 * it.
	 * 
	 * @param entitiyList List of {@link Entity}s.
	 * @return List of {@link CoreferencePair}s generated from the list of {@link Entity}s.
	 */
	public static List<CoreferencePair> splitCorefChainsToPairs(List<Entity> entitiyList) {
		List<CoreferencePair> corefPairs = new ArrayList<CoreferencePair>();
		
		for (Entity entity : entitiyList) {
			for (int i=0; i<entity.getMentions().size() -1 ; i++) {
				
				/* create a coreferencepair for two neighboring mentions */
				for (int z = i + 1; z <= entity.getMentions().size() - 1; z++) {
					CoreferencePair crp = new CoreferencePair(entity.getMentions().get(i), 
															  entity.getMentions().get(z));
	
					if (z == i + 1) crp.setDirectNeighbor(true);
					else crp.setDirectNeighbor(false);

					corefPairs.add(crp);
				}
			}
		}
		return corefPairs;
	}
	
	/**
	 * Loads a new MMAXParser and extracts the token and correct {@link CoreferencePair}s from it.
	 * 
	 * @param mmaxFileName MMAX file to be loaded.
	 * @return Number of raw words in the text.
	 */
	public static  MMAXParser processMMAXFile(String mmaxFileName) {
		MMAXParser mmaxParser = new MMAXParser(mmaxFileName);
		Logging.getInstance().getLogger().info("Extracting raw words from " + mmaxFileName);
		mmaxParser.extractToken();
		mmaxParser.extractCorefs("coref");
		
		return mmaxParser;
	}

	/**
	 * Returns the plaintext as a concatenated string which handles punctuation correctly.
	 * 
	 * @return Concatenated string containing all the text.
	 */
	public String getPlainTextAsString() {
		String textString = "";
		
		for (Word w : this.getWordList()) {
			if (w.getToken().matches("\\p{Punct}"))
				textString = textString + w.getToken();
			else
				textString = textString + " " + w.getToken();
		}
		textString = textString.replaceAll("[\\(\\)]", "");
		
		return textString;
	}
}
