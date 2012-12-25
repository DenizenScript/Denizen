package net.aufdemrand.denizen.listeners.core;

import net.aufdemrand.denizen.listeners.AbstractListenerType;

public class BlockListenerType extends AbstractListenerType{

	enum BlockType { BUILD, COLLECT, BREAK }
	
	@Override
	public void onEnable() {
		//nothing to do here
	}
}
