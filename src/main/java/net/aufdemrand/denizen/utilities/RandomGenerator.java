package net.aufdemrand.denizen.utilities;

import java.util.Random;

public class RandomGenerator {
	
	static Random rand = new Random();
	
	public static int randInt(int from, int to) {
		return rand.nextInt(to - from + 1) + from;
	}
	
	public static int nextInt(int n) {
		return rand.nextInt(n);
	}
	
	public static double nextDouble() {
		return rand.nextDouble();
	}
	
}
