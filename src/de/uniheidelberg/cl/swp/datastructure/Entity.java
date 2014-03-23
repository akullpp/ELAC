/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.datastructure
 * class: Entity
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
 * An entity represents entities from ACL/ACE-annotation. Each entity has a unique id and a list of
 *  coreferents which refer to it.
 */
public class Entity implements Comparable<Entity>{
	
	@SuppressWarnings("unused")
	private String type;
	private String id;
	
	/**
	 * List of coreferents = list of mentions
	 */
	private List<Mention> coreferents;
	
	/**
	 * Initializes an Entity with a unique id.
	 * @param entityID unique id for the entity
	 */
	public Entity(String entityID) {
		this.id = entityID;
		this.coreferents = new ArrayList<Mention>();		
	}
	
	/**
	 * Getter for the entity id.
	 * @return The unique id for the entity as a string.
	 */
	public String getID() { return this.id;	}
	
	/**
	 * Getter for all mentions of an entity.
	 * @return The list of {@link Mention}s for this {@link Entity}.
	 */
	public List<Mention> getMentions() { return this.coreferents; }
	
	/**
	 * Add a new {@link Mention} to the list of mentions.
	 * @param newMention new {@link Mention}.
	 */
	public void addMention(Mention newMention) { this.coreferents.add(newMention);	}
	
	/**
	 * Setter for a list containing all Mentions of this Entity.
	 * @param mentions List of {@link Mention}s for this Entity.
	 */
	public void setMentions(List<Mention> mentions) { this.coreferents = mentions;	}
	
	/**
	 * Convenience method to print all mentions assigned to the mention.
	 */
	public void printMentions() {
		System.out.println("[Entity] " + this.getID());
		for (Mention mention : this.coreferents) {
			System.out.println("  '-> Mention: " + mention.getType());
			for (Word word : mention.getWordList()) {
				try {
					System.out.println(" \t -> " + word.getXmlId() + " [" + word.getToken() + "]");
				} catch (Exception e) {
					System.out.println(word);}
			}
		}
	}

	@Override
	public int compareTo(Entity o) {
		if (this.id.matches(o.getID()))
				return 0;
				return this.id.compareTo(o.getID());
	}
}