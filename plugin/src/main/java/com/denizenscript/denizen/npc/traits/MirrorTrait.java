package com.denizenscript.denizen.npc.traits;
import com.denizenscript.denizen.Denizen;
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

    public void respawn() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> {
            if (npc.isSpawned()) {
                Location loc = npc.getEntity().getLocation();
                npc.despawn(DespawnReason.PENDING_RESPAWN);
                Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> {
                    npc.spawn(loc);
                });
            }
        });
    }

    public void mirrorOn() {
        UUID uuid = getUUID(npc);
        if (!ProfileEditor.mirrorUUIDs.contains(uuid)) {
            ProfileEditor.mirrorUUIDs.add(uuid);
            respawn();
        }
    }

    public void mirrorOff() {
        UUID uuid = getUUID(npc);
        if (ProfileEditor.mirrorUUIDs.contains(uuid)) {
            ProfileEditor.mirrorUUIDs.remove(uuid);
            respawn();
        }
    }

    public void enableMirror() {
        mirror = true;
        mirrorOn();
    }

    public void disableMirror() {
        mirror = false;
        mirrorOff();
    }

    @Override
    public void onSpawn() {
        if (mirror) {
            mirrorOn();
        }
    }

    @Override
    public void onRemove() {
        if (mirror) {
            mirrorOff();
        }
    }

    @Override
    public void onAttach() {
        if (mirror) {
            mirrorOn();
        }
    }
}
