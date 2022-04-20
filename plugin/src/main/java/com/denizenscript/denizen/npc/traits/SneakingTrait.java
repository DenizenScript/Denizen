package com.denizenscript.denizen.npc.traits;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.NPCTag;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import org.bukkit.entity.EntityType;
import org.bukkit.event.Listener;

public class SneakingTrait extends Trait implements Listener {

    @Persist("sneaking")
    private boolean sneaking = false;

    @Override
    public void onSpawn() {
        if (sneaking) {
            sneak();
        }
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
        new NPCTag(npc).action("sneak", null);
        if (npc.getEntity().getType() != EntityType.PLAYER) {
            return;
        }
        NMSHandler.entityHelper.setSneaking(npc.getEntity(), true);
        sneaking = true;
    }

    /**
     * Makes the NPC stand
     */
    public void stand() {
        // Notated in SittingTrait
        new NPCTag(npc).action("stand", null);
        NMSHandler.entityHelper.setSneaking(npc.getEntity(), false);
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
