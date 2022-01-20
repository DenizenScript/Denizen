package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import org.bukkit.Material;

public class PlayerBoostElytra implements Property {

    public static boolean describes(ObjectTag player) {
        return player instanceof PlayerTag
                && ((PlayerTag) player).isOnline()
                && ((PlayerTag) player).getPlayerEntity().isGliding();
    }

    public static PlayerBoostElytra getFrom(ObjectTag player) {
        if (!describes(player)) {
            return null;
        }
        return new PlayerBoostElytra((PlayerTag) player);
    }

    public static final String[] handledMechs = new String[] {
            "firework_boost"
    };

    public PlayerBoostElytra(PlayerTag player) {
        this.player = player;
    }

    PlayerTag player;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "firework_boost";
    }

    public static void registerTags() {
        // None
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object PlayerTag
        // @name firework_boost
        // @input ItemTag
        // @Plugin Paper
        // @description
        // Firework boosts the player with the specified firework rocket.
        // The player must be gliding.
        // -->
        if (mechanism.matches("firework_boost") && mechanism.hasValue()) {
            ItemTag item = mechanism.valueAsType(ItemTag.class);
            if (item.getBukkitMaterial() != Material.FIREWORK_ROCKET) {
                mechanism.echoError("Invalid input item: must be a firework rocket.");
                return;
            }
            player.getPlayerEntity().boostElytra(item.getItemStack());
        }
    }
}
