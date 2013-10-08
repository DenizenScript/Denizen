package net.aufdemrand.denizen.utilities.packets;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.aufdemrand.denizen.utilities.packets.handler.InvisibleLoreHandler;
import net.aufdemrand.denizen.utilities.packets.handler.NameplateHandler;

public class PacketHelper {

    private static NameplateHandler npHandler;
    private static InvisibleLoreHandler ilHandler;

    public PacketHelper(Denizen denizen) {
        npHandler = new NameplateHandler(denizen);

        // TODO: Find a way to get this to work in creative
        // ilHandler = new InvisibleLoreHandler(denizen);

        Depends.protocolManager.addPacketListener(getNameplateHandler());
    }

    public static NameplateHandler getNameplateHandler() {
         return npHandler;
    }

    public static InvisibleLoreHandler getInvisibleLoreHandler() {
        return ilHandler;
    }

}
