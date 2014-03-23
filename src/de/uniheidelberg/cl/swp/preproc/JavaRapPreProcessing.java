/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.preproc
 * class: JavaRapPreProcessing
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Entity;
import de.uniheidelberg.cl.swp.datastructure.Mention;
import de.uniheidelberg.cl.swp.datastructure.Word;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.testacr.JavaRapRunner;
import de.uniheidelberg.cl.swp.testacr.PerformanceMeasurementMain;
import de.uniheidelberg.cl.swp.util.MMAXParser;
import de.uniheidelberg.cl.swp.util.SSHConnection;
import edu.nus.comp.nlp.tool.anaphoraresolution.Util;
import edu.nus.comp.nlp.tool.anaphoraresolution.PlainText;



/**
 * This class provides the conversion features needed to process both training and test data with 
 * JavaRap and stores their results so that they can be used by {@link JavaRapRunner}
 * afterwards. <br>
 * 
 * It's possible to execute JavaRap remotely on a specified server.<br>
 * 
 * <br>
 * Javarap.jar is required: 
 * <a href="http://aye.comp.nus.edu.sg/~qiu/NLPTools/JavaRAP.html">
 * http://aye.comp.nus.edu.sg/~qiu/NLPTools/JavaRAP.html</a> <br>
 * 
 * <p>
 * The results are stored in the folder <code>./Basedata/javarap/<filename> </code> for the 
 * corresponding MMAX2 corpus.
 *</p>
 */
public class JavaRapPreProcessing {
	
		private MMAXParser mmax;
		
		/**The current {@link SSHConnection} to the 
		 * server for remote execution. */
		private SSHConnection conn;
		
		/** A Hashmap mapping the words from mmax 
		 *  to the sents/word positions determined by JavaRap. */
		private Map<Integer,Map<Integer,Word>> sentencesForMMAx;
		
		
		/**
		 * Getter for the currently used {@link SSHConnection} for remote execution.
		 * @return The current {@link SSHConnection}.
		 */
		public SSHConnection getConn() {
			return conn;
		}

		
		/**
		 * Goes through JavaRap words and tries to map each one of them 
		 * to the corresponding {@link Word} in the MMAX Words to set their positions.<br> 
		 * This is done to determine the sentence/word positions as javaRap uses a special 
		 * algorithm for sentence splitting and tokenization.
		 * 
		 * @param mmax The MMAX file to be processed.
		 */
		private void matchPositionsWithJavaRap(MMAXParser mmax) {

			System.out.printf("Determining word positions for MMAx words via javarap...");

			String tempFile = mmax.getDiscourse().getWordFileName();
			StringBuffer sbf = Util.read(tempFile);
			PlainText plain = new PlainText(sbf);
			
			// use javarap's sentence splitting algorithm
			plain.setSingleLine(true);
			plain.removeTag();
			
			
		    String[] sents = plain.splitSentences();
		    
		    int sentence = 0;
	    	int currentPositionInWordList = 0;

		    
	    	this.sentencesForMMAx = new HashMap<Integer,Map<Integer,Word>>();

		    
		    // iterate over JavaRap sentences
		    for (int i = 0; i < sents.length; i++) {
		    	
		    	int wordPosition = 0;
		    	System.out.printf(" [" + i + "] ");
		    	
		    	// store each sentence in its own hashmap to improve access time
		    	sentencesForMMAx.put(sentence, new HashMap<Integer,Word>());
		    	
		    	String[] wordsInSent = sents[i].split("   ");
		    	
		    	// iterate over single words
		    	for (int wp = 0; wp < wordsInSent.length; wp++) {
		    		// determine the corresponding word in MMAx by string and position
		    		String token = wordsInSent[wp].trim();
		    		
		    		if (token.length() == 0) {
		    			wordPosition -= 1;
		    			continue;
		    		}
		    		
		    		// look up the corresponding mmax Word object
		    		Word matchingMMAxWord = getWordFromString(token, currentPositionInWordList);
		    			
		    		currentPositionInWordList = mmax.getWordList().indexOf(matchingMMAxWord);
		    		
		    		
		    		// set the word position for the MMAx word 
		    		mmax.getWords().get(matchingMMAxWord.getXmlId()).setRelativeWordPosition(
		    				sentence, wordPosition);
		    		sentencesForMMAx.get(sentence).put(wordPosition, mmax.getWordList().get(
		    				currentPositionInWordList));
		    		wordPosition += 1;
		    	}
		    	sentence += 1;
		   }
		}
		
		
		/**
		 * Runs the JavaRap-System for the specified file.
		 *  
		 * After running JavaRap, the results are processed to extract the coreferents and stored 
		 * in <code>./Basedata/javarap/<filename>.txt</code> 
		 * 
		 * @param corpus Either training or test. 
		 * The path has to be specified via {@link Configuration}).  
		 * @param useSsh True, if a remote connection should be used, false if a local version of
		 *  JavaRap should be used. 
		 * @param javaRapPath Path to AnaphoraResolution.jar (i.e. path to JavaRap). 
		 * 
		 */
		public void run(String corpus, boolean useSsh, String javaRapPath) {
			List<String> mmaxTestFiles = PerformanceMeasurementMain.getMMAxFiles(corpus);

			if (useSsh) {
				if (conn == null) {
					try{conn = new SSHConnection(); }
					catch (Exception e) {
						System.err.println("Couldn't estsablish a connection to the server. " +
								"Wrong hostname?");
						System.exit(1);
					}	
				}
				
			} else {
				File f = new File(javaRapPath);
				if (! f.exists()) {
					System.err.println("\nJavaRap not found. Check JavaRap path!\n");
					System.exit(1);
				}
			}
			
			for (String mmaxTestFile : mmaxTestFiles) {
				/* Load the current mmax file. */
				try { mmax = MMAXParser.processMMAXFile(mmaxTestFile); }
				catch (Exception e) {
					System.err.println("Couldn't load " + mmaxTestFile + ". I'll skip this file.");
					continue;
				}
				
			
				/* List to store JavaRaps console output linewise */
				List<String> javaRapResults = new ArrayList<String>();
			
				matchPositionsWithJavaRap(mmax);

				try {
					// path to the base data file
					File mmaxWordFile = new File(mmax.getDiscourse().getWordFileName());
					
					// inputstream to read javarap's console output
					InputStream stdout;
					Session sess = null;
					
					/* copy the file to be classified temporarily to remote server */
					if (useSsh) {
						try {conn.copyFileToServer(mmaxWordFile.getCanonicalPath()); }
						catch (IOException e) {
							System.err.println("\nCouldn't copy " +
									mmaxWordFile.getCanonicalFile() + " to the server path." +
											"User rights and credentials set correctly?\n");
							System.exit(1);
						}
						
						/* Create a session */
						sess = conn.getCon().openSession();
						// read javarap's output
						stdout = new StreamGobbler(sess.getStdout());
			
						// execute javarap via ssh with the file copied before
						sess.execCommand("java -jar " + javaRapPath + " " + conn.getServerPath() 
											+ mmaxWordFile.getName());
					} else {
						Process child = Runtime.getRuntime().exec("java -jar " + javaRapPath + " " 
								+  mmaxWordFile.getCanonicalPath());
					    stdout = child.getInputStream();
					}
			
					BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
	
					// read javarap's console output
					while (true)
					{
						String line = br.readLine();
						
						if (line == null) break;
						
						if (line.matches("^\\([^\\)]+.*")) {
							/* store each result line in a list which is processed later */
							javaRapResults.add(line);
						}
					}
					
					// if ssh was used: do cleanup and close the session.
					if (useSsh) {
						/* Show exit status, if available (otherwise "null") */
						System.out.println("\n+ ExitCode: " + sess.getExitStatus());
						/* Close this session */
						sess.close();
					}
					
					/* Obtain the correct word positions */
					List<Entity> coreferents = processJavaRapOutput(javaRapResults);
					List<CoreferencePair> corefPairs =
						MMAXParser.splitCorefChainsToPairs(coreferents);
					writeOutput(corefPairs);
					
				} catch (Exception e) {
					e.printStackTrace();	
				}
			}
		}
		
		/**
		 * Writes the results for the current file to
		 * <code>/Basedata/javarap/%filename.txt%</code>.
		 * 
		 * @param corefPairs The List of {@link CoreferencePair}s which JavaRap 
		 * 					 has extracted (using run).
		 */
		private void writeOutput(List<CoreferencePair> corefPairs) {
			String path = this.mmax.getDiscourse().getCommonBasedataPath(); 
			File outputPath = new File(path + "/javarap/");
			
			if (! outputPath.exists()) outputPath.mkdir();
			
			File wordFile = new File(mmax.getDiscourse().getWordFileName());
			
			
			try {
				File outputFile = new File(outputPath.getCanonicalPath() + "/" +
						wordFile.getName());
				Writer w = new FileWriter(outputFile);
				BufferedWriter bw = new BufferedWriter(w);
				
				for (CoreferencePair coreferencePair : corefPairs) {
					bw.write(coreferencePair.getAntecedent().getWordList().get(0).getXmlId() +
							"~" + coreferencePair.getAnaphor().getWordList().get(0).getXmlId() +
							"\n" );
				}	
				bw.close();
				
			}
			catch (Exception e) {
				System.out.println("Dateifehler!");
			}
			
			
		}
		
		/**
		 * Processor of JavaRap output.
		 * <br>
		 * JavaRaps output consists of pairs of words represented by their position in the 
		 * text (sentence and word count), e.g.
		 * (1,4) Bill Clinton <-- (2,4) he
		 * 
		 * Those positions need to be extracted and converted into WordObjects extracted from the 
		 * original MMAX annotation. This is done by explicitly mapping each position from JavaRap
		 * to the corresponding Word from MMAX.
		 * 
		 * @param javaRapOutput Output of JavaRap.
		 */
		private List<Entity> processJavaRapOutput(List<String> javaRapOutput) {		
			/* Store the word posistion in an int[]
			 * antecedentPositions[0]: sentenceCount
			 * antecedentPositions[1]: wordCount      */
			Integer[] antecedentPositions = new Integer[2];
			String antecedent = "";
			
			Integer[] anaphorPositions = new Integer[2];
			String anaphora = "";
			
			System.out.println("Converting JavaRap positions to MMAx words.");
			
			List<Entity> coreferents = new ArrayList<Entity>();
			
			// iterate over all the result lines
			for (String line : javaRapOutput) {
				// regular expression to extract word positions and string representation.
				Pattern p = Pattern.compile("^\\(([^\\)]+)\\) ([^\\<]+)");
				Matcher m = p.matcher(line);
				
				
				if (m.find()) {
					// word positions for the antecedent
					antecedentPositions[0] = Integer.parseInt(m.group(1).split(",")[0]);
					antecedentPositions[1] = Integer.parseInt(m.group(1).split(",")[1]);
					// string representation
					antecedent = m.group(2);
				} else
					continue;

				p = Pattern.compile("- \\(([^\\)]+)\\) (.*)");
				m = p.matcher(line);
				
				if (m.find()) {
					// word positions for the anaphora
					anaphorPositions[0] = Integer.parseInt(m.group(1).split(",")[0]);
					anaphorPositions[1] = Integer.parseInt(m.group(1).split(",")[1]);
					anaphora = m.group(2);
				} else
					continue;
				
				
				/* Create a "null" entity as JavaRap doesn't extract Entities.
				 * Each coreference pair is stored as two mentions of one unique null-entity.
				 */
				Entity nullEntity = new Entity("null");
				
				// Get the word object from MMAX corresponding to their positions.
				Word word1 = getWordForJavaRapPosition(antecedentPositions[0], 
													   antecedentPositions[1], 
													   antecedent.split(" ")[0]);
				Word word2 = getWordForJavaRapPosition(anaphorPositions[0], 
													   anaphorPositions[1], 
													   anaphora.split(" ")[0]);
				
				// Create two new mentions for the pair and add it to the "null"-entity.
				Mention mention1 = new Mention();
				mention1.addWord(word1);
				Mention mention2 = new Mention();
				mention2.addWord(word2);
				
				if (word1 != null && word2 != null) {
					nullEntity.addMention(mention1);
					nullEntity.addMention(mention2);
					// store the current entity in the list of predicted entities.
					coreferents.add(nullEntity);
				}
			}
			return coreferents;
		}
		
		/**
		 * Chooses the nearest String as a fallback option for position estimation.
		 * <br>
		 * If no {@link Word} has been found at the specified position, use a  fallback option,
		 * i.e. look up all the possible corresponding {@link Word}s by string matching and choose
		 * the nearest one.
		 * 
		 * @param sentence Sentence number.
		 * @param wordCount Count of words.
		 * @param token Token.
		 * @param wordAtPosition Word object for position.
		 * @return The most possible Word.
		 */
		private Word getWordFromPositionFallback(int sentence, int wordCount, 
												 String token, Word wordAtPosition) {
			System.err.println("Fallback fuer " + token  + " at " + sentence + "," + wordCount );
			List<Word> possibleWords = new ArrayList<Word>();
			
			/* Remove all puntuation and whitespaces */
			token = token.replaceAll("[\\p{Punct} ]", "");
			
			for (int i = 0 ; i< mmax.getWordList().size(); i++ ) {
				Word cWord = mmax.getWordList().get(i);
				
				if (token.equals(cWord.getToken().replaceAll("[\\p{Punct} ]", "")))	 {
					possibleWords.add(cWord);
				} 
			}

			// take the "nearest" word.
			if (possibleWords.size() > 1)  {
				Word mostProbable = null;
				float minDistance = Float.POSITIVE_INFINITY;
				for (Word word : possibleWords) {
					/* if the current word is nearer than any other word: set this word as nearest 
					 * word. */
					if (Math.abs(word.getAbsolutePosition() -
								 wordAtPosition.getAbsolutePosition())< minDistance) {
						mostProbable = word;
						minDistance = Math.abs(word.getAbsolutePosition() - 
											   wordAtPosition.getAbsolutePosition());
					}
				}
				
				return mostProbable;
			}
			try {// only one word found
				return possibleWords.get(0);
			} catch (Exception e) {
				System.err.println("!! Didn't find a word for " + token);
				return null;
			}
		}
		
		/**
		 * Convers JavaRap's positions to something useful.
		 * <br>
		 * Uses JavaRap's output for text positions, e.g. (1,3) and transfers
		 * this notion to unique ids in the text for 
		 * the denoted word.
		 * 
		 * @param sentence Sentence position.
		 * @param wordCount word position within sentence.
		 * @param token The token to be searched.
		 * @return The Word object at the specified position.
		 */
		private Word getWordForJavaRapPosition(int sentence, int wordCount, String token) {
			// erase punctuation (isn't done by javarap: 'he,' -> 'he')
			if (token.substring(token.length()-1).matches("\\p{Punct}") && token.length() > 1)
				token = token.substring(0, token.length()-1);
			
			Word possible = null;
			
			if (this.sentencesForMMAx.get(sentence) == null) {} 
			else if (this.sentencesForMMAx.get(sentence).get(wordCount) == null) {} 
			else if (this.sentencesForMMAx.get(sentence).get(wordCount).getToken().equals(token)) {
				return this.sentencesForMMAx.get(sentence).get(wordCount); 
			} 
			
			try {
				for (Word curWord : this.sentencesForMMAx.get(sentence).values()) {
					if (curWord.getSentence() == sentence) {
							if (token.equals(curWord.getToken())) {
								return curWord;
							} else if (curWord.getWordPositionInSent() < wordCount + 5 && 
									curWord.getWordPositionInSent() > wordCount - 5 ) {
								possible = curWord;
							}
					}
				} 
				if (possible == null) {
					throw new Exception("No possible word found in sentence " + sentence);
				}
			}
			catch (Exception e) {
					return getWordForJavaRapPosition(sentence -1, wordCount, token);
			}
			return getWordFromPositionFallback(sentence, wordCount, token, possible);
		}
		
		
		/**
		 * Finds a word in MMAX-Wordlist which has not yet been classified
		 * and matches the string.
		 */
		private Word getWordFromString(String token, int start ) {
			// remove punctuation at the end of words
			if (token.trim().substring(token.length()-1).matches("\\p{Punct}") &&
					token.length() > 1)
				token = token.substring(0, token.length()-1);
			
			// look up each word using a sliding window 
			for (int i = start; i< start + 10 && i < this.mmax.getWordList().size() ; i++) {
				if (token.trim().equals(this.mmax.getWordList().get(i).getToken()))
					return this.mmax.getWordList().get(i);
			}

			// if no word has been found: try to take two words together
			for (int i = start; i< start + 10 && i < this.mmax.getWordList().size() -1 ; i++) {
				if (token.replaceAll(" ", "").equals(
						this.mmax.getWordList().get(i).getToken().replaceAll(" ", "")  + 
						this.mmax.getWordList().get(i+1).getToken().replaceAll(" ", ""))) {
					
					return this.mmax.getWordList().get(i);
				}
			}
			
			// if no word has been found, increase the beginning of the window
			return this.mmax.getWordList().get(start+1);
		}
		
		/**
		 * Main method to run JavaRap on the training and test data.
		 * 
		 * <b>Syntax: </b><br> 
		 * <code> java de.uniheidelberg.cl.swp.preproc.JavaRapPreProcessing ssh|loc <path> </code>
		 * 
		 * <p>
		 * To run JavaRap Preprocessing:
		 * <br><br>
		 * 1. Make sure that training and test files have been specified correctly in the config
		 * file!
		 * 2. Make sure you have chmod 777 set on the temporary folder!<br>
		 * </p>
		 * 
		 * <p>
		 * <b>To run JavaRap using a remote connection</b><br>
		 * 
		 * <code> java de.uniheidelberg.cl.swp.preproc.JavaRapPreProcessing ssh <path> </code> 
		 * (note: you are prompted for your credentials) <br> </p>
		 * 
		 * <p>
		 * <b>To run JavaRap locally</b> <br>
		 * <code>java de.uniheidelberg.cl.swp.preproc.JavaRapPreProcessing loc <path> </code>
		 * </p>
		 * 
		 * @param args <code>path</code> Path to the JavaRap jar file (AnaphoraResoultion.jar has 
		 * to exist). <br>
		 * <code>ssh|loc</code> Determines local or remote execution.
		 * 
		 */
		public static void main(String[] args) {
			boolean ssh = false;
			String javaRapPath = null;
			
			if (args.length < 2) {
				System.out.println("Syntax: JavaRapPreprocessing ssh|loc <JavaRap path>\n");
				System.exit(1);
			} else {
				
				if (args[0].matches("ssh")) {
					ssh = true;
				}
				javaRapPath = args[1];	
			}
						 
			JavaRapPreProcessing jp = new JavaRapPreProcessing();
			jp.run("Test", ssh, javaRapPath);
			
			jp.run("Training", ssh, javaRapPath);
			
			/* if a connection still exists: close it as we're finished */
			if (jp.getConn() != null)
				jp.getConn().close();
			
		}


		
	
}
