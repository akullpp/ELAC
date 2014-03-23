/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.testacr
 * class: Evaluation
 * 
 * Authors: E-Mail
 * Thomas B�gel: boegel@cl.uni-heidelberg.de
 * Lukas Funk: funk@cl.uni-heidelberg.de
 * Andreas Kull: kull@cl.uni-heidelberg.de
 * 
 * Please find a detailed explanation of this particular class/package and its role and usage at
 * the first JavaDoc following this comment.
 * 
 * Copyright 2010 Thomas B�gel & Lukas Funk & Andreas Kull
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

import java.util.ArrayList;
import java.util.List;

import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Mention;
import de.uniheidelberg.cl.swp.datastructure.Word;
import de.uniheidelberg.cl.swp.featureExtraction.FeatureMatchChecker;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.io.Logging;

/**
 * This class handles the comparison and evaluation 
 * of acr outputs according to a given Gold Standard.
 * <br>
 * See Documentation for more details on how to use this class.
 * <br><br>
 * <b>Evaluation modes:</b><br>
 * There are currently two main evaluation methods implemented, see {@link Mode} for details.
 *
 */
public class Evaluation {
	/**
	 * Number of pairs which have been predicted correctly.
	 */
	private double truePositives = 0;
	
	/**
	 * Number of pairs which are coreferential (according to the 
	 * gold standard) but haven't been recognized by the system.
	 */
	private double falseNegatives = 0;
	
	/**
	 * A false positive (FP) occurs when the
	 * outcome is incorrectly predicted as positive when it is actually negative.
	 */
	private double falsePositives = 0;
	
	/**
	 * Correctly classified coreference pairs.
	 * 
	 */
	private List<CoreferencePair> truePositiveList;
	
	/**
	 * False positives (correct CorefPairs from the gold standard
	 * which haven't been recognized by the system). 
	 */
	private List<CoreferencePair> falseNegativeList;
	
	/**
	 * A false positive (FP) occurs when the
	 * outcome is incorrectly predicted as positive when it is actually negative.
	 */
	private List<CoreferencePair> falsePositiveList;
	
	private Mode evaluationMode;
	
	/**
	 * Specifies the evaluation mode to be used.
	 *
	 */
	public enum Mode {
		/**
		 * This is a widely used standard evaluation method for coreference chains.
		 * All the coreferent expressions belonging together are evaluated as chains. Points are 
		 * assigned for each link between two elements in one chain. This means, the system has to
		 * recognize all the coreference pairs in one chain across 
		 * the whole text.
		 */
		MUC6,
		
		/**
		 * This is a simplified method to evaluate {@link CoreferencePair}s, only testing for two 
		 * sequenced {@link CoreferencePair}s. This method is used by JavaRap but doesn't recognize
		 *  any chains across the text.
		 */
		DIRECTNEIGBORSONLY;
	}
	
	/**
	 * Initialize an evaluation process for an ACR system.
	 * As a default, MUC6 is used as Evaluation {@link Mode}. 
	 */
	public Evaluation() {
		this(Mode.valueOf(Configuration.getInstance().getProperties().getProperty(
																			"EvaluationMethod")));
	}
	
	/**
	 * Initialize an evaluation process for an ACR system.
	 * The mode can be specified by the caller. 
	 */
	private Evaluation(Mode mode) {
		this.falseNegativeList = new ArrayList<CoreferencePair>();
		this.truePositiveList = new ArrayList<CoreferencePair>();
		this.falsePositiveList = new ArrayList<CoreferencePair>();
		this.evaluationMode = mode;
	}
	
	
	
	/**
	 * Evaluate acrEntities in respect to a List of {@link CoreferencePair}s from the gold
	 * Standard. Computes both the precision and the recall.
	 * 
	 * @param acrCorefPairs Predictions by the ACR system being evaluated.
	 * @param goldCorefPairs Gold standard = correct {@link CoreferencePair}s.
	 * @return Recall of the system, (correctly classified / CoreferencePairs in goldStandard)
	 */
	public double evaluateCorefPairs(List<CoreferencePair> acrCorefPairs,
			List<CoreferencePair> goldCorefPairs) {
		Logging.getInstance().getLogger().entering(null, null);
		Logging.getInstance().getLogger().info(" \t\t [*** Evaluation process ***]");

		// for each file: delete the lists containing the results
		truePositiveList.clear();
		falseNegativeList.clear();
		falsePositiveList.clear();
		
		computeFalsePositives(goldCorefPairs, acrCorefPairs);
		
		double currentRecall = computeRecall(goldCorefPairs, acrCorefPairs);
		double currentPrecision = truePositives / (falsePositiveList.size() + 
													truePositiveList.size());
		
		Logging.getInstance().getLogger().info(" \t\t [*** Evaluation result: " + 
														truePositiveList.size() + 
														" true positives ***]");
		
		Logging.getInstance().getLogger().info(" \t\t [*** Recall for this file: " + 
														currentRecall + " ***]");
		
		Logging.getInstance().getLogger().info(" \t\t [*** Precision for this file: " + 
														currentPrecision + " ***]");
		
		return currentRecall;
	}
	
	/**
	 * Evaluate acrEntities in respect to a List of {@link CoreferencePair}s from the gold
	 * Standard, filtering out {@link CoreferencePair}s which do not match the required 
	 * feature values.
	 * 
	 * @param acrCorefPairs
	 * @param goldCorefPairs
	 * @return Recall of the system, (correctly classified / CoreferencePairs in goldStandard)
	 */
	public double evaluateFeatureDep(List<CoreferencePair> acrCorefPairs,
			List<CoreferencePair> goldCorefPairs) {
		// sort out all corefpairs with features, that shouldn't be evaluated
		FeatureMatchChecker fmc = new FeatureMatchChecker();
		goldCorefPairs = fmc.filterNonMatchingFeatures(goldCorefPairs);
		acrCorefPairs = fmc.filterNonMatchingFeatures(acrCorefPairs);
		return this.evaluateCorefPairs(acrCorefPairs, goldCorefPairs);
	}
	
	
	/**
	 * Computes the recall for the current system.
	 * Only coref-pairs which fulfill the required feature values will be used as gold standard to 
	 * allow feature  based performance measurement.
	 * 
	 * @param goldCorefPairs
	 * @param hypothesis
	 * @return The recall for the current acr-system according and mmax-file.
	 */
	private double computeRecall(List<CoreferencePair> goldCorefPairs,
			List<CoreferencePair> hypothesis) {
		

		// the number of correctly recognized pairs for the current file
		float currentPoints = 0F;
		
		
		goldStandard:
		// try to find a corresponding corefPair for each pair out of the gold standard
		for (CoreferencePair corefPair : goldCorefPairs) {
			
			if (this.evaluationMode == Mode.DIRECTNEIGBORSONLY &&
					corefPair.isDirectNeighbor() == false)
				continue;
			
			
			for (CoreferencePair hypoPair : hypothesis) {
				
				if (isCorrectPair(corefPair, hypoPair)) {
					currentPoints += 1.0;
					corefPair.setAcrSystem(hypoPair.getAcrSystem());
					truePositiveList.add(corefPair);
					continue goldStandard;
				}
				
			}	falseNegativeList.add(corefPair);	
		}
		
		this.truePositives += truePositiveList.size();
		this.falseNegatives += falseNegativeList.size();
		
		return currentPoints/goldCorefPairs.size();
	}
	
	/**
	 * Computes the false positives for the current system.
	 * I.e. correctly classified / #pairs in hypothesis.
	 * Actually, this methods only determines false positives, as all other performance values 
	 * used to compute the precision are gathered by computeRecall.
	 * 
	 * @param goldCorefPairs
	 * @param hypothesis
	 * @return The recall for the current acr-system according and mmax-file.
	 */
	private double computeFalsePositives(List<CoreferencePair> goldCorefPairs, 
										 List<CoreferencePair> hypothesis) {
		/* Idea: iterate over acr hypothesis and try to find a corresponding 
		 * pair the gold standard. If there is none  -> false positive. 
		 */
		hypothesisLoop:
		for (CoreferencePair hypoPair : hypothesis) {
						
			if (this.evaluationMode == Mode.DIRECTNEIGBORSONLY &&
					hypoPair.isDirectNeighbor() == false) {
				continue;
			}

			for (CoreferencePair goldPair : goldCorefPairs) {
				
				if (isCorrectPair(goldPair, hypoPair)) continue hypothesisLoop;
				
			}	falsePositiveList.add(hypoPair);	
		}
		
		this.falsePositives += falsePositiveList.size();

		return falsePositives;
	}
	
	
	/**
	 * Determines whether there is a correspondence between a predicted {@link CoreferencePair} and
	 * the List of {@link CoreferencePair}s in the gold standard.
	 * 
	 * @param prediciton The prediction which should be evaluated.
	 * @param goldStandard The list of {@link CoreferencePair} extracted from the gold standard.
	 * @return <code>True</code> if there is a corresponding {@link CoreferencePair}
	 * 		   <code>false</code> otherwise.
	 */
	public static boolean findCorrespondingCorefPair(CoreferencePair prediciton, 
													 List<CoreferencePair> goldStandard) {
		for (CoreferencePair goldPair : goldStandard) {
			
			if (isCorrectPair(goldPair, prediciton)) return true;
			
		} return false;	
	}

	
	
	
	
	/**
	 * Checks whether the two coreference pairs denote the same mention.
	 * @param corefPairGold The gold standard coreference pair.
	 * @param corefPairHypo The output from the acr system.
	 * @return <code>True</code> if the coref pairs point refer to the same mention 
	 * 		   (=correct result), <code>false</code> otherwise.
	 */
	private static boolean isCorrectPair(CoreferencePair corefPairGold,
			CoreferencePair corefPairHypo) {
		if (! isCorrectMention(corefPairGold.getAnaphor(), corefPairHypo.getAnaphor(),0)) {
			
			if (! isCorrectMention(corefPairGold.getAntecedent(), corefPairHypo.getAnaphor(),0))
				return false;
			
			else {
				
				if (isCorrectMention(corefPairGold.getAnaphor(), corefPairHypo.getAntecedent(),0))
					return true;
			}
				
		} else {
			
			if (isCorrectMention(corefPairGold.getAntecedent(), corefPairHypo.getAntecedent(),0))
				return true;
		}
		
		return false;
	}
	
	/**
	 * This method checks whether two mentions match by iteration over all the words of one
	 * mention.
	 * 
	 * @param mention1 The mention which has been predicted.
	 * @param mention2 The mention extracted from Gold Standard.
	 * @param wordPosition The word position of the first word.
	 * @return <code>True</code> if the mentions match, <code>false</code> otherwise.
	 */
	private static boolean isCorrectMention(Mention mention1, Mention mention2,
			int wordPosition) {
		if ( wordPosition > (mention2.getWordList().size() -1)) return false;
		
		Word word1  = mention2.getWordList().get(wordPosition); 
		
		for (Word word2 : mention1.getWordList()) {
			
			if (wordIdMatch(word1, word2)) return true;
		}
		
		return isCorrectMention(mention1, mention2, wordPosition + 1);
	}
	
	/**
	 * Checks, whether two word IDs denote the same {@link Word}.
	 * @param goldWord The first word to compare. 
	 * @param hypoWord The second word to compare.
	 * @return <code>True</code> if the Words match, <code>false</code> otherwise.
	 */
	private static boolean wordIdMatch(Word goldWord, Word hypoWord) {
		return (goldWord.getXmlId().equals(hypoWord.getXmlId()));
	}
	

	/**
	 * Getter for the list of false negatives.
	 * 
	 * @return A list of false negative {@link CoreferencePair}s 
	 */
	public List<CoreferencePair> getFalseNegativeList() {return falseNegativeList;}

	/**
	 * Getter for the list of true positives.
	 * 
	 * @return A list of true positives {@link CoreferencePair}s 
	 */
	public List<CoreferencePair> getTruePositiveList() {return truePositiveList;}

	/**
	 * Getter for the list of false positives.
	 * 
	 * @return A list of false positive {@link CoreferencePair}s 
	 */
	public List<CoreferencePair> getFalsePositiveList() {return falsePositiveList;}
	
	/**
	 * Getter for the list of false negatives.
	 * 
	 * @return Number of false negatives.
	 */
	public double getFalseNegatives() {return falseNegatives;}
	
	/**
	 * Getter for the list of true positives.
	 * 
	 * @return Number of true positives.
	 */
	public double getTruePositives() {return truePositives;	}
	
	/**
	 * Getter for the list of false positives.
	 * 
	 * @return Number of false positives.
	 */
	public double getFalsePositives() {return falsePositives;}
	
}
