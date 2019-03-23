package net.aufdemrand.denizen.events.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.events.BukkitScriptEvent;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.scripts.ScriptEntryData;
import net.aufdemrand.denizencore.scripts.containers.ScriptContainer;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.TabCompleteEvent;

public class PlayerTabCompleteScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // tab complete
    //
    // @Regex ^on tab complete$
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
    // dList to set the list of available tab completions.
    //
    // @Player when the tab completion is done by a player.
    //
    // -->

    public PlayerTabCompleteScriptEvent() {
        instance = this;
    }

    public static PlayerTabCompleteScriptEvent instance;
    public TabCompleteEvent event;
    public String buffer;
    public dList completions;
    public CommandSender sender;

    public String getCommand() {
        String[] args = buffer.trim().split(" ");
        String cmd = args.length > 0 ? args[0] : "";
        if (sender instanceof Player) {
            cmd = cmd.replaceFirst("/", "");
        }
        return cmd;
    }

    public String getCurrentArg() {
        int i = buffer.lastIndexOf(' ');
        return i > 0 ? buffer.substring(i + 1) : getCommand();
    }

    @Override
    public boolean couldMatch(ScriptContainer scriptContainer, String s) {
        return CoreUtilities.toLowerCase(s).startsWith("tab complete");
    }

    @Override
    public boolean matches(ScriptPath path) {
        return true;
    }

    @Override
    public String getName() {
        return "TabComplete";
    }

    @Override
    public boolean applyDetermination(ScriptContainer container, String determination) {
        if (determination.length() > 0 && !isDefaultDetermination(determination)) {
            completions = dList.valueOf(determination);
            return true;
        }
        return super.applyDetermination(container, determination);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        return new BukkitScriptEntryData(sender instanceof Player ? new dPlayer((Player) sender) : null, null);
    }

    @Override
    public dObject getContext(String name) {
        switch (name) {
            case "buffer":
                return new Element(buffer);
            case "command":
                return new Element(getCommand());
            case "current_arg":
                return new Element(getCurrentArg());
            case "completions":
                return completions;
            case "server":
                return new Element(sender instanceof ConsoleCommandSender);
        }
        return super.getContext(name);
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        buffer = event.getBuffer();
        completions = new dList(event.getCompletions());
        sender = event.getSender();
        this.event = event;
        fire(event);
        event.setCompletions(completions);
    }
}
