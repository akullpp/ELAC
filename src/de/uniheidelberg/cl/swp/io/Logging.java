/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.io
 * class: Logging
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
package de.uniheidelberg.cl.swp.io;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;


/**
 * Embeds a {@link Logger} for collecting information.
 * <br>
 * Singleton implementation, so that the {@link Logger} is loaded only once and globally
 * accessible.
 */
public class Logging {
	private static Logging instance = null;
	private Logger logger;
	
	/**
	 * Creates an instance which just embeds a {@link Logger}.
	 */
	private Logging() {
		FileHandler fh;
		this.logger = Logger.getLogger("ACRLogger");
		
		logger.setLevel(Level.INFO);

		String logPath =
			Configuration.getInstance().getProperties().getProperty("PathForLog","./");
		
		try {
			fh = new FileHandler(logPath + "output.log", false);
		    logger.addHandler(fh);
		    // reduce console output
		    logger.setUseParentHandlers(false);
		    SimpleFormatter formatter = new SimpleFormatter();
		    fh.setFormatter(formatter);
		} catch (Exception e) {
			System.err.println("Couldn't instantiate logger!");
			e.printStackTrace();
		} 
	}
	
	/**
	 * Singleton implementation.
	 * 
	 * @return An instance of Logging.
	 */
	public static Logging getInstance() {
		if (instance == null) {
			instance = new Logging();
		}
		return instance;
	}
	
	/**
	 * Getter for the embedded {@link Logger}.
	 * 
	 * @return The embedded Logger.
	 */
	public Logger getLogger() {
		return logger;
	}
}
