package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.destroystokyo.paper.event.player.PlayerClientOptionsChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerClientOptionsChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // player client options change
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Player Always.
    //
    // @Triggers when a player changes their client options.
    //
    // @Context
    // <context.server_listings_enabled> returns an ElementTag(Boolean) of whether the player has server listings enabled.
    // <context.chat_visibility> returns the player's chat visibility.
    // <context.locale> returns the player's locale.
    // <context.main_hand> returns an ElementTag(Boolean) of whether the player's main hand has changed.
    // <context.skin_parts> returns a MapTag of whether the player's skin parts are enabled or not.
    // <context.view_distance> returns the player's view distance.
    // <context.server_listings_changed> returns whether the player's server listings have changed.
    // <context.chat_colors> returns whether the player has chat colors enabled.
    // <context.chat_colors_changed> returns whether the player's chat colors have changed.
    // <context.chat_visibility_changed> returns the player's chat visibility.
    // <context.locale_changed> returns whether the player's locale has changed.
    // <context.main_hand_changed> returns whether the player's main hand has changed.
    // <context.skin_parts_changed> returns whether the player's skin parts have changed.
    // <context.text_filtering_changed> returns whether the player's text filtering has changed.
    // <context.text_filtering_enabled> returns whether the player has text filtering enabled.
    // <context.view_distance_changed> returns whether the player's view distance has changed.
    //
    // @Player Always.
    //
    // -->

    public PlayerClientOptionsChangeScriptEvent() {
        registerCouldMatcher("player client options change");
    }

    public PlayerClientOptionsChangeEvent event;

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        return switch (name) {
            case "server_listings_enabled" -> NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? new ElementTag(event.allowsServerListings()) : null;
            case "chat_visibility" -> new ElementTag(event.getChatVisibility());
            case "locale" -> new ElementTag(event.getLocale());
            case "main_hand" -> new ElementTag(event.getMainHand());
            case "skin_parts" -> {
                MapTag map = new MapTag();
                map.putObject("cape", new ElementTag(event.getSkinParts().hasCapeEnabled()));
                map.putObject("jacket", new ElementTag(event.getSkinParts().hasJacketEnabled()));
                map.putObject("leftSleeve", new ElementTag(event.getSkinParts().hasLeftSleeveEnabled()));
                map.putObject("rightSleeve", new ElementTag(event.getSkinParts().hasRightSleeveEnabled()));
                map.putObject("leftPants", new ElementTag(event.getSkinParts().hasLeftPantsEnabled()));
                map.putObject("rightPants", new ElementTag(event.getSkinParts().hasRightPantsEnabled()));
                map.putObject("hat", new ElementTag(event.getSkinParts().hasHatsEnabled()));
                yield map;
            }
            case "view_distance" -> new ElementTag(event.getViewDistance());
            case "server_listings_changed" -> NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? new ElementTag(event.hasAllowServerListingsChanged()) : null;
            case "chat_colors_enabled" -> new ElementTag(event.hasChatColorsEnabled());
            case "chat_colors_changed" -> new ElementTag(event.hasChatColorsEnabledChanged());
            case "chat_visibility_changed" -> NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? new ElementTag(event.hasChatVisibilityChanged()) : null;
            case "locale_changed" -> new ElementTag(event.hasLocaleChanged());
            case "main_hand_changed" -> new ElementTag(event.hasMainHandChanged());
            case "skin_parts_changed" -> new ElementTag(event.hasSkinPartsChanged());
            case "text_filtering_changed" -> NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? new ElementTag(event.hasTextFilteringChanged()) : null;
            case "text_filtering_enabled" -> NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) ? new ElementTag(event.hasTextFilteringEnabled()) : null;
            case "view_distance_changed" -> new ElementTag(event.hasViewDistanceChanged());
            default -> super.getContext(name);
        };
    }

    @EventHandler
    public void onPlayerClientOptionsChange(PlayerClientOptionsChangeEvent event) {
        this.event = event;
        fire(event);
    }
}
