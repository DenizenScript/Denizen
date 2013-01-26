package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.Depends;
import net.aufdemrand.denizen.utilities.packets.handler.NameplateHandler;

public class PacketHelper {

	private final NameplateHandler npHandler;
	
	public PacketHelper(Denizen denizen) {
		npHandler = new NameplateHandler(denizen);
		
		Depends.protocolManager.addPacketListener(getNameplateHandler());
	}
	
	public final NameplateHandler getNameplateHandler() {
		 return npHandler;
	}
	
}
