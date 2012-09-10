package net.aufdemrand.denizen.listeners;

import org.bukkit.event.EventHandler;

public abstract class AbstractListener {

	
	
	@EventHandler
	public abstract <E> void listen(E event);
	
	public abstract void save();
	
	public abstract void load();
	
	public abstract void complete();
	
}
