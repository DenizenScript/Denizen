package com.denizenscript.denizen.events.server;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

public class TabCompleteScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // tab complete
    //
    // @Group Server
    //
    // @Switch command:<command_name> to only process the event if the command matches the input name.
    //
    // @Cancellable true
    //
    // @Triggers when a player or the console is sent a list of available tab completions.
    //
    // @Context
    // <context.buffer> returns the full raw command buffer.
    // <context.command> returns the command name.
    // <context.current_arg> returns the current argument for completion.
    // <context.completions> returns a list of available tab completions.
    // <context.server> returns true if the tab completion was triggered from the console.
    //
    // @Determine
    // ListTag to set the list of available tab completions.
    //
    // @Player when the tab completion is done by a player.
    //
    // -->

    public TabCompleteScriptEvent() {
        registerCouldMatcher("tab complete");
        registerSwitches("command");
    }

    public TabCompleteEvent event;

    @Override
    public boolean matches(ScriptPath path) {
        if (!runGenericSwitchCheck(path, "command", getCommand())) {
            return false;
        }
        return super.matches(path);
    }

    public String getCommand() {
        String[] args = event.getBuffer().trim().split(" ");
        String cmd = args.length > 0 ? args[0] : "";
        if (event.getSender() instanceof Player) {
            cmd = cmd.replaceFirst("/", "");
        }
        return cmd;
    }

    public String getCurrentArg() {
        int i = event.getBuffer().lastIndexOf(' ');
        return i > 0 ? event.getBuffer().substring(i + 1) : getCommand();
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        String determination = determinationObj.toString();
        if (determination.length() > 0) {
            event.setCompletions(ListTag.valueOf(determination, getTagContext(path)));
            return true;
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(event.getSender() instanceof Player ? new PlayerTag((Player) event.getSender()) : null, null);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "buffer":
                return new ElementTag(event.getBuffer());
            case "command":
                return new ElementTag(getCommand());
            case "current_arg":
                return new ElementTag(getCurrentArg());
            case "completions":
                return new ListTag(event.getCompletions());
            case "server":
                return new ElementTag(event.getSender() instanceof ConsoleCommandSender);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        this.event = event;
        fire(event);
    }
}
