package net.aufdemrand.denizen.utilities.packets.handler;

import com.comphenix.protocol.Packets;
import com.comphenix.protocol.events.ConnectionSide;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.FieldAccessException;
import com.comphenix.protocol.reflect.StructureModifier;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.traits.NameplateTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;


public class NameplateHandler extends PacketAdapter {
    
    public NameplateHandler( Denizen denizen ) {
        super(denizen, ConnectionSide.SERVER_SIDE, Packets.Server.NAMED_ENTITY_SPAWN);
    }
    
    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.isCancelled() || event.getPacketID() != Packets.Server.NAMED_ENTITY_SPAWN) return;
        
        PacketContainer packet = event.getPacket();
        StructureModifier<String> text = packet.getSpecificModifier(String.class);
        
        try {
            Player observer = event.getPlayer();
            Entity watched = packet.getEntityModifier(observer.getWorld()).read(0);
            
            NPCRegistry npcRegistry = CitizensAPI.getNPCRegistry();
        
            if(npcRegistry == null) return; // Save check (e.g. reloads)
            
            for(NPC npc : npcRegistry) {
                if( !npc.hasTrait(NameplateTrait.class) ) continue;

                Entity npcEntity = npc.getBukkitEntity();
        
                if(npcEntity != null && npcEntity.equals(watched)) {
                    NameplateTrait trait = npc.getTrait(NameplateTrait.class);
                    
                    text.write(0, trait.getTrimmedTag(observer.getName()));
                    return;
                }
            }
        } catch(FieldAccessException e) {}
    }
}

