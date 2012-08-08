package net.aufdemrand.denizen.scripts;

public class PriorityPair implements Comparable<PriorityPair> {
	int priority;
	String name;

	public PriorityPair(int priority, String scriptName) {
		this.priority = priority;
		this.name = scriptName;
	}

	@Override
	public int compareTo(PriorityPair pair) {
		return priority < pair.priority ? -1 : priority > pair.priority ? 1 : 0;
	}
}
