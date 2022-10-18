package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.Material;

public class PaperPlayerExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <PlayerTag.affects_monster_spawning>
        // @returns ElementTag(Boolean)
        // @mechanism PlayerTag.affects_monster_spawning
        // @group properties
        // @Plugin Paper
        // @description
        // Returns whether the player affects monster spawning. When false, no monsters will spawn naturally because of this player.
        // -->
        PlayerTag.registerOnlineOnlyTag(ElementTag.class, "affects_monster_spawning", (attribute, object) -> {
            return new ElementTag(object.getPlayerEntity().getAffectsSpawning());
        });

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
        PlayerTag.registerOnlineOnlyMechanism("affects_monster_spawning", ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireBoolean()) {
                object.getPlayerEntity().setAffectsSpawning(input.asBoolean());
            }
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name firework_boost
        // @input ItemTag
        // @Plugin Paper
        // @description
        // Firework boosts the player with the specified firework rocket.
        // The player must be gliding.
        // -->
        PlayerTag.registerOnlineOnlyMechanism("firework_boost", ItemTag.class, (object, mechanism, input) -> {
            if (!object.getPlayerEntity().isGliding()) {
                mechanism.echoError("Cannot adjust 'firework_boost': player must be gliding.");
                return;
            }
            if (input.getBukkitMaterial() != Material.FIREWORK_ROCKET) {
                mechanism.echoError("Invalid input item '" + input + "': must be a firework rocket.");
                return;
            }
            object.getPlayerEntity().boostElytra(input.getItemStack());
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name fake_op_level
        // @input ElementTag(Number)
        // @Plugin Paper
        // @description
        // Sends a fake operator level to the client, enabling clientside op-required features like the debug gamemode hotkey (F3+F4).
        // Input should be a number from 0 to 4, 0 indicating not op and 4 indicating maximum level op.
        // The input number value corresponds to "op-permission-level" in the server.properties. See also <@link url https://minecraft.fandom.com/wiki/Permission_level>
        // This will be reset when a player rejoins, changes world, has their real op status changed, ...
        // -->
        PlayerTag.registerOnlineOnlyMechanism("fake_op_level", ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireInteger()) {
                object.getPlayerEntity().sendOpLevel((byte) input.asInt());
            }
        });

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {

            // <--[mechanism]
            // @object PlayerTag
            // @name add_tab_completions
            // @input ListTag
            // @Plugin Paper
            // @description
            // Adds custom tab completions that will be suggested to the player when typing in chat.
            // Tab completions added by this mechanism can be removed using <@link mechanism PlayerTag.remove_tab_completions>.
            // -->
            PlayerTag.registerOnlineOnlyMechanism("add_tab_completions", ListTag.class, (object, mechanism, input) -> {
                object.getPlayerEntity().addAdditionalChatCompletions(input);
            });

            // <--[mechanism]
            // @object PlayerTag
            // @name remove_tab_completions
            // @input ListTag
            // @Plugin Paper
            // @description
            // Removes custom tab completions added by <@link mechanism PlayerTag.add_tab_completions>.
            // -->
            PlayerTag.registerOnlineOnlyMechanism("remove_tab_completions", ListTag.class, (object, mechanism, input) -> {
                object.getPlayerEntity().removeAdditionalChatCompletions(input);
            });
        }
    }
}
