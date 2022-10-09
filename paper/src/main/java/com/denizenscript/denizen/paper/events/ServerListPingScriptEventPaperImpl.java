package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.events.server.ListPingScriptEvent;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.paper.PaperModule;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.destroystokyo.paper.event.server.PaperServerListPingEvent;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.profile.PlayerTextures;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class ServerListPingScriptEventPaperImpl extends ListPingScriptEvent {

    public static class FakeProfile implements PlayerProfile {
        public String name;
        @Override public @Nullable String getName() {
            return name;
        }
        @Override public @NotNull String setName(@Nullable String s) {
            String old = name;
            name = s;
            return old;
        }
        @Override public @Nullable UUID getUniqueId() { return null; }
        @Override public @Nullable UUID getId() { return null; }
        @Override public @Nullable UUID setId(@Nullable UUID uuid) { return null; }
        @Override public @NotNull PlayerTextures getTextures() { return null; }
        @Override public void setTextures(@Nullable PlayerTextures playerTextures) { }
        @Override public @NotNull Set<ProfileProperty> getProperties() { return null; }
        @Override public boolean hasProperty(@Nullable String s) { return false; }
        @Override public void setProperty(@NotNull ProfileProperty profileProperty) { }
        @Override public void setProperties(@NotNull Collection<ProfileProperty> collection) { }
        @Override public boolean removeProperty(@Nullable String s) { return false; }
        @Override public void clearProperties() { }
        @Override public boolean isComplete() { return false; }
        @Override public @NotNull CompletableFuture<PlayerProfile> update() { return null; }
        @Override public org.bukkit.profile.@NotNull PlayerProfile clone() { return null; }
        @Override public boolean completeFromCache() { return false; }
        @Override public boolean completeFromCache(boolean b) { return false; }
        @Override public boolean completeFromCache(boolean b, boolean b1) { return false; }
        @Override public boolean complete(boolean b) { return false; }
        @Override public boolean complete(boolean b, boolean b1) { return false; }
        @Override public @NotNull Map<String, Object> serialize() { return null; }
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("protocol_version:") && ArgumentHelper.matchesInteger(determination.substring("protocol_version:".length()))) {
            ((PaperServerListPingEvent) event).setProtocolVersion(Integer.parseInt(determination.substring("protocol_version:".length())));
            return true;
        }
        else if (lower.startsWith("version_name:")) {
            ((PaperServerListPingEvent) event).setVersion(determination.substring("version_name:".length()));
            return true;
        }
        else if (lower.startsWith("exclude_players:")) {
            HashSet<UUID> exclusions = new HashSet<>();
            for (PlayerTag player : ListTag.valueOf(determination.substring("exclude_players:".length()), getTagContext(path)).filter(PlayerTag.class, getTagContext(path))) {
                exclusions.add(player.getUUID());
            }
            Iterator<Player> players = ((PaperServerListPingEvent) event).iterator();
            while (players.hasNext()) {
                if (exclusions.contains(players.next().getUniqueId())) {
                    players.remove();
                }
            }
            return true;
        }
        else if (lower.startsWith("alternate_player_text:")) {
            if (!CoreConfiguration.allowRestrictedActions) {
                Debug.echoError("Cannot use 'alternate_player_text' in list ping event: 'Allow restricted actions' is disabled in Denizen config.yml.");
                return true;
            }
            ((PaperServerListPingEvent) event).getPlayerSample().clear();
            for (String line : ListTag.valueOf(determination.substring("alternate_player_text:".length()), getTagContext(path))) {
                FakeProfile lineProf = new FakeProfile();
                lineProf.setName(line);
                ((PaperServerListPingEvent) event).getPlayerSample().add(lineProf);
            }
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public void setMotd(String text) {
        event.motd(PaperModule.parseFormattedText(text, ChatColor.WHITE));
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "motd":
                return new ElementTag(PaperModule.stringifyComponent(event.motd()));
            case "protocol_version":
                return new ElementTag(((PaperServerListPingEvent) event).getProtocolVersion());
            case "version_name":
                return new ElementTag(((PaperServerListPingEvent) event).getVersion());
            case "client_protocol_version":
                return new ElementTag(((PaperServerListPingEvent) event).getClient().getProtocolVersion());
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onListPing(PaperServerListPingEvent event) {
        syncFire(event);
    }
}
