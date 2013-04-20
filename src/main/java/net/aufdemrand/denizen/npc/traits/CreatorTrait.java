package net.aufdemrand.denizen.npc.traits;

import net.citizensnpcs.api.trait.Trait;

public class CreatorTrait extends Trait {
	
	String creator = null;
	
	
	public String getCreator() {
		return creator;
	}
	
	protected CreatorTrait() {
		super("Creator");
	}

}
