/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.datastructure
 * class: Word
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

import de.uniheidelberg.cl.swp.testacr.JavaRapRunner;


/**
 * A word stores information about one word in a text (content form, agreement features). Each word 
 * as a unique id which refers to the exact position in the text.
 *
 */
public class Word implements Comparable<Word>{
	
	private String stringForm;
	
	@SuppressWarnings("unused")
	private String pos;	
	
	/**
	 * Sentence position (used mainly for {@link JavaRapRunner}).
	 */
	private int sentence;
	
	/**
	 * Word position (offset) in a sentence (used mainly for {@link JavaRapRunner}).
	 */
	private int wordPositionInSent;
	/**
	 * absolutePosition begins counting at the first char and counts all the single chars up to the 
	 * word.
	 */
	private int absolutePosition;
	
	/**
	 * xmlID is just used if words are already annotated in some kind of XML format and contain an 
	 * id.
	 */
	private String xmlID;	
	
	/**
	 * Initialize a new Word object with the following information:
	 * 
	 * @param stringForm String representation (Token).
	 * @param sentence Sentence position of the word.
	 * @param wordPosition	Word position within the sentence.
	 * @param id Unique id for the word (usually extracted from MMAX-file).
	 */
	public Word(String stringForm, int sentence, int wordPosition, String id) {
		this.stringForm = stringForm;
		this.sentence = sentence;
		this.wordPositionInSent = wordPosition;
		this.xmlID = id;
	}	
	
	/**
	 * This constructor should be used in general.
	 * @param stringForm String form representation.
	 * @param absolutePosition Absolute position in text (counted in characters).
	 */
	public Word(String stringForm, int absolutePosition) {
		this.absolutePosition = absolutePosition;
		this.stringForm = stringForm;
	}
	
	/**
	 * Initialize a new Word object with the following information:
	 *
	 * @param stringForm String representation (token).
	 * @param pos Part of Speech tag.
	 * @param id Unique id for the word.
	 */
	public Word(String stringForm, String pos, String id) {
		this.stringForm = stringForm;
		this.pos = pos;
		this.xmlID = id;
	}
	
	/**
	 * Initialize a new Word object with the following information: 
	 * @param stringForm String representation (token)
	 */
	public Word(String stringForm) { this(stringForm, "", ""); }

	/**
	 * Setter for relative position.
	 * @param sentenceNumber The number of the sentence this word is located in. 
	 * @param wordPosition the relative position of the word within its sentence. 
	 */
	public void setRelativeWordPosition(int sentenceNumber, int wordPosition) {
		this.sentence = sentenceNumber;
		this.wordPositionInSent = wordPosition;
	}
	
	/**
	 * Getter for the XML id of a word. 
	 * @return The XML id assigned to a word in an XML annotated file.
	 */
	public String getXmlId() {	return this.xmlID;	}
	
	/**
	 * Getter for the string representation of a word.
	 * @return String representation of this word.
	 */
	public String getToken() {	return this.stringForm;	}
	
	/**
	 * Getter for the sentence number in which a word occurs.
	 * @return The number of this word's sentence within a text.
	 */
	public int getSentence() { return sentence; }
	
	/**
	 * Getter for the position of a word in a sentence.
	 * @return The relative position of this word within a sentence. 
	 */
	public int getWordPositionInSent() { return wordPositionInSent;	}
	
	/**
	 * Getter for the absolute position of a word.
	 * @return The absolute position of this word counted in character.s
	 */
	public int getAbsolutePosition() { return absolutePosition; }
	
	/**
	 * Setter for the absolute position of a word.
	 * @param absolute the absolute position of this word counted in characters from the beginning 
	 * of the text.
	 */
	public void setAbsolutePosition(int absolute) { this.absolutePosition = absolute; }

	/**
	 * Compares the absolute position of two words.
	 * @return The distance of the two words counted in characters.
	 */
	public int compareTo(Word o) {
		return this.absolutePosition - o.absolutePosition;
	}
}