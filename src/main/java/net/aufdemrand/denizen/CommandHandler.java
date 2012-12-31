package net.aufdemrand.denizen;

import java.util.Set;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.ConstantsTrait;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.npc.traits.PushableTrait;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.command.Command;
import net.citizensnpcs.command.CommandContext;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Messaging;
import net.citizensnpcs.util.Paginator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler {

	private final Citizens plugin;

	public CommandHandler(Citizens plugin) {
		this.plugin = plugin;
	}


	/*
	 * PUSHABLE
	 */

	@net.citizensnpcs.command.Command(
			aliases = { "npc" }, usage = "pushable -t (-r) (--delay #)", desc = "Makes a NPC pushable.",
			flags = "rt", modifiers = { "pushable", "push" }, min = 1, max = 2, permission = "npc.pushable")
	@net.citizensnpcs.command.Requirements(selected = true, ownership = true)
	public void pushable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (!npc.hasTrait(PushableTrait.class)) npc.addTrait(PushableTrait.class);
		PushableTrait trait = npc.getTrait(PushableTrait.class);

		if (args.hasFlag('r') && !args.hasFlag('t')) {
			trait.setReturnable(!trait.isReturnable());
			Messaging.send(sender, ChatColor.YELLOW + npc.getName() + (trait.isReturnable() ? " will " : " will not ") + "return when being pushed" 
					+ (!trait.isReturnable() || trait.isPushable() ? "." : ", but is currently not pushable."));
			return;

		} else if (args.hasValueFlag("delay") && !args.hasFlag('t')) {
			if (args.getFlag("delay").matches("\\d+") && args.getFlagInteger("delay") > 0) {
				trait.setDelay(Integer.valueOf(args.getFlag("delay")));
				trait.setReturnable(true);
				Messaging.send(sender, ChatColor.YELLOW + npc.getName() + " will return after '" + args.getFlag("delay") + "' seconds"
						+ (trait.isPushable() ? "." : ", but is currently not pushable."));
				return;
			} else {
				Messaging.send(sender, ChatColor.RED + "Delay must be a valid number of seconds!");
				return;
			}

		} else if (args.hasFlag('t') && !args.hasValueFlag("delay") && !args.hasFlag('r')) {
			trait.toggle();
			Messaging.send(sender, ChatColor.YELLOW + npc.getName() + (trait.isPushable() ? " is" : " is not") + " currently pushable" +
					(trait.isReturnable() && trait.isPushable() ? " and will return when pushed after '" + trait.getDelay() + "' seconds." : "."));
			return;

		} else if (args.hasFlag('t')) {
			trait.toggle();
			if (args.hasFlag('r')) trait.setReturnable(true);
			if (args.hasValueFlag("delay") && args.getFlag("delay").matches("\\d+") && args.getFlagInteger("delay") > 0)
				trait.setDelay(args.getFlagInteger("delay"));
			Messaging.send(sender, ChatColor.YELLOW + npc.getName() + (trait.isPushable() ? " is" : " is not") + " currently pushable" +
					(trait.isReturnable() && trait.isPushable() ? " and will return when pushed after '" + trait.getDelay() + "' seconds." : "."));
			return;

		} else if (args.length() > 2) {
			Messaging.send(sender, "");
			Messaging.send(sender, "<f>Use '-t' to toggle pushable state. <b>Example: /npc pushable -t");
			Messaging.send(sender, "<f>To have the NPC return when pushed, use '-r'.");
			Messaging.send(sender, "<f>Change the return delay with '--delay #'.");
			Messaging.send(sender, "");
		}

		Messaging.send(sender, ChatColor.YELLOW + npc.getName() + (trait.isPushable() ? " is" : " is not") + " currently pushable" +
				(trait.isReturnable() ? " and will return when pushed after " + trait.getDelay() + " seconds." : "."));
	}


	/*
	 * CONSTANTS
	 */
	@net.citizensnpcs.command.Command(
			aliases = { "npc" }, usage = "constant --set|remove name --value constant value", 
			desc = "Views/adds/removes NPC string constants.", flags = "r", modifiers = { "constants", "constant", "const" },
			min = 1, max = 3, permission = "npc.constants")
	@net.citizensnpcs.command.Requirements(selected = true, ownership = true)
	public void constants(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (!npc.hasTrait(ConstantsTrait.class)) npc.addTrait(ConstantsTrait.class);
		ConstantsTrait trait = npc.getTrait(ConstantsTrait.class);
		if (args.hasValueFlag("set")) {
			if (!args.hasValueFlag("value")) throw new CommandException("--SET requires use of the '--VALUE \"constant value\"' argument."); 
			trait.setConstant(args.getFlag("set"), args.getFlag("value"));
			Messaging.send(sender, ChatColor.YELLOW + npc.getName() + " has added constant '" + args.getFlag("set") + "'.");
			return;

		} else if (args.hasValueFlag("remove")) {
			trait.removeConstant(args.getFlag("remove"));
			Messaging.send(sender, ChatColor.YELLOW + npc.getName() + " has removed constant '" + args.getFlag("remove") + "'.");
			return;

		} else if (args.length() > 2 && args.getInteger(1, 0) < 1) {
			Messaging.send(sender, "");
			Messaging.send(sender, "<f>Use '--set name' to add/set a new NPC-specific constant.");
			Messaging.send(sender, "<f>Must also specify '--value \"constant value\"'.");
			Messaging.send(sender, "<b>Example: /npc constant --set constant_1 --value \"test value\"");
			Messaging.send(sender, "<f>Remove NPC-specific constants with '--remove name'");
			Messaging.send(sender, "<f>Note: Constants set will override any specified in an");
			Messaging.send(sender, "<f>assignment. Constants specified in assignments cannot be");
			Messaging.send(sender, "<f>removed with this command.");
			Messaging.send(sender, "");
			return;
		}

		trait.describe(sender, args.getInteger(1, 1));
	}


	/*
	 * ASSIGNMENT
	 */
	@net.citizensnpcs.command.Command(
			aliases = { "npc" }, usage = "assignment --set assignment_name (-r)", 
			desc = "Controls the assignment for an NPC.", flags = "r", modifiers = { "assignment", "assign" },
			min = 1, max = 3, permission = "npc.assign")
	@net.citizensnpcs.command.Requirements(selected = true, ownership = true)
	public void assignment(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (!npc.hasTrait(AssignmentTrait.class)) npc.addTrait(AssignmentTrait.class);
		Player player = null;
		if (sender instanceof Player) player = (Player) sender;
		AssignmentTrait trait = npc.getTrait(AssignmentTrait.class);

		if (args.hasValueFlag("set")) {
			if (trait.setAssignment(args.getFlag("set"), player)) 
				Messaging.send(sender, ChatColor.YELLOW + npc.getName() + " has been assigned '" + trait.getAssignment() + "'.");
			else Messaging.send(sender, ChatColor.RED + "Invalid assignment! Has the script sucessfully loaded, or has it been mispelled?");
			return;

		} else if (args.hasFlag('r')) {
			trait.removeAssignment(player);
			Messaging.send(sender,  ChatColor.YELLOW + npc.getName() + "'s assignment has been removed.");
			return;

		} else if (args.length() > 2 && args.getInteger(1, 0) < 1) {
			Messaging.send(sender, "");
			Messaging.send(sender, "<f>Use '--set name' to set an assignment script to this NPC.");
			Messaging.send(sender, "<b>Example: /npc assignment --set \"Magic Shop\"");
			Messaging.send(sender, "<f>Remove an assignment with '-r'.");
			Messaging.send(sender, "<f>Note: Assigning a script will fire an 'On Assignment:' action.");
			Messaging.send(sender, "");
			return;
		}

		trait.describe(sender, args.getInteger(1, 1));
	}


	/*
	 * TRIGGER
	 */
	@net.citizensnpcs.command.Command(
			aliases = { "npc" }, usage = "trigger [--name trigger name] [(--cooldown [seconds])|(--radius [radius])|(-t)]", 
			desc = "Controls the various triggers for an NPC.", flags = "t", modifiers = { "trigger", "tr" },
			min = 1, max = 3, permission = "npc.trigger")
	@net.citizensnpcs.command.Requirements(selected = true, ownership = true)
	public void trigger(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (!npc.hasTrait(TriggerTrait.class)) npc.addTrait(TriggerTrait.class);
		TriggerTrait trait = npc.getTrait(TriggerTrait.class);
		if (args.hasValueFlag("name")) {
			if (args.hasFlag('t')) trait.toggleTrigger(args.getFlag("name"));
			if (args.hasValueFlag("cooldown")) trait.setLocalCooldown(args.getFlag("Name"), args.getFlagDouble("cooldown"));
			if (args.hasValueFlag("radius")) {
				trait.setLocalRadius(args.getFlag("Name"), args.getFlagInteger("radius"));
				Messaging.send(sender, ChatColor.YELLOW + args.getFlag("name").toUpperCase() + " trigger radius now " + args.getFlag("radius") + ".");
			}
			Messaging.send(sender, ChatColor.YELLOW + args.getFlag("name").toUpperCase() + " trigger " + (trait.isEnabled(args.getFlag("name")) ? "is" : "is not") + " currently enabled" +
					(trait.isEnabled(args.getFlag("name")) ?  "with a cooldown of '" + trait.getCooldownDuration(args.getFlag("name")) + "' seconds."  : "."));
			return;
			
		} else if (args.length() > 2 && args.getInteger(1, 0) < 1) {
			Messaging.send(sender, "<f>Usage: /npc trigger [--name trigger_name] [(--cooldown #)|(--radius #)|(-t)]");
			Messaging.send(sender, "");
			Messaging.send(sender, "<f>Use '--name trigger_name' to specify a specific trigger, and '-t' to toggle.");
			Messaging.send(sender, "<b>Example: /npc trigger --name damage -t");
			Messaging.send(sender, "<f>You may also use '--cooldown #' to specify a new cooldown time, and '--radius #' to specify a specific radius, when applicable.");
			Messaging.send(sender, "");
			return;
		}

		trait.describe(sender, args.getInteger(1, 1));
	}


	/*
	 * NICKNAME
	 */
	@net.citizensnpcs.command.Command(
			aliases = { "npc" }, usage = "nickname [--set nickname]", 
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


	/*
	 * HEALTH
	 */
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


	/*
	 * DENIZEN DEBUG
	 */
	@net.citizensnpcs.command.Command(
			aliases = { "denizen" }, usage = "debug", 
			desc = "Toggles debug mode for Denizen.", modifiers = { "debug", "de", "db" },
			min = 1, max = 3, permission = "denizen.debug", flags = "s")
	public void debug(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (args.hasFlag('s')) {
			if (!dB.debugMode) dB.toggle();
			dB.showStackTraces = !dB.showStackTraces;
		} else dB.toggle();

		Messaging.send(sender, ChatColor.YELLOW + "Denizen debugger is " + (dB.debugMode ? 
				((dB.showStackTraces) ? "enabled and showing stack-traces." : "enabled.") : "disabled."));
	}    


	/*
	 * DENIZEN VERSION
	 */
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


	/*
	 * DENIZEN SAVE
	 */
	@net.citizensnpcs.command.Command(
			aliases = { "denizen" }, usage = "save", 
			desc = "Saves the current state of Denizen/saves.yml.", modifiers = { "save" },
			min = 1, max = 3, permission = "denizen.basic", flags = "s")
	public void save(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		((Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen")).saveSaves();

		Messaging.send(sender, ChatColor.GREEN + "Denizen/saves.yml saved to disk from memory.");
	}


	/*
	 * DENIZEN LISTENER
	 */
	@net.citizensnpcs.command.Command(
			aliases = { "denizen" }, usage = "listener (--player) --id listener_id --report|cancel|finish", 
			desc = "Checks/cancels/finishes listeners in progress.", modifiers = { "listener" },
			min = 1, max = 3, permission = "denizen.basic", flags = "s")
	public void listener(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		Denizen denizen = ((Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen"));

		Player player = null;
		if (sender instanceof Player) player = (Player) sender;

		if (args.hasValueFlag("player"))
			player = aH.getPlayerFrom(args.getFlag("player"));

		if (player == null) throw new CommandException("Specified player not online or not found!");

		if (args.hasValueFlag("report")) {
			for (AbstractListener quest : denizen.getListenerRegistry().getListenersFor(player).values())
				if (quest.getListenerId().equalsIgnoreCase(args.getFlag("report")))
					Messaging.send(sender, quest.report());
			return;

		} else if (args.hasValueFlag("cancel")) {
			for (AbstractListener quest : denizen.getListenerRegistry().getListenersFor(player).values())
				if (quest.getListenerId().equalsIgnoreCase(args.getFlag("cancel"))) {

					Messaging.send(sender, ChatColor.GREEN + "Cancelling '" + quest.getListenerId() + "' for " + player.getName() + ".");
					quest.cancel();
				}
			return;

		} else if (args.hasValueFlag("finish")) {
			for (AbstractListener quest : denizen.getListenerRegistry().getListenersFor(player).values())
				if (quest.getListenerId().equalsIgnoreCase(args.getFlag("finish"))) {
					Messaging.send(sender, ChatColor.GREEN + "Force-finishing '" + quest.getListenerId() + "' for " + player.getName() + ".");
					quest.finish();
				}
			return;			

		} else if (args.length() > 2 && args.getInteger(1, 0) < 1) {
			Messaging.send(sender, "");
			Messaging.send(sender, "<f>Use '--report|cancel|finish id' to modify/view a specific quest listener.");
			Messaging.send(sender, "<b>Example: /npc listener --report \"Journey 1\"");
			Messaging.send(sender, "");
			return;
		}

		Paginator paginator = new Paginator();
		paginator.header("Active quest listeners for " + player.getName() + ":");
		paginator.addLine("<e>Key: <a>Type  <b>ID");
		if (denizen.getListenerRegistry().getListenersFor(player).isEmpty())
			paginator.addLine("None.");
		else for (AbstractListener quest : denizen.getListenerRegistry().getListenersFor(player).values())
			paginator.addLine("<a>" + quest.getListenerType() + "  <b>" + quest.getListenerId());

		paginator.sendPage(sender, args.getInteger(1, 1));
		return;

	}


	/*
	 * DENIZEN RELOAD 
	 */
	@Command ( aliases = { "denizen" }, usage = "reload (saves|config|scripts) (-a)", 
			desc = "Reloads various Denizen YML files from disk to memory.", modifiers = { "reload" },
			min = 1, max = 3, permission = "denizen.basic", flags = "a" )
	public void reload(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		Denizen denizen = (Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen");

		// Get reload type
		if (args.hasFlag('a')) {
			denizen.reloadSaves();
			denizen.reloadConfig();
			denizen.reloadScripts();
			Messaging.send(sender, ChatColor.GREEN + "Denizen/saves.yml, Denizen/config.yml, and Denizen/scripts/... reloaded from disk to memory.");
			return;
		}
		// Reload a specific item
		if (args.length() > 2) {
			if  (args.getString(1).equalsIgnoreCase("saves")) {
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
			}
		}

		Messaging.send(sender, "");
		Messaging.send(sender, "<f>Specify which parts to reload. Valid options are: SAVES, CONFIG, SCRIPTS");
		Messaging.send(sender, "<b>Example: /denizen reload scripts");
		Messaging.send(sender, "<f>Use '-a' to reload all parts.");
		Messaging.send(sender, "");
		return;
	}


	/*
	 * DENIZEN SCRIPTS
	 */
	@net.citizensnpcs.command.Command(
			aliases = { "denizen" }, usage = "scripts (--type assignment|task|activity|interact) (--filter string)", 
			desc = "Lists currently loaded dScripts.", modifiers = { "scripts" },
			min = 1, max = 4, permission = "denizen.basic")
	public void scripts(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		Denizen denizen = (Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen");
		// Fill arguments
		String type = null;   if (args.hasValueFlag("type"))   type = args.getFlag("type");
		String filter = null; if (args.hasValueFlag("filter")) filter = args.getFlag("filter");
		// Get script names from the scripts.yml in memory
		Set<String> scripts = denizen.getScripts().getKeys(false);
		// New Paginator to display script names
		Paginator paginator = new Paginator().header("Scripts");
		paginator.addLine("<e>Key: <a>Type  <b>Name");
		// Add scripts to Paginator
		for (String script : scripts) {
			if (denizen.getScripts().contains(script + ".TYPE")) {
				// If a --type has been specified... 
				if (type != null) {
					if (denizen.getScripts().getString(script + ".TYPE").equalsIgnoreCase(type))
						if (filter != null) { 
							if (script.contains(filter.toUpperCase()))
								paginator.addLine("<a>" + denizen.getScripts().getString(script + ".TYPE").toUpperCase().substring(0, 4) + "  <b>" + script);
						}
						else paginator.addLine("<a>" + denizen.getScripts().getString(script + ".TYPE").toUpperCase().substring(0, 4) + "  <b>" + script);
					// If a --filter has been specified...
				} else if (filter != null) { 
					if (script.contains(filter.toUpperCase()))
						paginator.addLine("<a>" + denizen.getScripts().getString(script + ".TYPE").toUpperCase().substring(0, 4) + "  <b>" + script);
				} else paginator.addLine("<a>" + denizen.getScripts().getString(script + ".TYPE").toUpperCase().substring(0, 4) + "  <b>" + script);
			}
		}
		// Send the contents of the Paginator to the Player (or Console)
		if (!paginator.sendPage(sender, args.getInteger(1, 1)))
			throw new CommandException(Messages.COMMAND_PAGE_MISSING);
	}

}


