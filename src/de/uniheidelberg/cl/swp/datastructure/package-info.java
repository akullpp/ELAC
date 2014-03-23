/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.datastructure
 * class: package-info
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
/**
 * This package contains all the necessary data structures used in our project.
 * <br>
 * <p>Architecture:<br>
 * One Entity consists of a List of Mentions. A Mention can be one or several words which
 * refer to the Entity.<br><br>
 * Example:<br> 
 * (Bill Clinton)1 raised the taxes. (He)2 said it was mandatory to ...
 * <br><br>
 * Entity: Bill Clinton<br>
 * 	-> Mention1: (Bill Clinton) [2 Words]<br>
 *  -> Mention2: (he) 			[1 Word]<br>
 *<br>
 * As some tools can't extract the entities, "Null"-Entities (semantically empty) are also 
 * possible so that the reference between the two Mentions (usually anaphora + antecedent) is still
 *  clear.</p>
 */
package de.uniheidelberg.cl.swp.datastructure;
