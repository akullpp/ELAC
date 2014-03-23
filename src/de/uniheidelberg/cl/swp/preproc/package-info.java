/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.preproc
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
 * Package used for Preprocessing the ACR results and to convert Salsa/Tiger.<br>
 * ELAC's components PMM and ML expect the presence of corpora in MMAX2 format  which have already 
 * been processed by JavaRap and BART (LingPipe will be executed on the fly). The directory 
 * containing the MMAX2 files should therefore also contain the results 
 * of those two systems. This is done to save runtime as BART and JavaRap tend to be quite time 
 * consuming.
 * <p>
 * To achieve this, we implemented a preprocessing package, which does the following:
 * <ul>
 * <li> Run JavaRap on a MMAX2 corpus and store the results under 
 * 		<code>./Basedata/javarap/</code></li>
 * <li> Run BART on a MMAX2 corpus and store the results under 
 * 		<code>./Markables/.*response_level.xml</code> </li>
 * <li> Convert a Salsa/Tiger Corpus into an interims format using inline annotation. </li> </ul>
 * </p>
 */
package de.uniheidelberg.cl.swp.preproc;