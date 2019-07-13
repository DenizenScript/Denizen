package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.nms.abstracts.ProfileEditor;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.UUID;

public class MirrorTrait extends Trait {

    @Persist("")
    public boolean mirror = true;

    public MirrorTrait() {
        super("mirror");
    }

    public static UUID getUUID(NPC npc) {
        UUID uuid = npc.getUniqueId();
        if (uuid.version() == 4) { // clear version
            long msb = uuid.getMostSignificantBits();
            msb &= ~0x0000000000004000L;
            msb |= 0x0000000000002000L;
            uuid = new UUID(msb, uuid.getLeastSignificantBits());
        }
        return uuid;
    }

    public static void respawn(NPC npc) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), () -> {
            if (npc.isSpawned()) {
                Location loc = npc.getEntity().getLocation();
                npc.despawn(DespawnReason.PENDING_RESPAWN);
                Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), () -> {
                    npc.spawn(loc);
                });
            }
        });
    }

    public static void mirrorOn(NPC npc) {
        UUID uuid = getUUID(npc);
        if (!ProfileEditor.mirrorUUIDs.contains(uuid)) {
            ProfileEditor.mirrorUUIDs.add(uuid);
            respawn(npc);
        }
    }

    public static void mirrorOff(NPC npc) {
        UUID uuid = getUUID(npc);
        if (ProfileEditor.mirrorUUIDs.contains(uuid)) {
            ProfileEditor.mirrorUUIDs.remove(uuid);
            respawn(npc);
        }
    }

    public void enableMirror() {
        mirror = true;
        mirrorOn(npc);
    }

    public void disableMirror() {
        mirror = false;
        mirrorOff(npc);
    }

    @Override
    public void onSpawn() {
        if (mirror) {
            mirrorOn(npc);
        }
    }

    @Override
    public void onRemove() {
        if (mirror) {
            mirrorOff(npc);
        }
    }

    @Override
    public void onAttach() {
        if (mirror) {
            mirrorOn(npc);
        }
    }
}
