package net.aufdemrand.denizen;

import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.npc.traits.PushableTrait;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Messaging;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {

    private final Citizens plugin;

    public CommandHandler(Citizens plugin) {
        this.plugin = plugin;
    }



        /*
           getdata|adddata|decdata shows/modifies the block data for block in targets.
        
        if (args[0].equalsIgnoreCase("getdata")) {
            player.sendMessage("Current block data: " + player.getTargetBlock(null, 20).getData());
            return true;
        }
        
        if (args[0].equalsIgnoreCase("adddata")) {
            Block toAddData = player.getTargetBlock(null, 20);
            toAddData.setData((byte) (toAddData.getData() + 1));
            player.sendMessage("Current block data: " + player.getTargetBlock(null, 20).getData());
            return true;	
        }
        
        if (args[0].equalsIgnoreCase("decdata")) {
            Block toAddData = player.getTargetBlock(null, 20);
            toAddData.setData((byte) (toAddData.getData() - 1));
            player.sendMessage("Current block data: " + player.getTargetBlock(null, 20).getData());
            return true;
        }

         */

        // Help commands

    /*
        if (args[0].equalsIgnoreCase("help")) {

            if(args.length == 1) {

                player.sendMessage(ChatColor.GOLD + "------- Denizen Commands -------");
                player.sendMessage(ChatColor.GOLD + "");
                player.sendMessage(ChatColor.GOLD + "Denizen Core Commands:");
                player.sendMessage(ChatColor.GOLD + "use /denizen HELP CORE");
                player.sendMessage(ChatColor.GOLD + "");
                player.sendMessage(ChatColor.GOLD + "Denizen NPC Commands:");
                player.sendMessage(ChatColor.GOLD + "use /denizen HELP NPC ");
                player.sendMessage(ChatColor.GOLD + "");
                player.sendMessage(ChatColor.GOLD + "For a cheat sheet of commands and arguments,");
                player.sendMessage(ChatColor.GOLD + "visit the wiki: http://wiki.citizensnpcs.net/Denizen");   
            }

            else if (args[1].equalsIgnoreCase("core")) {

                player.sendMessage(ChatColor.GOLD + "------- Denizen Core Commands -------");
                player.sendMessage(ChatColor.GOLD + "");
                player.sendMessage(ChatColor.GOLD + "/denizen RELOAD");
                player.sendMessage(ChatColor.GOLD + "  Reloads config.yml, scripts.yml and saves.yml");
                player.sendMessage(ChatColor.GOLD + "/denizen SAVE");
                player.sendMessage(ChatColor.GOLD + "  Saves to disk config.yml and saves.yml");
                player.sendMessage(ChatColor.GOLD + "/denizen VERSION");
                player.sendMessage(ChatColor.GOLD + "  Displays version and build of Denizen plugin");
                player.sendMessage(ChatColor.GOLD + "/denizen DEBUG");
                player.sendMessage(ChatColor.GOLD + "  Logs debugging information for reporting problems");
                player.sendMessage(ChatColor.GOLD + "/denizen SCHEDULE");
                player.sendMessage(ChatColor.GOLD + "  Forces the Denizens to check their schedules");   
            }

            else if (args[1].equalsIgnoreCase("npc")) {

                player.sendMessage(ChatColor.GOLD + "------- Denizen NPC Commands -------");
                player.sendMessage(ChatColor.GOLD + "");
                player.sendMessage(ChatColor.GOLD + "/denizen INFO");
                player.sendMessage(ChatColor.GOLD + "  Shows the config nodes for the Denizen NPC");
                player.sendMessage(ChatColor.GOLD + "/denizen ASSIGN [PRIORITY] [SCRIPT NAME]");
                player.sendMessage(ChatColor.GOLD + "  Assigns a script and priority for the Denizen NPC");
                player.sendMessage(ChatColor.GOLD + "/denizen UNASSIGN [SCRIPT NAME]");
                player.sendMessage(ChatColor.GOLD + "  Unassigns a script from the Denizen NPC");
                player.sendMessage(ChatColor.GOLD + "/denizen TRIGGER TOGGLE|LIST [TRIGGER NAME]");
                player.sendMessage(ChatColor.GOLD + "  Toggles triggers for a Denizen NPC");
                player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK LOCATION|BLOCK [Name]");
                player.sendMessage(ChatColor.GOLD + "  Set bookmarks the Denizens. Use /denizen HELP BOOKMARK");
                player.sendMessage(ChatColor.GOLD + "/denizen SCHEDULE");
                player.sendMessage(ChatColor.GOLD + "  Clears current Activity and forces a schedule check");   }

            else if (args[1].equalsIgnoreCase("bookmark")) {

                player.sendMessage(ChatColor.GOLD + "------- Denizen Bookmark Commands -------");
                player.sendMessage(ChatColor.GOLD + "");
                player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK LOCATION [Location Name]");
                player.sendMessage(ChatColor.GOLD + "  Saves the location you are in to the Denizen for reference");
                player.sendMessage(ChatColor.GOLD + "  with Script commands such as MOVETO, SPAWN and REMEMBER");
                player.sendMessage(ChatColor.GOLD + "/denizen BOOKMARK BLOCK [Block Name]");
                player.sendMessage(ChatColor.GOLD + "  Sets a bookmark for the block that is in your crosshairs");
                player.sendMessage(ChatColor.GOLD + "  to be referenced to with Script commands such as SWITCH,");
                player.sendMessage(ChatColor.GOLD + "  and CHANGE");   
            }

            return true;
        } 

        // TODO: Fix info command
        // if (args[0].equalsIgnoreCase("info")) {
        //	plugin.getNPCRegistry().showInfo(player, plugin.getNPCRegistry().getDenizen(theNPC));
        //	return true;
        // }

        if (args[0].equalsIgnoreCase("reschedule")) {
            plugin.getSaves().set("Denizen." + theNPC.getName() + ".Active Activity Script", null);
            plugin.getActivityEngine().scheduler(false);
            plugin.getSaves().set("Denizens." + theNPC.getName() + "." + theNPC.getId() + ".Active Activity Script", null);
            player.sendMessage(ChatColor.GREEN + "Reset activities for " + theNPC.getName() + "/" + theNPC.getId() + " and rescheduled.");
            return true;
        }

        if (args[0].equalsIgnoreCase("test")) {
            plugin.getDebugger().log(plugin.getScripts().saveToString());
            return true;
        }
        return false;	
    }

    */
    

    @net.citizensnpcs.command.Command(
            aliases = { "npc" }, usage = "pushable (-r) (--delay #)", desc = "Makes a NPC pushable.",
            flags = "r", modifiers = { "pushable", "push", "pu" }, min = 1, max = 2, permission = "npc.pushable")
    @net.citizensnpcs.command.Requirements(selected = true, ownership = true)
    public void pushable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(PushableTrait.class)) npc.addTrait(PushableTrait.class);
        PushableTrait trait = npc.getTrait(PushableTrait.class);
        if (args.hasFlag('r')) {
            trait.setReturnable(true);
            Messaging.send(sender, ChatColor.GREEN + npc.getName() + " will return when being pushed.");
        } else if (args.hasValueFlag("delay")) {
            if (args.getFlag("delay").matches("\\d+")) {
                trait.setDelay(Integer.valueOf(args.getFlag("delay")));
                Messaging.send(sender, "Return delay set to " + args.getFlag("delay") + " seconds.");
            } else Messaging.send(sender, ChatColor.RED + "Delay must be a valid number of seconds!");
        } else trait.toggle();
        Messaging.send(sender, ChatColor.YELLOW + npc.getName() + (trait.isPushable() ? "is" : "is not") + " currently pushable" +
                (trait.isReturnable() ? " and will return when pushed after " + trait.getDelay() + " seconds." : "."));
    }


    @net.citizensnpcs.command.Command(
            aliases = { "npc" }, usage = "assignment --set [assignment name] (-r)", 
            desc = "Controls the assignment for an NPC.", flags = "r", modifiers = { "assignment", "assign", "as" },
            min = 1, max = 3, permission = "npc.assign")
    @net.citizensnpcs.command.Requirements(selected = true, ownership = true)
    public void assignment(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(AssignmentTrait.class)) npc.addTrait(AssignmentTrait.class);
        Player player = null;
        if (sender instanceof Player) player = (Player) sender;
        AssignmentTrait trait = npc.getTrait(AssignmentTrait.class);
        if (args.hasValueFlag("set")) {
            if (trait.setAssignment(args.getFlag("set"), player)) Messaging.send(sender, ChatColor.GREEN + "Assignment set.");
            else Messaging.send(sender, ChatColor.RED + "Invalid assignment! Has the script sucessfully loaded?");
            return;
        } else if (args.hasFlag('r')) {
            trait.removeAssignment(player);
            Messaging.send(sender,  ChatColor.YELLOW + "Assignment removed.");
            return;
        }
        trait.describe(sender, args.getInteger(1, 1));
    }


    @net.citizensnpcs.command.Command(
            aliases = { "npc" }, usage = "trigger --name [trigger name] --cooldown [seconds] (-t)", 
            desc = "Controls the various triggers for an NPC.", flags = "t", modifiers = { "trigger", "tr" },
            min = 1, max = 3, permission = "npc.trigger")
    @net.citizensnpcs.command.Requirements(selected = true, ownership = true)
    public void trigger(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(TriggerTrait.class)) npc.addTrait(TriggerTrait.class);
        TriggerTrait trait = npc.getTrait(TriggerTrait.class);
        if (args.hasValueFlag("name")) {
            if (args.hasFlag('t')) trait.toggleTrigger(args.getFlag("name"));
            if (args.hasValueFlag("cooldown")) trait.setLocalCooldown(args.getFlag("Name"), args.getFlagInteger("cooldown"));
            Messaging.send(sender, ChatColor.YELLOW + args.getFlag("name").toUpperCase() + " trigger " + (trait.isEnabled(args.getFlag("name")) ? "is" : "is not") + " currently enabled" +
                    (trait.isEnabled(args.getFlag("name")) ?  "with a cooldown of '" + trait.getCooldownDuration(args.getFlag("name")) + "' seconds."  : "."));
            return;
        }
        trait.describe(sender, args.getInteger(1, 1));
    }


    @net.citizensnpcs.command.Command(
            aliases = { "npc" }, usage = "nickname --set [nickname]", 
            desc = "Gives the NPC a nickname, used with a Denizen-compatible Speech Engine.", modifiers = { "nickname", "nick", "ni" },
            min = 1, max = 3, permission = "npc.nickname")
    @net.citizensnpcs.command.Requirements(selected = true, ownership = true)
    public void nickname(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(NicknameTrait.class)) npc.addTrait(NicknameTrait.class);
        NicknameTrait trait = npc.getTrait(NicknameTrait.class);
        if (args.hasValueFlag("set")) {
            trait.setNickname(args.getFlag("set"));
            Messaging.send(sender, ChatColor.GREEN + "Nickname set.");
            return;
        } else if (args.hasFlag('r')) {
            trait.setNickname("");
            Messaging.send(sender, ChatColor.YELLOW + "Nickname removed.");
            return;
        }
        if (trait.hasNickname())
            Messaging.send(sender, ChatColor.YELLOW + npc.getName() + "'s nickname is '" + trait.getNickname() + "'.");
        else Messaging.send(sender, ChatColor.YELLOW + npc.getName() + " does not have a nickname!");
    }


    @net.citizensnpcs.command.Command(
            aliases = { "npc" }, usage = "health --set # (-r)", 
            desc = "Sets the max health for an NPC.", modifiers = { "health", "he", "hp" },
            min = 1, max = 3, permission = "npc.health", flags = "r")
    @net.citizensnpcs.command.Requirements(selected = true, ownership = true)
    public void health(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(HealthTrait.class)) npc.addTrait(HealthTrait.class);
        HealthTrait trait = npc.getTrait(HealthTrait.class);
        if (args.hasValueFlag("set")) {
            trait.setMaxHealth(args.getFlagInteger("set"));
            trait.setHealth();
            Messaging.send(sender, ChatColor.GREEN + "Max health set.");
            return;
        } else if (args.hasFlag('r')) {
            trait.setHealth();
            Messaging.send(sender, ChatColor.GREEN + npc.getName() + "'s health reset to " + trait.getMaxHealth() + ".");
        }
        Messaging.send(sender, ChatColor.YELLOW + npc.getName() + "'s health is '" + trait.getHealth() + "/" + trait.getMaxHealth() + "'.");
    }


    @net.citizensnpcs.command.Command(
            aliases = { "denizen" }, usage = "debug", 
            desc = "Toggles debug mode for Denizen.", modifiers = { "debug", "de", "db" },
            min = 1, max = 3, permission = "denizen.debug", flags = "s")
    public void debug(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Denizen denizen = (Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen");

        if (args.hasFlag('s')) {
            if (!denizen.getDebugger().debugMode) denizen.getDebugger().toggle();
            denizen.getDebugger().showStackTraces = !denizen.getDebugger().showStackTraces;
        } else denizen.getDebugger().toggle();

        Messaging.send(sender, ChatColor.YELLOW + "Denizen debugger is " + (denizen.getDebugger().debugMode ? 
                ((denizen.getDebugger().showStackTraces) ? "enabled and showing stack-traces." : "enabled.") : "disabled."));
    }    

    
    @net.citizensnpcs.command.Command(
            aliases = { "denizen" }, usage = "version", 
            desc = "Shows the currently loaded version of Denizen.", modifiers = { "version"},
            min = 1, max = 3, permission = "denizen.basic")
    public void version(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Messaging.send(sender, ChatColor.YELLOW + " _/_ _  ._  _ _  ");
        Messaging.send(sender, ChatColor.YELLOW + "(/(-/ )/ /_(-/ ) " + ChatColor.GRAY + " scriptable NPCs"); 
        Messaging.send(sender, "");
        Messaging.send(sender, ChatColor.GRAY + "by: " + ChatColor.WHITE + "aufdemrand");
        Messaging.send(sender, ChatColor.GRAY + "version: "+ ChatColor.WHITE + Denizen.versionTag);
    }    

    
    @net.citizensnpcs.command.Command(
            aliases = { "denizen" }, usage = "save", 
            desc = "Saves the current state of Denizen/saves.yml.", modifiers = { "save"},
            min = 1, max = 3, permission = "denizen.basic", flags = "s")
    public void save(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        ((Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen")).saveSaves();
        
        Messaging.send(sender, ChatColor.GREEN + "Denizen/saves.yml saved to disk from memory.");
    }

    
    @net.citizensnpcs.command.Command(
            aliases = { "denizen" }, usage = "reload (saves|config|scripts) (-a)", 
            desc = "Saves the current state of Denizen/saves.yml.", modifiers = { "save"},
            min = 1, max = 3, permission = "denizen.basic", flags = "a")
    public void reload(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Denizen denizen = (Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen");
        
        if (args.getString(1).equalsIgnoreCase("saves")) {
            denizen.reloadSaves();
            Messaging.send(sender, ChatColor.GREEN + "Denizen/saves.yml reloaded from disk to memory.");
            return;
        } else if (args.getString(1).equalsIgnoreCase("config")) {
            denizen.reloadConfig();
            Messaging.send(sender, ChatColor.GREEN + "Denizen/config.yml reloaded from disk to memory.");
            return;
        } else if (args.getString(1).equalsIgnoreCase("scripts")) {
            denizen.reloadScripts();
            Messaging.send(sender, ChatColor.GREEN + "Denizen/scripts/... reloaded from disk to memory.");
            return;
        } else if (args.hasFlag('a')) {
            denizen.reloadSaves();
            denizen.reloadConfig();
            denizen.reloadScripts();
            Messaging.send(sender, ChatColor.GREEN + "Denizen/saves.yml, Denizen/config.yml, and Denizen/scripts/... reloaded from disk to memory.");
            return;
        }
        throw new CommandException();
    }
    
}


