package com.denizenscript.denizen.paper.events;

import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import static com.denizenscript.denizen.paper.PaperModule.stringifyComponent;

public class PlayerCompletesAdvancementScriptEventPaperImpl extends BukkitScriptEvent implements Listener {

    public PlayerCompletesAdvancementScriptEventPaperImpl() {
        instance = this;
    }
    public static PlayerCompletesAdvancementScriptEventPaperImpl instance;
    public PlayerAdvancementDoneEvent event;
    @Override
    public boolean couldMatch(ScriptPath path) {
        return path.eventLower.startsWith("player completes advancement");
    }

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "name", event.getAdvancement().getKey().getKey())) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public String getName() {
        return "PlayerCompletesAdvancement";
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getPlayer());
    }

    @Override
    public ObjectTag getContext(String name) {
        if (name.equals("criteria")) {
            ListTag criteria = new ListTag();
            criteria.addAll(event.getAdvancement().getCriteria());
            return criteria;
        }
        else if (name.equals("advancement")) {
            return new ElementTag(event.getAdvancement().getKey().getKey());
        }
        else if (name.equals("message")) {
            return new ElementTag(stringifyComponent(event.message(), ChatColor.WHITE));
        }
        return super.getContext(name);
    }
    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        String lower = CoreUtilities.toLowerCase(determination);
        if (lower.startsWith("no_message")) {
            event.message(null);
            return true;
        }
        else if (event instanceof PlayerAdvancementDoneEvent) {
            event.message(Component.text(determination));
            return true;
        }
        else {
            return super.applyDetermination(path, determinationObj);
        }
    }

    @EventHandler
    public void onPlayerCompletesAdvancement(PlayerAdvancementDoneEvent event) {
        // TODO: Should this not fire if it's a 'fake' advancement created by Denizen?
        this.event = event;
        fire(event);
    }
}

