package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Material;

public class PaperPlayerProperties implements Property {
    public static boolean describes(ObjectTag player) {
        return player instanceof PlayerTag
                && ((PlayerTag) player).isOnline();
    }

    public static PaperPlayerProperties getFrom(ObjectTag player) {
        if (!describes(player)) {
            return null;
        }
        return new PaperPlayerProperties((PlayerTag) player);
    }

    public static final String[] handledMechs = new String[] {
            "affects_monster_spawning", "firework_boost", "fake_op_level"
    };

    private PaperPlayerProperties(PlayerTag player) {
        this.player = player;
    }

    PlayerTag player;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "PaperPlayerProperties";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <PlayerTag.affects_monster_spawning>
        // @returns ElementTag(Boolean)
        // @mechanism PlayerTag.affects_monster_spawning
        // @group properties
        // @Plugin Paper
        // @description
        // Returns whether the player affects monster spawning. When false, no monsters will spawn naturally because of this player.
        // -->
        PropertyParser.<PaperPlayerProperties, ElementTag>registerTag(ElementTag.class, "affects_monster_spawning", (attribute, player) -> {
            return new ElementTag(player.player.getPlayerEntity().getAffectsSpawning());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object PlayerTag
        // @name affects_monster_spawning
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @description
        // Sets whether this player affects monster spawning. When false, no monsters will spawn naturally because of this player.
        // @tags
        // <PlayerTag.affects_monster_spawning>
        // -->
        if (mechanism.matches("affects_monster_spawning") && mechanism.requireBoolean()) {
            player.getPlayerEntity().setAffectsSpawning(mechanism.getValue().asBoolean());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name firework_boost
        // @input ItemTag
        // @Plugin Paper
        // @description
        // Firework boosts the player with the specified firework rocket.
        // The player must be gliding.
        // -->
        if (mechanism.matches("firework_boost") && mechanism.requireObject(ItemTag.class)) {
            if (!player.getPlayerEntity().isGliding()) {
                mechanism.echoError("Player must be gliding to use firework_boost.");
                return;
            }
            ItemTag item = mechanism.valueAsType(ItemTag.class);
            if (item.getBukkitMaterial() != Material.FIREWORK_ROCKET) {
                mechanism.echoError("Invalid input item: must be a firework rocket.");
                return;
            }
            player.getPlayerEntity().boostElytra(item.getItemStack());
        }

        // <--[mechanism]
        // @object PlayerTag
        // @name fake_op_level
        // @input ElementTag(Number)
        // @Plugin Paper
        // @description
        // Sends a fake operator level to the client, enabling clientside op-required features like the debug gamemode hotkey (F3+F4).
        // Input should be a number from 0 to 4, 0 indicating not op and 4 indicating maximum level op.
        // -->
        if (mechanism.matches("fake_op_level") && mechanism.requireInteger()) {
            player.getPlayerEntity().sendOpLevel((byte) mechanism.getValue().asInt());
        }
    }
}
