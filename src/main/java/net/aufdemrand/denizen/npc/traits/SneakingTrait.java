package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.minecraft.server.v1_8_R3.EntityHuman;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public class SneakingTrait extends Trait implements Listener  {

    @Persist("sneaking")
    private boolean sneaking = false;

    EntityHuman eh = null;

    @Override
    public void onSpawn() {
        eh = ((CraftPlayer) npc.getEntity()).getHandle();
        if (sneaking) sneak();
    }

    // <--[action]
    // @Actions
    // sneak
    //
    // @Triggers when the NPC starts sneaking.
    //
    // @Context
    // None
    //
    // -->
    /**
     * Makes the NPC sneak
     */
    public void sneak() {
        DenizenAPI.getDenizenNPC(npc).action("sneak", null);

        if (npc.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        eh.getDataWatcher().watch(0, (byte) 0x02);

        sneaking = true;
    }

    /**
     * Makes the NPC stand
     */
    public void stand() {
        // Notated in SittingTrait
        DenizenAPI.getDenizenNPC(npc).action("stand", null);

        eh.getDataWatcher().watch(0, (byte) 0x00);

        sneaking = false;
    }

    /**
     * Checks if the NPC is currently sneaking
     *
     * @return boolean
     */
    public boolean isSneaking() {
        return sneaking;
    }

    public SneakingTrait() {
        super("sneaking");
    }
}
