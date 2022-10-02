package com.denizenscript.denizen.paper.events;


import com.denizenscript.denizen.events.BukkitScriptEvent;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.objects.WorldTag;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntryData;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class WorldGameRuleChangeScriptEvent extends BukkitScriptEvent implements Listener {

    // <--[event]
    // @Events
    // gamerule changes (in <world>)
    //
    // @Plugin Paper
    //
    // @Group Paper
    //
    // @Triggers when a gamerule changes.
    //
    // @Context
    // <context.gamerule> returns name of the GameRule which was changed.
    // <context.value> returns the new value of the GameRule.
    // <context.world> returns the world where the GameRule is applied.
    // <context.source_type> returns the sender who changed the GameRule.
    // <context.command_block_location> returns the command block's location (if the command was run from one).
    // <context.command_minecart> returns the EntityTag of the command minecart (if the command was run from one).
    //
    // @Player when the sender of the command is a player.
    //
    // -->

    public WorldGameRuleChangeScriptEvent() {
        registerCouldMatcher("gamerule changes (in <world>)");
    }

    public WorldGameRuleChangeEvent event;

    public WorldTag world;
    public LocationTag commandBlockLocation;
    public EntityTag commandMinecart;
    public ElementTag sourceType;
    public CommandSender source;

    @Override
    public boolean matches(ScriptPath path) {
        if (path.eventArgLowerAt(2).equals("in") && !world.tryAdvancedMatcher(path.eventArgLowerAt(3))) {
            return false;
        }
        return super.matches(path);
    }

    @Override
    public ObjectTag getContext(String name) {
        switch (name) {
            case "gamerule": return new ElementTag(event.getGameRule().getName());
            case "value": return new ElementTag(event.getValue());
            case "source_type": return sourceType;
            case "command_block_location": return commandBlockLocation;
            case "command_minecart": return commandMinecart;
            case "world": return new WorldTag(event.getWorld());
        }
        return super.getContext(name);
    }

    @Override
    public boolean applyDetermination(ScriptPath path, ObjectTag determinationObj) {
        if (determinationObj instanceof ElementTag) {
            String lower = CoreUtilities.toLowerCase(determinationObj.toString());
            if (lower.startsWith("value:")) {
                ElementTag value = new ElementTag(lower.substring("value:".length()));
                if (value.isInt() || value.isBoolean()) {
                    event.setValue(value.toString());
                    return true;
                }
            }
        }
        return super.applyDetermination(path, determinationObj);
    }

    @Override
    public ScriptEntryData getScriptEntryData() {
        PlayerTag player = null;
        if (source instanceof Player) {
            player = new PlayerTag((Player) source);
        }
        return new BukkitScriptEntryData(player, null);
    }

    @EventHandler
    public void onGameRuleChanged(WorldGameRuleChangeEvent event) {
        this.source = event.getCommandSender();
        this.commandMinecart = getCommandMinecart();
        this.commandBlockLocation = getCommandBlock();
        this.sourceType = getSourceType();
        this.event = event;
        fire(event);
    }

    public LocationTag getCommandBlock() {
        if (source instanceof BlockCommandSender)
            return new LocationTag(((BlockCommandSender) source).getBlock().getLocation());
        return null;
    }

    public EntityTag getCommandMinecart() {
        if (source instanceof CommandMinecart)
            return new EntityTag(((CommandMinecart) source));
        return null;
    }

    public ElementTag getSourceType() {
        if (source instanceof Player) {
            return new ElementTag("player");
        }
        else if (source instanceof BlockCommandSender) {
            return new ElementTag("command_block");
        }
        else if (source instanceof CommandMinecart) {
            return new ElementTag("command_minecart");
        }
        else {
            return new ElementTag("server");
        }
    }
}
