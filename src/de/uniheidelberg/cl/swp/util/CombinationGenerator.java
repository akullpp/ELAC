/*
 * ELAC: Ensemble Learning for Anaphora- and Coreference-Resolution-Systems
 * package: de.uniheidelberg.cl.swp.util
 * class: CombinationGenerator
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
package de.uniheidelberg.cl.swp.util;

import java.math.BigInteger;


/**
 * This class determines all possible combinations following the approach of Kenneth H Rosen.
 * <br>
 * The code was originally written by Michael Gilleland (see 
 * <a href="http://www.merriampark.com/comb.htm">http://www.merriampark.com/comb.htm</a> for
 * details) though we slightly modified it for our project.
 * <br>
 * Please read the original documentation for furter information and license restrictions of this
 * class.
 */
public class CombinationGenerator {
	private int[] a;
	private int n;
	private int r;
	private BigInteger numLeft;
	private BigInteger total;

	/**
	 * Default Constructor.
	 * 
	 * @param n The number of Integers from which is chosen.
	 * @param r The number of "clusters".
	 */
	public CombinationGenerator(int n, int r) {
		if (r > n) {
			throw new IllegalArgumentException();
		}
		if (n < 1) {
			throw new IllegalArgumentException();
		}
		this.n = n;
		this.r = r;
		a = new int[r];
		BigInteger nFact = getFactorial(n);
		BigInteger rFact = getFactorial(r);
		BigInteger nminusrFact = getFactorial(n - r);
		total = nFact.divide(rFact.multiply(nminusrFact));
		reset();
	}

	/**
	 * Reset.
	 */
	public void reset () {
		for (int i = 0; i < a.length ; i++) {
			a[i] = i;
		}
		numLeft = new BigInteger (total.toString ());
	}

	/**
	 * Determines whether more combinations exist or not.
	 * 
	 * @return True if more combinations exists, false otherwise.
	 */
	public boolean hasMore() {
		return numLeft.compareTo(BigInteger.ZERO) == 1;
	}

	/**
	 * Computes factorials.
	 * 
	 * @param n Number of loops.
	 * @return Factorial.
	 */
	private static BigInteger getFactorial(int n) {
		BigInteger fact = BigInteger.ONE;
		for (int i = n; i > 1; i--) {
			fact = fact.multiply(new BigInteger(Integer.toString(i)));
		}
		return fact;
	}

	/**
	 * Main entry point to return all the possible combinations.
	 * 
	 * @return Array containing a possible combination.
	 */
	public int[] getNext() {
		if (numLeft.equals(total)) {
			numLeft = numLeft.subtract(BigInteger.ONE);
			return a;
		}
		int i = r - 1;
		
		while (a[i] == n - r + i) {
			i--;
		}
		a[i] = a[i] + 1;

		for (int j = i + 1; j < r; j++) {
			a[j] = a[i] + j - i;
		}
		numLeft = numLeft.subtract(BigInteger.ONE);
		
		return a;
	}
}
