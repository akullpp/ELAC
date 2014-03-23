/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.testacr
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
 * <p>
 * This package provides the interface for all the tasks dealing with PerformanceMeasurement.
 * </p>
 * 
 * <b>Classes involved</b>
 * <ul>
 * <li>The whole process is executed and monitored by
 * {@link de.uniheidelberg.cl.swp.testacr.PerformanceMeasurementMain}.</li>
 * <li>The Runner.java-Interface defines methods which should be provided by each runner class
 * (e.g. for each system).</li>
 * <li>Each system is represented by one class
 * (e.g. {@link de.uniheidelberg.cl.swp.testacr.JavaRapRunner}, 
 * {@link de.uniheidelberg.cl.swp.testacr.LingpipeRunner} and
 * {@link de.uniheidelberg.cl.swp.testacr.BARTRunner}).<br>
 * The runner classes know how to run the system on a specified input file and how to extract the
 * results.</li>
 * <li>Evaluation.java will evaluate the results and compare them to the gold standard.</li>  
 * </ul>
 */
package de.uniheidelberg.cl.swp.testacr;


