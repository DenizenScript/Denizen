package net.aufdemrand.denizen;

import java.util.Set;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.ConstantsTrait;
import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.npc.traits.NameplateTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.npc.traits.PushableTrait;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.ScriptRepo;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.util.Messaging;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Paginator;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.JSONException;


public class CommandHandler {

	private final Citizens plugin;

	public CommandHandler(Citizens plugin) {
		this.plugin = plugin;
	}


    /**
     * <p>Controls a NPCs Pushable Trait. When a NPC is 'pushable', the NPC
     * will move out of the way when colliding with another LivingEntity.</p>
     *
     * <p>Pushable NPCs have 3 different settings available: Toggled, Returnable, and Delay</p>
     *
     * <p>When an NPCs Pushable Trait is toggled off, it will not function. Entities which
     * collide may occupy the same space. To toggle pushable on or off, use the Bukkit command:</br>
     * <code>/npc pushable -t</code>
     * </p>
     *
     * <p>Setting the NPC as 'returnable' will automatically navigate the NPC back to
     * its original location after a specified delay. If not returnable, NPCs will retain
     * their position after being moved.</br>
     * <code>/npc pushable -r</code>
     * </p>
     *
     * <p>To change the delay of a returnable NPC, use the following Bukkit Command,
     * specifying the number of seconds in which the delay should be.</br>
     * <code>/npc pushable --delay #</code></p>
     *
     * <p>It is possible to use multiple arguments at once. For example:
     * <code>/npc pushable -t -r --delay 10</code></p>
     *
     * <p>Note: If allowed to move in undesirable areas, the NPC may be un-returnable
     * if the navigator cancels navigation due to being stuck. Care should be taken
     * to ensure a safe area around the NPC.</p>
     *
     */
	@Command(
			aliases = { "npc" }, usage = "pushable -t (-r) (--delay #)", desc = "Makes a NPC pushable.",
			flags = "rt", modifiers = { "pushable", "push" }, min = 1, max = 2, permission = "npc.pushable")
	@Requirements(selected = true, ownership = true)
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


    /**
     * <p>Configures a NPCs constants. Uses Denizen's ConstantTrait to keep track of NPC-specific constants.
     * This provides seamless integration with dScript Assignment Script 'Default Constants' in which string
     * variables can be stored and retrieved with the use of Replaceable FLAGs, or API. Constants set at
     * the NPC level override any constants from the NPCs Assignment Script.</p>
     *
     * <p>Constants may be used in several ways: Setting, Removing, and Viewing</p>
     *
     * <p>To set a constant, all that is required is a name and value. Use the Bukkit command in the
     * following manner:</br>
     * <code>/npc constant --set constant_name --value 'multi word value'</code></p>
     *
     * <p>Removing a constant from an NPC only requires a name. Note: It is not possible to remove a
     * constant set by the NPCs Assignment Script, except by modifying the script itself.
     * <code>/npc constant --remove constant_name</code></p>
     *
     * <p>Viewing constants is easy, just use <code>/npc constant #</code>, specifying a page number.
     * Constants which have been overridden in the Assignment Constants list are formatted with a
     * 'strike-through' to indicate this case.
     *
     * <p>To reference a constant value, use the Denizen Replaceable TAG format <code>&#60;CONS:name></code>.
     * Constants may also have other TAGs in their value, which will be 'replaced' whenever the constant
     * is called to be used.</p>
     *
     */
	@Command(
			aliases = { "npc" }, usage = "constant --set|remove name --value constant value", 
			desc = "Views/adds/removes NPC string constants.", flags = "r", modifiers = { "constants", "constant", "cons" },
			min = 1, max = 3, permission = "npc.constants")
	@Requirements(selected = true, ownership = true)
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
	@Command(
			aliases = { "npc" }, usage = "assignment --set assignment_name (-r)", 
			desc = "Controls the assignment for an NPC.", flags = "r", modifiers = { "assignment", "assign" },
			min = 1, max = 3, permission = "npc.assign")
	@Requirements(selected = true, ownership = true)
	public void assignment(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (!npc.hasTrait(AssignmentTrait.class)) npc.addTrait(AssignmentTrait.class);
		Player player = null;
		if (sender instanceof Player) player = (Player) sender;
		AssignmentTrait trait = npc.getTrait(AssignmentTrait.class);

		if (args.hasValueFlag("set")) {
			if (trait.setAssignment(args.getFlag("set").replace("\"", ""), player))
				if (trait.hasAssignment())
                Messaging.send(sender, ChatColor.YELLOW + npc.getName() + "'s assignment is now: '" + trait.getAssignment().getName() + "'.");
                else Messaging.send(sender, ChatColor.YELLOW + npc.getName() + "'s assignment was not able to be set.");
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
	@Command(
			aliases = { "npc" }, usage = "trigger [trigger name] [(--cooldown [seconds])|(--radius [radius])|(-t)]",
			desc = "Controls the various triggers for an NPC.", flags = "t", modifiers = { "trigger", "tr" },
			min = 1, max = 3, permission = "npc.trigger")
	@Requirements(selected = true, ownership = true)
	public void trigger(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (!npc.hasTrait(TriggerTrait.class)) npc.addTrait(TriggerTrait.class);
		TriggerTrait trait = npc.getTrait(TriggerTrait.class);
		if ((args.hasValueFlag("name") || (args.argsLength() > 1 && (args.getJoinedStrings(1) != null) && !args.getString(1).matches("\\d+")))) {
            // Get the name of the trigger
            String triggerName;
            if (args.hasValueFlag("name")) triggerName = args.getFlag("name");
            else triggerName = args.getJoinedStrings(1);
            // Check to make sure trigger exists
            if (DenizenAPI.getCurrentInstance().getTriggerRegistry().get(triggerName) == null) {
                Messaging.send(sender, ChatColor.RED + "'" + triggerName.toUpperCase() + "' trigger does not exist.");
                Messaging.send(sender, "<f>Usage: /npc trigger [trigger_name] [(--cooldown #)|(--radius #)|(-t)]");
                Messaging.send(sender, "");
                Messaging.send(sender, "<f>Use '--name trigger_name' to specify a specific trigger, and '-t' to toggle.");
                Messaging.send(sender, "<b>Example: /npc trigger --name damage -t");
                Messaging.send(sender, "<f>You may also use '--cooldown #' to specify a new cooldown time, and '--radius #' to specify a specific radius, when applicable.");
                Messaging.send(sender, "");
                return;
            }
            // If toggling
			if (args.hasFlag('t')) trait.toggleTrigger(triggerName);
			// If setting cooldown
            if (args.hasValueFlag("cooldown"))
                trait.setLocalCooldown(triggerName, args.getFlagDouble("cooldown"));
			// If specifying radius
            if (args.hasValueFlag("radius")) {
				trait.setLocalRadius(triggerName, args.getFlagInteger("radius"));
				Messaging.send(sender, ChatColor.YELLOW + triggerName.toUpperCase() + " trigger radius now " + args.getFlag("radius") + ".");
			}
            // Show current status of the trigger
			Messaging.send(sender, ChatColor.YELLOW + triggerName.toUpperCase() + " trigger " + (trait.isEnabled(triggerName) ? "is" : "is not") + " currently enabled" +
					(trait.isEnabled(triggerName) ?  " with a cooldown of '" + trait.getCooldownDuration(triggerName) + "' seconds."  : "."));
			return;
        }

		trait.describe(sender, args.getInteger(1, 1));
	}


	/*
	 * NICKNAME
	 */
	@Command(
			aliases = { "npc" }, usage = "nickname [--set nickname]", 
			desc = "Gives the NPC a nickname, used with a Denizen-compatible Speech Engine.", modifiers = { "nickname", "nick", "ni" },
			min = 1, max = 3, permission = "npc.nickname")
	@Requirements(selected = true, ownership = true)
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
	 * NAMEPLATE / NAMETAG
	 */
	@Command(
			aliases = { "npc" }, usage = "nameplate [--color color]", 
			desc = "Sets the namepalte color of the NPC.", modifiers = { "nameplate", "nametag", "np", "nt" },
			min = 1, max = 3, permission = "npc.nameplate")
	@Requirements(selected = true, ownership = true)
	public void nameplate(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (!npc.hasTrait(NameplateTrait.class)) npc.addTrait(NameplateTrait.class);
		NameplateTrait trait = npc.getTrait(NameplateTrait.class);
		
		if (args.hasValueFlag("color")) {
			String colorString = args.getFlag("color").toUpperCase();
			ChatColor color = null;
			
			try {
				color = ChatColor.valueOf(colorString);
			} catch( Exception e)  {}
			
			if(color != null) {
				trait.setColor(color);
				Messaging.send(sender, ChatColor.GREEN + "Nameplate color set.");
			} else {
				Messaging.send(sender, ChatColor.RED + "No color with name '" + colorString + "' found!");
			}
			
			return;
		} else if( args.hasFlag('r') ) {
			trait.setColor(null);
			Messaging.send(sender, ChatColor.GREEN + "Nameplate color reset.");
		}

		if (trait.hasColor())
			Messaging.send(sender, ChatColor.YELLOW + npc.getName() + "'s nameplate color is " + trait.getColor() + trait.getColor().name() + ".");
		else Messaging.send(sender, ChatColor.YELLOW + npc.getName() + " does not have a nameplate color!");
	}


	/*
	 * HEALTH
	 */
	@Command(
			aliases = { "npc" }, usage = "health --set # (-r)", 
			desc = "Sets the max health for an NPC.", modifiers = { "health", "he", "hp" },
			min = 1, max = 3, permission = "npc.health", flags = "rs")
	@Requirements(selected = true, ownership = true)
	public void health(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		if (!npc.hasTrait(HealthTrait.class)) npc.addTrait(HealthTrait.class);
		HealthTrait trait = npc.getTrait(HealthTrait.class);

        boolean showMore = true;

        if (args.hasValueFlag("max")) {
			trait.setMaxhealth(args.getFlagInteger("max"));
			trait.setHealth();
			Messaging.send(sender, ChatColor.GREEN + npc.getName() + "'s health maximum is now " + trait.getMaxhealth() + ".");
            showMore = false;

        } if (args.hasValueFlag("set")) {
                trait.setHealth(args.getFlagInteger("set"));

        } if (args.hasValueFlag("respawndelay")) {
            trait.setRespawnDelay(args.getFlag("respawndelay"));
            Messaging.send(sender, ChatColor.GREEN + npc.getName() + "'s respawn delay now " + trait.getRespawnDelay()
                    + (trait.isRespawnable() ? "." : ", but is not currently auto-respawnable upon death."));
            showMore = false;

        } if (args.hasValueFlag("respawnlocation")) {
            trait.setRespawnLocation(args.getFlag("respawnlocation"));
            Messaging.send(sender, ChatColor.GREEN + npc.getName() + "'s respawn location now " + trait.getRespawnLocationAsString()
                    + (trait.isRespawnable() ? "." : ", but is not currently auto-respawnable upon death."));
            showMore = false;

        } if (args.hasFlag('s')) {
            trait.setRespawnable(!trait.isRespawnable());
            Messaging.send(sender, ChatColor.GREEN + npc.getName() + (trait.isRespawnable()
                    ? " will now auto-respawn on death after " + trait.getRespawnDelay() + " seconds."
                    : " will no longer auto-respawn on death."));
            showMore = false;

        } if (args.hasFlag('a')) {
            trait.animateOnDeath(!trait.animatesOnDeath());
            Messaging.send(sender, ChatColor.GREEN + npc.getName() + (trait.animatesOnDeath()
                    ? " will now animate on death."
                    : " will no longer animate on death."));
            showMore = false;

        } else if (args.hasFlag('r')) {
			trait.setHealth();
			Messaging.send(sender, ChatColor.GREEN + npc.getName() + "'s health reset to " + trait.getMaxhealth() + ".");
            showMore = false;
		}

        if (showMore)
		    Messaging.send(sender, ChatColor.YELLOW + npc.getName() + "'s health is '" + trait.getHealth() + "/" + trait.getMaxhealth() + "'.");
	}


	/*
	 * DENIZEN DEBUG
	 */
	@Command(
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
	@Command(
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
	@Command(
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
	@Command(
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
			ScriptHelper.reloadScripts();
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
                ScriptHelper.reloadScripts();
				Messaging.send(sender, ChatColor.GREEN + "Denizen/scripts/... reloaded from disk to memory.");
				return;
			}
		}

		Messaging.send(sender, "");
		Messaging.send(sender, "<f>Specify which parts to reload. Valid options are: SAVES, CONFIG, SCRIPTS");
		Messaging.send(sender, "<b>Example: /denizen reload scripts");
		Messaging.send(sender, "<f>Use '-a' to reload all parts.");
		Messaging.send(sender, "");
    }


	/*
	 * DENIZEN SCRIPTS
	 */
	@Command(
			aliases = { "denizen" }, usage = "scripts (--type assignment|task|activity|interact) (--filter string)", 
			desc = "Lists currently loaded dScripts.", modifiers = { "scripts" },
			min = 1, max = 4, permission = "denizen.basic")
	public void scripts(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		Denizen denizen = (Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen");
		// Fill arguments
		String type = null;   if (args.hasValueFlag("type"))   type = args.getFlag("type");
		String filter = null; if (args.hasValueFlag("filter")) filter = args.getFlag("filter");
		// Get script names from the scripts.yml in memory
		Set<String> scripts = ScriptRegistry._getScriptNames();
		// New Paginator to display script names
		Paginator paginator = new Paginator().header("Scripts");
		paginator.addLine("<e>Key: <a>Type  <b>Name");
		// Add scripts to Paginator
		for (String script : scripts) {
            ScriptContainer scriptContainer = ScriptRegistry.getScriptContainer(script);
				// If a --type has been specified...
				if (type != null) {
					if (scriptContainer.getType().equalsIgnoreCase(type))
						if (filter != null) {
							if (script.contains(filter.toUpperCase()))
								paginator.addLine("<a>" + scriptContainer.getType().substring(0, 4) + "  <b>" + script);
						}
						else paginator.addLine("<a>" + scriptContainer.getType().substring(0, 4) + "  <b>" + script);
					// If a --filter has been specified...
				} else if (filter != null) {
					if (script.contains(filter.toUpperCase()))
						paginator.addLine("<a>" + scriptContainer.getType().substring(0, 4) + "  <b>" + script);
				} else paginator.addLine("<a>" + scriptContainer.getType().substring(0, 4) + "  <b>" + script);
		}
		// Send the contents of the Paginator to the Player (or Console)
		if (!paginator.sendPage(sender, args.getInteger(1, 1)))
			throw new CommandException(Messages.COMMAND_PAGE_MISSING);
	}

	/*
	 * CITIZENS SCRIPT REPO
	 */
	@Command(
			aliases = {"denizen"}, usage = "repo (info|search|load)",
			desc = "Repo commands.", modifiers = {"info", "search", "load"},
			min = 1, permission = "denizen.repo")
	public void repo(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
		Denizen denizen = (Denizen) plugin.getServer().getPluginManager().getPlugin("Denizen");
		if(args.argsLength()>1 && args.getString(2).equalsIgnoreCase("info")){
			if(args.argsLength()==3){
				try{
					ScriptRepo.info(sender, args.getString(2));
				}catch (JSONException e){
					Messaging.send(sender, "The Script Repo sent a bad reply, please try again.");
				}
			}else{
				Messaging.send(sender, "Invalid usage! Correct usage: ");
				Messaging.send(sender, "/citizens denizen repo info [ID] - View information about a script.");
			}
		}else if(args.argsLength()>1 && args.getString(2).equalsIgnoreCase("search")){
			if(args.argsLength()>=3){
				String query = Utilities.arrayToString(args.getSlice(3), "+");
				try{
					ScriptRepo.search(sender, query);
				}catch (JSONException e){
					Messaging.send(sender, "The Script Repo sent a bad reply, please try again.");
				}
			}else{
				Messaging.send(sender, "Invalid usage! Correct usage: ");
				Messaging.send(sender, "/citizens denizen repo search [search query] - Search the script repo.");
			}
		}else if(args.argsLength()>1 && args.getString(2).equalsIgnoreCase("load")){
			if(args.argsLength()==3){
				try{
					ScriptRepo.load(sender, args.getString(2));
				}catch (JSONException e){
					Messaging.send(sender, "The Script Repo sent a bad reply, please try again.");
				}
			}else{
				Messaging.send(sender, "Invalid usage! Correct usage: ");
				Messaging.send(sender, "/citizens denizen repo load (ID) - Load the specified script (or the most recently viewed) onto your server.");
			}
		}
		Messaging.send(sender, "§cThe Citizens Script Repo is a website accessible at §bhttp://scripts.citizensnpcs.com/§c"
				+ " where you can browse and post numerous types of scripts"
				+ " including Denizen scripts.");
		Messaging.send(sender, "§cYou can also search and download scripts directly in game with the repo subcommand.");
		Messaging.send(sender, "/citizens denizen repo info [ID] - View information about a script.");
		Messaging.send(sender, "/citizens denizen repo search [search query] - Search the script repo.");
		Messaging.send(sender, "/citizens denizen repo load (ID) - Load the specified script (or the most recently viewed) onto your server.");
	}

    /*
     * DENIZEN TEST, always a new flavor
     */
    @Command(
            aliases = { "denizen" }, usage = "report",
            desc = "For testing purposes only, use at your own risk!", modifiers = { "report" },
            min = 1, max = 3, permission = "denizen.basic")
    public void text(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        TriggerTrait trait = npc.getTrait(TriggerTrait.class);
        trait.report();
    }

}


