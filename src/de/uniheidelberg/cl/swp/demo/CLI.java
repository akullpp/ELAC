/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.demo
 * class: CLI
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
package de.uniheidelberg.cl.swp.demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import de.uniheidelberg.cl.swp.featureExtraction.FeatureDistribution;
import de.uniheidelberg.cl.swp.mlprocess.AblationTesting;
import de.uniheidelberg.cl.swp.mlprocess.MLProcess;
import de.uniheidelberg.cl.swp.preproc.JavaRapPreProcessing;
import de.uniheidelberg.cl.swp.preproc.SalsaParser;
import de.uniheidelberg.cl.swp.testacr.PerformanceMeasurementMain;


/**
 * The intention of this commandline userinterface is to provide a fast solution and entry
 * point for new users.
 * <br>
 * It isn't designed for complex operations or chaining/modifying the program itself. Please check
 * the documentation for further information and the README for detailed instructions on the CLI.
 */
public class CLI {
	private BufferedReader br;
	
	/**
	 * Creates a new main menu.
	 */
	public CLI() { newCLI(); }
	
	/**
	 * Main menu.
	 */
	private void newCLI() {
		br = new BufferedReader(new InputStreamReader(System.in));
		
		System.out.print("\n\nOptions:\n" +
				"1: JavaRap Preprocessing\n" +
				"2: Feature Distribution\n" +
				"3: Salsa Parser\n" +
				"4: Training\n" +
				"5: Testing\n" +
				"6: Ablation Testing\n" +
				"7: Exit\n\n" +
				"Enter a digit: ");
		
		try {
			int choice = Integer.parseInt(br.readLine());
			
			switch (choice) {
			case 1:
				runJRP();
				newCLI();
				break;
			case 2:
				runFD();
				newCLI();
				break;
			case 3:
				runSP();
				newCLI();
				break;
			case 4:
				runPMM();
				newCLI();
				break;
			case 5: 
				runMLP();
				break;
			case 6:
				runAT();
				break;
			default: System.exit(0);
			}
		} catch (IOException ioe) {
			System.err.println("\nError: " + ioe);
			System.exit(1);
		}
		catch (NumberFormatException nfe) {
			System.err.print("\nError: Please enter a digit\n");
			System.exit(1);
		}
	}
	
	/**
	 * Runs the JavaRapPreProcessor.
	 */
	private void runJRP() {
		try {
			String[] args = new String[2];

			System.out.print("\n\nHow to access JavaRap?\n" +
					"1: ssh\n" +
					"2: local\n" +
			"Enter a digit: ");

			int choice = Integer.parseInt(br.readLine());

			switch (choice) {
			case 1:
				args[0] = "ssh";
				break;
			case 2:
				args[0] = "loc";
				break;
			}

			System.out.print("\nPlease specify the JavaRap path: ");

			args[1] = br.readLine();
			JavaRapPreProcessing.main(args);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	/**
	 * Runs the Feature Distribution extractor.
	 */
	private void runFD() {
		try {
			String[] args = new String[1];

			System.out.print("\n\nDistribution of:\n" +
					"1: Test corpus\n" +
					"2: Training corpus\n" +
					"Please enter a digit: ");

			int choice = Integer.parseInt(br.readLine());

			if (choice == 1) { args[0] = "Test"; }
			else if (choice == 2) { args[0] = "Training"; }
			else { throw new NumberFormatException(); }

			FeatureDistribution.main(args);
		} catch (IOException ioe) {
			System.err.println("\nError: " + ioe);
			System.exit(1);
		}
		catch (NumberFormatException nfe) {
			System.err.print("\nError: Please enter a valid digit\n");
			System.exit(1);
		}
	}
	
	/**
	 * Runs the Salsa Parser.
	 */
	private void runSP() {
		String[] args = new String[2];
		
		try {
		System.out.print("\n\nPlease provide a Salsa/Tiger XML file: ");
		
		args[0] = br.readLine();
		
		System.out.print("Please provide a name for new file: ");
		
		args[1] = br.readLine();
		SalsaParser.main(args);
		} catch (IOException ioe) {
			System.out.println("\nCouldn't read file\n");
		}
	}
	
	/**
	 * Starts the training process.
	 */
	private void runPMM() {
		PerformanceMeasurementMain pmm = new PerformanceMeasurementMain();
		try {
			pmm.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Starts the testing process.
	 */
	private void runMLP() {
		try {
			System.out.print("\n\nOptions:\n" +
					"1: Custom ARFF file\n" +
					"2: Default Training's ARFF file\n\n" +
					"Enter a digit: ");

			int choice = Integer.parseInt(br.readLine());

			switch (choice) {
			case 1:
				System.out.print("Provide absolute path: ");
				MLProcess mlp1 = new MLProcess(br.readLine());
				mlp1.run();
				newCLI();
				break;
			case 2:
				MLProcess mlp2 = new MLProcess();
				mlp2.run();
				newCLI();
				break;
			default: System.exit(0);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found!");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs the Ablation Test.
	 */
	private void runAT() {
		try {
			String[] args = new String[1];

			System.out.print("\n\nPlease specify a name for the output file: ");

			args[0] = br.readLine();

			AblationTesting.main(args);
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}
}
