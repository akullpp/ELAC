/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.testacr
 * class: LingpipeRunner
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

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eml.MMAX2.core.MMAX2;


import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.ChunkFactory;
import com.aliasi.chunk.Chunker;
import com.aliasi.chunk.Chunking;
import com.aliasi.coref.EnglishMentionFactory;
import com.aliasi.coref.Mention;
import com.aliasi.coref.MentionFactory;
import com.aliasi.coref.WithinDocCoref;
import com.aliasi.sentences.IndoEuropeanSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.AbstractExternalizable;

import de.uniheidelberg.cl.swp.datastructure.CoreferencePair;
import de.uniheidelberg.cl.swp.datastructure.Entity;
import de.uniheidelberg.cl.swp.datastructure.Word;
import de.uniheidelberg.cl.swp.io.Configuration;
import de.uniheidelberg.cl.swp.io.Logging;
import de.uniheidelberg.cl.swp.util.MMAXParser;

/**
 * Implementation of the LingPipe coreference algorithm.
 * Requires lingpipe.jar in the build path.<br>
 * LingPipe returns the coreference pairs as absolute word positions which are 
 * mapped to the words in the {@link MMAX2} files to be able to convert the 
 * words into {@link CoreferencePair}s.
 * 
 * <a href="http://alias-i.com/lingpipe/web/download.html">Lingpipe download</a>.
 *
 *
 */
public class LingpipeRunner extends Runner {

	/**
	 * Default path to the chunkermodel used by mEntityChunker.
	 */
	private final static String DEFAULTCHUNKERMODEL =
		"./models/ne-en-news-muc6.AbstractCharLmRescoringChunker";
	
	/* Lingpipe pipeline components */
    protected SentenceChunker mSentenceChunker;
    private Chunker mEntityChunker;
    static Pattern MALE_PRONOUNS 	= Pattern.compile("\\b(He|he|Him|him|His|his)\\b");
    static Pattern FEMALE_PRONOUNS 	= Pattern.compile("\\b(She|she|Her|her|Hers|hers)\\b");


    /**
     * Final list of predicted entities.
     */
    private List<Entity> entityList;
    
    /**
     * Map used to store results temporarily and to 
     * be able to add mentions to entities later. 
     */
    private HashMap<String,Entity> entityMap;
    


    public void init(MMAXParser mmax) {
    	this.mmax = mmax;
    }
    
    /**
     * Constructor to initialize {@link LingpipeRunner}. <br>
     * Loads the components used by LingPipe.
     */
    public LingpipeRunner() {
    	this.entityList = new ArrayList<Entity>();

    	/* Read the chunker model path for lingpipe from the property file */
    	String chunkerResourceName =
    		Configuration.getInstance().getProperties().getProperty("LingPipeChunkgerModel",
    				DEFAULTCHUNKERMODEL);
    	
    	try {
	    	/* Load the pipeline Components used by lingpipe */
    		TokenizerFactory mTokenizerFactory = IndoEuropeanTokenizerFactory.class.newInstance(); 
	    	SentenceModel mSentenceModel = IndoEuropeanSentenceModel.class.newInstance();
	    	this.mSentenceChunker = new SentenceChunker(mTokenizerFactory,mSentenceModel);
			this.mEntityChunker = 
						(Chunker)AbstractExternalizable.readObject(new File(chunkerResourceName));
			
	} catch (Exception e) {
		System.err.println("Couldn't load Lingpiperunner.");
		e.printStackTrace(); }
	}
   
    /**
     * Run the acrSystem.
     */
    public void run() {
		this.entityList.clear();
		
		// start linpgipe process
		this.process(mmax.getPlainTextAsString().toCharArray(), 0, 
					 mmax.getPlainTextAsString().length());
    }
    
    /**
     * Getter for the acr's predictions as a List of Entities.
     */
    public List<Entity> getCoreferents() {return this.entityList; }

    /**
     * Find the corresponding Word (in respect to MMAx Words) at the
     * same position to get its id.
     *  
     * @param absolutePosition The position within the text for the current word.
     * @return The Word object referring to the corresponding MMAx-Word.
     */
    public Word getWordFromAbsolutePosition(int absolutePosition) {
    	for (Word word : mmax.getWordList()) {
    		if (word.getAbsolutePosition() == absolutePosition-1) return word;
		}
    	return null;
    }
    
    /**
     * Extract sentences from the specified character slice,
     * wrapping them in XML sentence elements and deferring
     * their text to <code>processSentence</code> for further
     * processing.
     *
     * @param cs Underlying characters.
     * @param start Index of the first character of slice.
     * @param end Index of one past the last character of the slice.
     */
    public void process(char[] cs, int start, int end) {

		MentionFactory mf = new EnglishMentionFactory();
		WithinDocCoref coref = new WithinDocCoref(mf);
		String text = new String(cs,start,end-start);
	
		this.entityMap = new HashMap<String,Entity>();
		
		Chunking sentenceChunking
		    = mSentenceChunker.chunk(cs,start,end);
		Iterator<Chunk> sentenceIt
		    = sentenceChunking.chunkSet().iterator();
		
		for (int i = 0; sentenceIt.hasNext(); ++ i) {
		    Chunk sentenceChunk = sentenceIt.next();
		    int sentStart = sentenceChunk.start();
		    int sentEnd = sentenceChunk.end();
		    String sentenceText = text.substring(sentStart,sentEnd);
	
		    processSentence(sentenceText,i,mf,coref, sentStart);
		}
		
		// finally, add all the predictions to the map containing the results
		this.entityList.addAll(this.entityMap.values());
    }	    


    /**
     * Code written by LingPipe.
     * <br>
     * Please consult their documentation.
     */
    public void processSentence(String sentenceText, int sentId, MentionFactory mf,
				WithinDocCoref coref, int sentstart) {

		Chunking mentionChunking
		    = mEntityChunker.chunk(sentenceText);
	
		Set<Chunk> chunkSet = new TreeSet<Chunk>(Chunk.TEXT_ORDER_COMPARATOR);
		chunkSet.addAll(mentionChunking.chunkSet());
	
		addPronouns(MALE_PRONOUNS,"MALE_PRONOUN",sentenceText,chunkSet);
		addPronouns(FEMALE_PRONOUNS,"FEMALE_PRONOUN",sentenceText,chunkSet);
			
		Iterator<Chunk> it = chunkSet.iterator();
		String text = mentionChunking.charSequence().toString();
		
		while (it.hasNext()) {
		    Chunk neChunk = it.next();
		    int start = neChunk.start();
		    int end = neChunk.end();
		    String type = neChunk.type();
		    String chunkText = text.substring(start,end);
		    Mention mention = mf.create(chunkText,type);
		    int mentionId = coref.resolveMention(mention,sentId);
		    
		    start = neChunk.start() + sentstart;
		    end = neChunk.end() + sentstart;
		    
		    // found a corresponding (MMAx) Word at mentions's position
		    if (!(getWordFromAbsolutePosition(start) == null)) {
		    	// add the mention to the appropriate entity
		    	de.uniheidelberg.cl.swp.datastructure.Mention m =
		    		new de.uniheidelberg.cl.swp.datastructure.Mention();
				m.addWord(getWordFromAbsolutePosition(start));
		    	String entityId = Integer.toString(mentionId);
		    	
				if (! this.entityMap.containsKey(entityId)) {
					this.entityMap.put(entityId, new Entity(entityId));
		    	}	
				this.entityMap.get(entityId).addMention(m);	    	
		    }
		    // no corresponding word found -> skip this Mention!
		    else
		    	Logging.getInstance().getLogger().warning("No corresponding Word object found " +
		    			"at position " + start + " >" + chunkText);
		}
    }

    
    /**
     * Code written by LingPipe.
     * <br>
     * Please consult their documentation.
     */
    void addPronouns(Pattern pattern, String tag, String sentenceText, Set<Chunk> chunkSet) {
    	java.util.regex.Matcher matcher = pattern.matcher(sentenceText);
    	int pos = 0;
    	while (matcher.find(pos)) {
    		Chunk proChunk = ChunkFactory.createChunk(matcher.start(),
    				matcher.end(),
    				tag);
    		// incredibly inefficient quadratic algorithm here, but bounded by sentence
    		Iterator<Chunk> it = chunkSet.iterator();
    		while (it.hasNext()) {
    			Chunk chunk = it.next();

    			if (overlap(chunk.start(),chunk.end(),
    					proChunk.start(),proChunk.end()))
    				it.remove();
    		}
    		chunkSet.add(proChunk);
    		pos = matcher.end();
    	}
    }
    
    /**
     * Code written by LingPipe.
     * <br>
     * Please consult their documentation.
     */
    static boolean overlap(int start1, int end1, int start2, int end2) {
    	return java.lang.Math.max(start1,start2) < java.lang.Math.min(end1,end2);
    }
}
