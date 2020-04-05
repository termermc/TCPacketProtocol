package net.termer.tcpacketprotocol.util;

import java.util.Random;

/**
 * Utility class to output unique integers.
 * @author termer
 * @since 1.0
 */
public class IntGenerator {
	// Create an integer with the minimum value
	private static int _val = Integer.MIN_VALUE;
	
	private static Random _rand = new Random();
	
	/**
	 * Returns a new int, which will be incremented from the last call.
	 * @return A new int
	 * @since 1.0
	 */
	public static int nextInt() {
		// Reset if needed
		if(_val == Integer.MAX_VALUE)
			_val = Integer.MIN_VALUE;
		
		return _val++;
	}
	
	/**
	 * Returns a random int
	 * @return A random int
	 * @since 1.0
	 */
	public static int randomInt() {
		return _rand.nextInt();
	}
}
