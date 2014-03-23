/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.preproc
 * class: SalsaParser
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
package de.uniheidelberg.cl.swp.preproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import de.uniheidelberg.cl.swp.datastructure.Word;
import de.uniheidelberg.cl.swp.io.Configuration;


/**
 * <p>This class provides a conversion from a Salsa/Tiger annotated file to an inline annotated 
 * file.<br> 
 * At the moment, only coreference chains are converted, all other annotations will not be 
 * available in the interim file.</p>  
 * 
 */
public class SalsaParser extends DOMParser {
	private Map<String, Word> contentWords = new HashMap<String, Word>();
	private Map<String, String> wordCorefSetAlignment = new HashMap<String, String>();
	private List<String> wordsList = new ArrayList<String>();
	private Map<String, List<Word>> nonTerminals = new HashMap<String, List<Word>>();
	private String output;
	
	/**
	 * Thrown if there's a problem with the format of the specified file.
	 */
	private class FileFormatException extends Exception {
		private static final long serialVersionUID = 1L;

		public FileFormatException(String message) {super(message);}
	}
	
	/**
	 * 
	 */
	public SalsaParser(String salsaXML, String outputFile) {
		try {
			parseXmlFile(salsaXML);
			
			if (doc == null) {
				throw new IOException();
			}
			
			extractCoreferences();
			
			this.output =
				Configuration.getInstance().getProperties().getProperty("ResultOutputDir") +
				outputFile;

			writeInlineFormatFile(output);
		} catch (IOException e) {
			System.out.println("Converted file couldn't be read or written!");
		} catch (FileFormatException e) {
			System.out.println("Coreferences couldn't be extracted!");
		}		
	}
	
	/**
	 * Extracts the content words stored in the &lt;t&gt;-tags and makes them
	 * accessible through a Mapping.
	 * 
	 * @throws FileFormatException If no words have been found.
	 */
	private void extractContentWords() throws FileFormatException {
		//get the root elememt
		Element root = doc.getDocumentElement();
		
		//get a nodelist of <t> elements
		NodeList nl = root.getElementsByTagName("t");
		
		if(nl != null && nl.getLength() > 0) {
			
			//for each <t> element
			for(int i = 0 ; i < nl.getLength(); i++) {
				
				//get the <t> element
				Element el = (Element)nl.item(i);
				
				//extract all the relevant information from <t>
				Word w = extractWordInformation(el);
				
				if (w.getToken().matches("\"")) continue;
				
				//add it to the wordMap
				contentWords.put(w.getXmlId(), w);
				this.wordsList.add(w.getXmlId());		
			}
		}
		
		if (this.wordsList.size() == 0) throw new FileFormatException("No words found!"); 
		
	}
	
	/**
	 * Extracts non terminals which are used to recognize 
	 * coreferences between phrases.
	 */
	private void extractNonTerminals() {
		// aligns nonTerminals to a list of words
		Element root = doc.getDocumentElement();
		
		NodeList ntList = root.getElementsByTagName("nt");
		
		for(int i = 0; i <= ntList.getLength() - 1; i++) {
			Element nonTerminal  = (Element)ntList.item(i);
			
			NodeList wordList = nonTerminal.getElementsByTagName("edge");
			List<Word> terminalList = new ArrayList<Word>();
			
			for(int z = 0; z <= wordList.getLength() - 1; z++) {
				//get the <edge> element = words of nt's
				Element edge = (Element)wordList.item(z);
				String terminalRef = edge.getAttribute("idref");
				Word terminal = this.contentWords.get(terminalRef);
				
				if (terminal != null) terminalList.add(terminal);
				
			}
			if (terminalList.size() > 0)
				nonTerminals.put(nonTerminal.getAttribute("id"), terminalList);
		}
	}
	
	/**
	 * Writes the Interim File consisting of a single inline annotated file.
	 * 
	 * @param outputPath Path to the destination file.
	 * @throws IOException If the output file could not be written.
	 */
	public void writeInlineFormatFile(String outputPath) throws IOException {
		File f = new File(outputPath);
		Writer w = new FileWriter(f);
		Writer bw = new BufferedWriter(w);
		
		List<String> wordsList = new ArrayList<String>();
		wordsList.addAll(this.contentWords.keySet());
		Collections.sort(wordsList);
		String multipleWord = null;
		
		/*
		 * General idea: iterate over all the words and see, whether this word 
		 * (or phrase) belongs to a coreference chain.
		 */
		for (String word : this.wordsList) {
			String wordOutput;
			
			if (contentWords.get(word).getToken().matches("[\\.\\?!]"))
				wordOutput = contentWords.get(word).getToken() + "\n";
			else if (contentWords.get(word).getToken().matches("[,;\\:'].*"))
				wordOutput = contentWords.get(word).getToken();
			else 
				wordOutput = " " + contentWords.get(word).getToken();
			
			if (multipleWord != null) {
				
				if (isInNonTerminal(multipleWord, word)) {
					
					if (contentWords.get(word).getToken().matches("[\\.\\?!]"))
						bw.write(contentWords.get(word).getToken());
					else {bw.write(wordOutput);}
					
				}else {
					//end of multiple word
					bw.write("</markable>" + wordOutput);
					multipleWord = null;
				}
			} 
			 else if (this.wordCorefSetAlignment.get(word) != null) {
				// single word coreference
				bw.write(" <markable coref_set=\""+ this.wordCorefSetAlignment.get(word) + 
						"\">"+ contentWords.get(word).getToken() + "</markable>");
			} 
			 else if (getNonTerminal(word) != null && 
					this.wordCorefSetAlignment.get(getNonTerminal(word)) != null) {
				// multiple word coreference
				String nonTerminal = getNonTerminal(word); 
				bw.write(" <markable coref_set=\""+ this.wordCorefSetAlignment.get(nonTerminal) +
						"\">");
				multipleWord = nonTerminal;
				bw.write(contentWords.get(word).getToken());
			
			} else bw.write(wordOutput);
		}
		bw.close();
		System.out.println("\n\n######\nSalsa conversion finished.\nOutput file written to " +
				output + "\n######\n");
	}
	
	/**
	 * Determines whether a word is part of a non terminal (usually a phrase).
	 * 
	 * @param nonTerminal The non terminal which is investigated.
	 * @param word The word which is controlled. 
	 * @return <code>True</code>, if the word is part of the specified non terminal, <code>
	 * 		   false</code> otherwise.
	 */
	private boolean isInNonTerminal(String nonTerminal, String word) {
		for (Word w : nonTerminals.get(nonTerminal)) {
			
			if (w.getXmlId().equals(word)) return true;	
		}
		return false;			
	}
	
	/**
	 * Returns the corresponding non terminal for a word.
	 * 
	 * @param wordId The word for which the nonterminal is searched.
	 * @return The corresponding non terminal for a word
	 */
	private String getNonTerminal(String wordId) {
		for (String nonTerminal: this.nonTerminals.keySet()) {
			
			if (nonTerminals.get(nonTerminal).get(0) == null)
				continue;
			else if (nonTerminals.get(nonTerminal).get(0).getXmlId().equals(wordId))
					return nonTerminal;
		}
		return null;
		
	}
	
	/**
	 * Extracts the attributes word, id and PoS for an Element and returns a {@link Word} object.
	 * 
	 * @param tElement The Element, for which the attributes are searched.
	 * @return A {@link Word} object containing all the extracted information.
	 */
	private Word extractWordInformation(Element tElement) {
		// get text values of word and id
		String word = tElement.getAttribute("word");
		String id = tElement.getAttribute("id");
		String pos = tElement.getAttribute("pos");

		//Create a new word with the values obtained from the xml file	
		Word newWord = new Word(word, pos, id);

		return newWord;
	}

	
	/**
	 * Main process which extracts the coreferences and coordinates the other
	 * methods.
	 */
	public void extractCoreferences() throws FileFormatException {
		this.extractContentWords();
		this.extractNonTerminals();
		
		//get the root elememt
		Element root = doc.getDocumentElement();
		
		//get coreference <frame> elements
		List<Element> coreferenceElements = getFrameElements(root);
		
		int corefSet = 0;
		
		//itereate over <frame> tags and extract the coreferences
		for (Element coreferenceFrame : coreferenceElements) {
			// get all fe-nodes under the current <frame>-tag
			NodeList feNodes = coreferenceFrame.getElementsByTagName("fe");
			

			if (feNodes.getLength() > 0) {
				// get the <fe name="current"> node (first item under <frame>)
				Element firstElement = (Element) feNodes.item(0);
				//extract current id from <fenode idref = "xy">
				String firstId = extractIdRef(firstElement);
				
				
				// get the <fe name="coreferent"> node (second item unter <frame>)
				Element secondElement = (Element) feNodes.item(1);
				//extract coreferent id from <fenode idref = "">
				String secondId = extractIdRef(secondElement);
				
				if (wordCorefSetAlignment.get(firstId) != null)
					wordCorefSetAlignment.put(secondId,wordCorefSetAlignment.get(firstId));
				else if (wordCorefSetAlignment.get(secondId) != null)
					wordCorefSetAlignment.put(firstId,wordCorefSetAlignment.get(secondId));
				else {
					corefSet += 1;
					wordCorefSetAlignment.put(firstId,String.valueOf(corefSet));
					wordCorefSetAlignment.put(secondId,String.valueOf(corefSet));
				}
			}	
		}
		
		if (wordCorefSetAlignment.size() == 0)
			throw new FileFormatException("No coreferences found in the provided file");
	}
	
	/**
	 * Extracts &lt;frame&gt; nodes and only returns coreference relations.
	 * 
	 * @param root Document root node.
	 * @return List of Frame elements containing coreference relations.
	 */
	private List<Element> getFrameElements(Element root) {
		// List of indices actually being coreference nodes!
		List<Element> coreferenceFrames = new ArrayList<Element>();
		
		//get a nodelist of ALL <frame> elements
		NodeList frameList = root.getElementsByTagName("frame");
		
		
		
		//iterate over frame-elements to sort out non-coreference nodes
		if(frameList != null && frameList.getLength() > 0) {
			
			for(int i = 0 ; i < frameList.getLength(); i++) {
				//get the current <frame> element
				//cast seems to be necessary and common (evil!)
				Element frameElement = (Element)frameList.item(i);
				
				// if it's a coreference node: store index
				if (frameElement.getAttribute("name").matches("Coreference"))
					coreferenceFrames.add(frameElement);
			}
		}
		
		return coreferenceFrames;
	}
	
	/**
	 * Getter for the content words extracted from the document.
	 * 
	 * @return Content words extracted from the document
	 */
	public Map<String, Word> getContentWords() {return this.contentWords;}
	
	
	/**
	 * Extracts the word id for the fenode-Element under nodeElement.
	 * 
	 * @param nodeElement Mother node for &lt;fenode&gt;, usually a &lt;fe&gt;-node.
	 * @return Word ID.
	 */
	private String extractIdRef(Element nodeElement) {
		NodeList nl = nodeElement.getElementsByTagName("fenode");
		Element feNode = (Element)nl.item(0);
		String coreferentString = feNode.getAttribute("idref");
		
		return coreferentString;
	}	
	
	/**
	 * The main method to convert a file in Tiger/Salsa format to an Interim file which can 
	 * be processed by MMAX2's Project Importer.
	 *  
	 * @param args Input file in Tiger/Salsa XML format.
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		SalsaParser converter = new SalsaParser(args[0], args[1]);
	}
}
