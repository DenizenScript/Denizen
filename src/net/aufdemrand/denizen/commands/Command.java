package net.aufdemrand.denizen.commands;

public interface Command {

	public boolean register();
	
	public boolean execute();
	
	public String toString();
	
}
