package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.destroystokyo.paper.ClientOption;
import com.destroystokyo.paper.SkinParts;
import net.kyori.adventure.util.TriState;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PaperPlayerExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <PlayerTag.affects_monster_spawning>
        // @returns ElementTag(Boolean)
        // @mechanism PlayerTag.affects_monster_spawning
        // @group paper
        // @Plugin Paper
        // @description
        // Returns whether the player affects monster spawning. When false, no monsters will spawn naturally because of this player.
        // -->
        PlayerTag.registerOnlineOnlyTag(ElementTag.class, "affects_monster_spawning", (attribute, object) -> {
            return new ElementTag(object.getPlayerEntity().getAffectsSpawning());
        });

        // <--[tag]
        // @attribute <PlayerTag.client_options>
        // @returns MapTag
        // @group paper
        // @Plugin Paper
        // @description
        // Returns the player's client options as a MapTag.
        // The allow_server_listings & text_filtering_enabled options are only available in MC 1.19+.
        // -->
        PlayerTag.registerOnlineOnlyTag(MapTag.class, "client_options", (attribute, object) -> {
            MapTag map = new MapTag();
            Player player = object.getPlayerEntity();
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
                map.putObject("allow_server_listings", new ElementTag(player.getClientOption(ClientOption.ALLOW_SERVER_LISTINGS)));
            }
            map.putObject("chat_colors_enabled", new ElementTag(player.getClientOption(ClientOption.CHAT_COLORS_ENABLED)));
            map.putObject("chat_visibility", new ElementTag(player.getClientOption(ClientOption.CHAT_VISIBILITY)));
            map.putObject("locale", new ElementTag(player.getClientOption(ClientOption.LOCALE)));
            map.putObject("main_hand", new ElementTag(player.getClientOption(ClientOption.MAIN_HAND)));
            MapTag skinParts = new MapTag();
            SkinParts parts = player.getClientOption(ClientOption.SKIN_PARTS);
            skinParts.putObject("cape", new ElementTag(parts.hasCapeEnabled()));
            skinParts.putObject("hat", new ElementTag(parts.hasHatsEnabled()));
            skinParts.putObject("jacket", new ElementTag(parts.hasJacketEnabled()));
            skinParts.putObject("left_sleeve", new ElementTag(parts.hasLeftSleeveEnabled()));
            skinParts.putObject("right_sleeve", new ElementTag(parts.hasRightSleeveEnabled()));
            skinParts.putObject("left_pants", new ElementTag(parts.hasLeftPantsEnabled()));
            skinParts.putObject("right_pants", new ElementTag(parts.hasRightPantsEnabled()));
            map.putObject("skin_parts", skinParts);
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
                map.putObject("text_filtering_enabled", new ElementTag(player.getClientOption(ClientOption.TEXT_FILTERING_ENABLED)));
            }
            map.putObject("view_distance", new ElementTag(player.getClientOption(ClientOption.VIEW_DISTANCE)));
            return map;
        });

        // <--[mechanism]
        // @object PlayerTag
        // @name affects_monster_spawning
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @group paper
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
        // @group paper
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
        // @group paper
        // @description
        // Sends a fake operator level to the client, enabling clientside op-required features like the debug gamemode hotkey (F3+F4).
        // Input should be a number from 0 to 4, 0 indicating not op and 4 indicating maximum level op.
        // The input number value corresponds to "op-permission-level" in the server.properties. See also <@link url https://minecraft.wiki/w/Permission_level>
        // This will be reset when a player rejoins, changes world, has their real op status changed, ...
        // -->
        PlayerTag.registerOnlineOnlyMechanism("fake_op_level", ElementTag.class, (object, mechanism, input) -> {
            if (mechanism.requireInteger()) {
                object.getPlayerEntity().sendOpLevel((byte) input.asInt());
            }
        });

        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {

            // <--[tag]
            // @attribute <PlayerTag.flying_fall_damage>
            // @returns ElementTag(Boolean)
            // @mechanism PlayerTag.flying_fall_damage
            // @group paper
            // @Plugin Paper
            // @description
            // Returns whether the player will take fall damage while <@link tag PlayerTag.can_fly> is true.
            // -->
            PlayerTag.registerOnlineOnlyTag(ElementTag.class, "flying_fall_damage", (attribute, object) -> {
                return new ElementTag(object.getPlayerEntity().hasFlyingFallDamage().toBooleanOrElse(false));
            });

            // <--[mechanism]
            // @object PlayerTag
            // @name add_tab_completions
            // @input ListTag
            // @Plugin Paper
            // @group paper
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
            // @group paper
            // @description
            // Removes custom tab completions added by <@link mechanism PlayerTag.add_tab_completions>.
            // -->
            PlayerTag.registerOnlineOnlyMechanism("remove_tab_completions", ListTag.class, (object, mechanism, input) -> {
                object.getPlayerEntity().removeAdditionalChatCompletions(input);
            });

            // <--[mechanism]
            // @object PlayerTag
            // @name flying_fall_damage
            // @input ElementTag(Boolean)
            // @Plugin Paper
            // @group paper
            // @description
            // Sets whether the player will take fall damage while <@link mechanism PlayerTag.can_fly> is true.
            // @tags
            // <PlayerTag.flying_fall_damage>
            // <PlayerTag.can_fly>
            // -->
            PlayerTag.registerOnlineOnlyMechanism("flying_fall_damage", ElementTag.class, (object, mechanism, input) -> {
                if (mechanism.requireBoolean()) {
                    object.getPlayerEntity().setFlyingFallDamage(TriState.byBoolean(input.asBoolean()));
                }
            });
        }
    }
}
