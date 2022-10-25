package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.npc.traits.*;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.AssignmentScriptContainer;
import com.denizenscript.denizen.utilities.command.manager.messaging.Messaging;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Anchors;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class NPCCommandHandler {

    public NPCCommandHandler(Citizens citizens) {
    }

    // <--[language]
    // @name /npc pushable command
    // @group Console Commands
    // @description
    // The '/npc pushable' command controls a NPCs Pushable Trait. When a NPC is 'pushable', the NPC
    // will move out of the way when colliding with another LivingEntity.
    //
    // Pushable NPCs have 3 different settings available: Toggled, Returnable, and Delay.
    //
    // When an NPCs Pushable Trait is toggled off, it will not function. Entities which
    // collide may occupy the same space. To toggle pushable on or off, use the Bukkit command:
    // /npc pushable -t
    //
    // Setting the NPC as 'returnable' will automatically navigate the NPC back to
    // its original location after a specified delay. If not returnable, NPCs will retain
    // their position after being moved.
    // /npc pushable -r
    //
    // To change the delay of a returnable NPC, use the following Bukkit Command,
    // specifying the number of seconds in which the delay should be.
    // /npc pushable --delay #

    // It is possible to use multiple arguments at once. For example:
    // /npc pushable -t -r --delay 10
    //
    // Note: If allowed to move in undesirable areas, the NPC may be un-returnable
    // if the navigator cancels navigation due to being stuck. Care should be taken
    // to ensure a safe area around the NPC.
    //
    // See also: 'pushable trait'
    // -->
    @Command(
            aliases = {"npc"}, usage = "pushable -t (-r) (--delay #)", desc = "Makes an NPC pushable.",
            flags = "rt", modifiers = {"pushable", "push"}, min = 1, max = 2, permission = "denizen.npc.pushable")
    @Requirements(selected = true, ownership = true)
    public void pushable(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        PushableTrait trait = npc.getOrAddTrait(PushableTrait.class);
        if (args.hasFlag('r') && !args.hasFlag('t')) {
            trait.setReturnable(!trait.isReturnable());
            Messaging.sendInfo(sender, npc.getName() + (trait.isReturnable() ? " will " : " will not ") + "return when being pushed"
                    + (!trait.isReturnable() || trait.isPushable() ? "." : ", but is currently not pushable."));
            return;
        }
        else if (args.hasValueFlag("delay") && !args.hasFlag('t')) {
            if (args.getFlag("delay").matches("\\d+") && args.getFlagInteger("delay") > 0) {
                trait.setDelay(Integer.parseInt(args.getFlag("delay")));
                trait.setReturnable(true);
                Messaging.sendInfo(sender, npc.getName() + " will return after '" + args.getFlag("delay") + "' seconds"
                        + (trait.isPushable() ? "." : ", but is currently not pushable."));
            }
            else {
                Messaging.sendError(sender, "Delay must be a valid number of seconds!");
            }
            return;
        }
        else if (args.hasFlag('t') && !args.hasValueFlag("delay") && !args.hasFlag('r')) {
            trait.toggle();
            Messaging.sendInfo(sender, npc.getName() + (trait.isPushable() ? " is" : " is not") + " currently pushable" +
                    (trait.isReturnable() && trait.isPushable() ? " and will return when pushed after '" + trait.getDelay() + "' seconds." : "."));
            return;
        }
        else if (args.hasFlag('t')) {
            trait.toggle();
            if (args.hasFlag('r')) {
                trait.setReturnable(true);
            }
            if (args.hasValueFlag("delay") && args.getFlag("delay").matches("\\d+") && args.getFlagInteger("delay") > 0) {
                trait.setDelay(args.getFlagInteger("delay"));
            }
        }
        else if (args.length() > 2) {
            Messaging.send(sender, "");
            Messaging.send(sender, "<f>Use '-t' to toggle pushable state. <b>Example: /npc pushable -t");
            Messaging.send(sender, "<f>To have the NPC return to their position when pushed, use '-r'.");
            Messaging.send(sender, "<f>Change the return delay with '--delay #'.");
            Messaging.send(sender, "");
        }
        Messaging.sendInfo(sender, npc.getName() + (trait.isPushable() ? " is" : " is not") + " currently pushable" +
                (trait.isReturnable() ? " and will return when pushed after " + trait.getDelay() + " seconds." : "."));
    }

    // <--[language]
    // @name /npc constant command
    // @group Console Commands
    // @description
    // The /npc constants command configures a NPC's constants. Uses Denizen's ConstantTrait to keep track of
    // NPC-specific constants. This provides seamless integration with an assignment script's 'Default Constants' in
    // which text variables can be stored and retrieved with the use of 'replaceable tags', or API. Constants set at
    // the NPC level override any constants from the NPC's assignment script.
    //
    // Constants may be used in several ways: Setting, Removing, and Viewing
    //
    // To set a constant, all that is required is a name and value. Use the Bukkit command in the
    // following manner: (Note the use of quotes on multi world values)
    // /npc constant --set constant_name --value 'multi word value'
    //
    // Removing a constant from an NPC only requires a name. Note: It is not possible to remove a
    // constant set by the NPCs Assignment Script, except by modifying the script itself.
    // /npc constant --remove constant_name
    //
    // Viewing constants is easy, just use '/npc constant #', specifying a page number. Constants which
    // have been overridden by the NPC are formatted with a strike-through to indicate this case.
    //
    // To reference a constant value, use the replaceable tag to get the NPCs 'constant' attribute. For example:
    // <npc.constant[constant_name]>. Constants may also have other tags in their value, which will be replaced
    // whenever the constant is used, allowing the use of dynamic information.
    // -->
    @Command(
            aliases = {"npc"}, usage = "constant --set|remove name --value constant value",
            desc = "Views/adds/removes NPC string constants.", flags = "r", modifiers = {"constants", "constant", "cons"},
            min = 1, max = 3, permission = "denizen.npc.constants")
    @Requirements(selected = true, ownership = true)
    public void constants(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        ConstantsTrait trait = npc.getOrAddTrait(ConstantsTrait.class);
        if (args.hasValueFlag("set")) {
            if (!args.hasValueFlag("value")) {
                throw new CommandException("--SET requires use of the '--VALUE \"constant value\"' argument.");
            }
            trait.setConstant(args.getFlag("set"), args.getFlag("value"));
            Messaging.sendInfo(sender, npc.getName() + " has added constant '" + args.getFlag("set") + "'.");
            return;
        }
        else if (args.hasValueFlag("remove")) {
            trait.removeConstant(args.getFlag("remove"));
            Messaging.sendInfo(sender, "Removed constant '" + args.getFlag("remove") + "' from " + npc.getName() + "<f>.");
            return;
        }
        Messaging.send(sender, "");
        Messaging.send(sender, "<f>Use '--set name' to add/set a new NPC-specific constant.");
        Messaging.send(sender, "<f>Must also specify '--value \"constant value\"'.");
        Messaging.send(sender, "<b>Example: /npc constant --set constant_1 --value \"test value\"");
        Messaging.send(sender, "<f>Remove NPC-specific constants with '--remove name'");
        Messaging.send(sender, "<f>Note: Constants set will override any specified in an");
        Messaging.send(sender, "<f>assignment. Constants specified in assignments cannot be");
        Messaging.send(sender, "<f>removed with this command.");
        Messaging.send(sender, "");
    }

    @Command(
            aliases = {"npc"}, usage = "assignment ((--set|--remove|--add) assignment_name) (-c)",
            desc = "Controls the assignment for an NPC.", flags = "rc", modifiers = {"assignment", "assign"},
            min = 1, max = 3, permission = "denizen.npc.assign")
    @Requirements(selected = true, ownership = true)
    public void assignment(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        if (args.hasFlag('r') || args.hasFlag('c')) {
            if (!npc.hasTrait(AssignmentTrait.class)) {
                Messaging.sendError(sender, "That NPC has no assignments.");
                return;
            }
            npc.getOrAddTrait(AssignmentTrait.class).clearAssignments(PlayerTag.mirrorBukkitPlayer(player));
            npc.removeTrait(AssignmentTrait.class);
            Messaging.sendInfo(sender, npc.getName() + "<f>'s assignments have been cleared.");
            return;
        }
        if (args.hasValueFlag("set")) {
            String script = args.getFlag("set").replace("\"", "");
            ScriptContainer container = ScriptRegistry.getScriptContainer(script);
            if (container == null) {
                Messaging.sendError(sender, "Invalid assignment! Has the script successfully loaded, or has it been misspelled?");
            }
            else if (!(container instanceof AssignmentScriptContainer)) {
                Messaging.sendError(sender, "A script with that name exists, but it is not an assignment script!");
            }
            else {
                AssignmentTrait trait = npc.getOrAddTrait(AssignmentTrait.class);
                trait.clearAssignments(PlayerTag.mirrorBukkitPlayer(player));
                trait.addAssignmentScript((AssignmentScriptContainer) container, PlayerTag.mirrorBukkitPlayer(player));
                Messaging.sendInfo(sender, npc.getName() + "<f>'s assignment is now just: '" + container.getName() + "'.");
            }
            return;
        }
        else if (args.hasValueFlag("add")) {
            String script = args.getFlag("add").replace("\"", "");
            ScriptContainer container = ScriptRegistry.getScriptContainer(script);
            AssignmentTrait trait = npc.getOrAddTrait(AssignmentTrait.class);
            if (container == null) {
                Messaging.sendError(sender, "Invalid assignment! Has the script successfully loaded, or has it been misspelled?");
            }
            else if (!(container instanceof AssignmentScriptContainer)) {
                Messaging.sendError(sender, "A script with that name exists, but it is not an assignment script!");
            }
            else if (trait.addAssignmentScript((AssignmentScriptContainer) container, PlayerTag.mirrorBukkitPlayer(player))) {
                Messaging.sendInfo(sender, npc.getName() + "<f> is now assigned to '" + container.getName() + "'.");
            }
            else {
                Messaging.sendError(sender, "That NPC was already assigned that script.");
            }
            return;
        }
        else if (args.hasValueFlag("remove")) {
            String script = args.getFlag("remove").replace("\"", "");
            AssignmentTrait trait = npc.getOrAddTrait(AssignmentTrait.class);
            if (trait.removeAssignmentScript(script, PlayerTag.mirrorBukkitPlayer(player))) {
                trait.checkAutoRemove();
                if (npc.hasTrait(AssignmentTrait.class)) {
                    Messaging.sendInfo(sender, npc.getName() + "<f> is no longer assigned to '" + script + "'.");
                }
                else {
                    Messaging.sendInfo(sender, npc.getName() + "<f> no longer has any assignment.");
                }
            }
            else {
                Messaging.sendError(sender, npc.getName() + "<f> was already not assigned to " + script + ".");
            }
            return;
        }
        Messaging.send(sender, "");
        Messaging.send(sender, "<f>Use '--set name' to set a single assignment script to this NPC.");
        Messaging.send(sender, "<b>Example: /npc assignment --set \"Magic Shop\"");
        Messaging.send(sender, "<f>Use '--add name' to add an assignment, or '--remove name' to remove one assignment.");
        Messaging.send(sender, "<f>Clear all assignments with '-c'.");
        Messaging.send(sender, "<f>Note: Assigning a script will fire an 'On Assignment:' action.");
        Messaging.send(sender, "");
    }

    @Command(
            aliases = {"npc"}, usage = "trigger [trigger name] [(--cooldown [seconds])|(--radius [radius])|(-t)]",
            desc = "Controls the various triggers for an NPC.", flags = "t", modifiers = {"trigger", "tr"},
            min = 1, max = 3, permission = "denizen.npc.trigger")
    @Requirements(selected = true, ownership = true)
    public void trigger(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        TriggerTrait trait = npc.getOrAddTrait(TriggerTrait.class);
        if ((args.hasValueFlag("name") || (args.argsLength() > 1 && (args.getJoinedStrings(1) != null) && !args.getString(1).matches("\\d+")))) {
            // Get the name of the trigger
            String triggerName;
            if (args.hasValueFlag("name")) {
                triggerName = args.getFlag("name");
            }
            else {
                triggerName = args.getJoinedStrings(1);
            }
            // Check to make sure trigger exists
            if (Denizen.getInstance().triggerRegistry.get(triggerName) == null) {
                Messaging.sendError(sender, "'" + triggerName.toUpperCase() + "' trigger does not exist.");
                Messaging.send(sender, "<f>Usage: /npc trigger [trigger_name] [(--cooldown #)|(--radius #)|(-t)]");
                Messaging.send(sender, "");
                Messaging.send(sender, "<f>Use '--name trigger_name' to specify the trigger, and '-t' to toggle it.");
                Messaging.send(sender, "<b>Example: /npc trigger --name damage -t");
                Messaging.send(sender, "<f>You may also use '--cooldown #' to specify a new cooldown time, and '--radius #' to specify a specific radius, when applicable.");
                Messaging.send(sender, "");
                return;
            }
            // If toggling
            if (args.hasFlag('t')) {
                trait.toggleTrigger(triggerName);
            }
            // If setting cooldown
            if (args.hasValueFlag("cooldown")) {
                trait.setLocalCooldown(triggerName, args.getFlagDouble("cooldown"));
            }
            // If specifying radius
            if (args.hasValueFlag("radius")) {
                trait.setLocalRadius(triggerName, args.getFlagInteger("radius"));
                Messaging.sendInfo(sender, triggerName.toUpperCase() + " trigger radius is now " + args.getFlag("radius") + ".");
            }
            // Show current status of the trigger
            Messaging.sendInfo(sender, triggerName.toUpperCase() + " trigger " + (trait.isEnabled(triggerName) ? "is" : "is not") + " currently enabled" +
                    (trait.isEnabled(triggerName) ? " with a cooldown of '" + trait.getCooldownDuration(triggerName) + "' seconds." : "."));
            return;
        }
        try {
            trait.describe(sender, args.getInteger(1, 1));
        }
        catch (net.citizensnpcs.api.command.exception.CommandException e) {
            throw new CommandException(e.getMessage());
        }
    }

    @Command(
            aliases = {"npc"}, usage = "nickname [--set nickname]",
            desc = "Gives the NPC a nickname, used with a Denizen-compatible Speech Engine.", modifiers = {"nickname", "nick", "ni"},
            min = 1, max = 3, permission = "denizen.npc.nickname")
    @Requirements(selected = true, ownership = true)
    public void nickname(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        NicknameTrait trait = npc.getOrAddTrait(NicknameTrait.class);
        if (args.hasValueFlag("set")) {
            trait.setNickname(args.getFlag("set"));
            Messaging.send(sender, "Nickname set to '" + args.getFlag("set") + "'.");
            return;
        }
        else if (args.hasFlag('r')) {
            trait.setNickname("");
            Messaging.sendInfo(sender, "Nickname removed.");
            return;
        }
        if (trait.hasNickname()) {
            Messaging.sendInfo(sender, npc.getName() + "'s nickname is '" + trait.getNickname() + "'.");
        }
        else {
            Messaging.sendInfo(sender, npc.getName() + " does not have a nickname!");
        }
    }

    @Command(
            aliases = {"npc"}, usage = "sit (--location x,y,z,world) (--anchor anchor_name) (-c)",
            desc = "Makes the NPC sit.", flags = "c", modifiers = {"sit"},
            min = 1, max = 3, permission = "denizen.npc.sit")
    @Requirements(selected = true, ownership = true)
    public void sitting(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc.hasTrait(SneakingTrait.class)) {
            npc.getOrAddTrait(SneakingTrait.class).stand();
            npc.removeTrait(SneakingTrait.class);
        }
        if (npc.hasTrait(SleepingTrait.class)) {
            npc.getOrAddTrait(SleepingTrait.class).wakeUp();
            npc.removeTrait(SleepingTrait.class);
        }
        SittingTrait trait = npc.getOrAddTrait(SittingTrait.class);
        if (args.hasValueFlag("location")) {
            LocationTag location = LocationTag.valueOf(args.getFlag("location"), CoreUtilities.basicContext);
            if (location == null) {
                Messaging.sendError(sender, "Usage: /npc sit --location x,y,z,world");
                return;
            }
            trait.sit(location);
            return;
        }
        else if (args.hasValueFlag("anchor")) {
            if (npc.hasTrait(Anchors.class)) {
                Anchors anchors = npc.getOrAddTrait(Anchors.class);
                if (anchors.getAnchor(args.getFlag("anchor")) != null) {
                    trait.sit(anchors.getAnchor(args.getFlag("anchor")).getLocation());
                    Messaging.send(sender, npc.getName() + " is now sitting.");
                    return;
                }
            }
            Messaging.sendError(sender, "NPC " + npc.getName() + "<f> does not have the anchor '" + args.getFlag("anchor") + "'!");
            return;
        }
        Location targetLocation;
        if (args.hasFlag('c')) {
            targetLocation = args.getSenderTargetBlockLocation().clone().add(0.5, 0, 0.5);
            targetLocation.setYaw(npc.getStoredLocation().getYaw());
        }
        else {
            targetLocation = npc.getStoredLocation().clone();
            targetLocation.add(0, -0.2, 0);
        }
        if (trait.isSitting()) {
            Messaging.send(sender, npc.getName() + " is already sitting, use '/npc stand' to stand the NPC back up.");
            return;
        }
        Block block = targetLocation.getBlock();
        BlockData data = block.getBlockData();
        if (data instanceof Stairs || data instanceof Bed || (data instanceof Slab && ((Slab) data).getType() == Slab.Type.BOTTOM)) {
            targetLocation.setY(targetLocation.getBlockY() + 0.3);
        }
        else if (data instanceof Campfire) {
            targetLocation.setY(targetLocation.getBlockY() + 0.2);
        }
        else if (block.getType().name().endsWith("CARPET")) {
            targetLocation.setY(targetLocation.getBlockY());
        }
        else if (block.getType().isSolid()) {
            targetLocation.setY(targetLocation.getBlockY() + 0.8);
        }
        trait.sit(targetLocation);
        Messaging.send(sender, npc.getName() + " is now sitting.");
    }

    @Command(
            aliases = {"npc"}, usage = "stand",
            desc = "Makes the NPC stand.", modifiers = {"stand"},
            min = 1, max = 1, permission = "denizen.npc.stand")
    @Requirements(selected = true, ownership = true)
    public void standing(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc.hasTrait(SittingTrait.class)) {
            SittingTrait trait = npc.getOrAddTrait(SittingTrait.class);
            if (!trait.isSitting()) {
                npc.removeTrait(SittingTrait.class);
                Messaging.sendError(sender, npc.getName() + " is already standing!");
                return;
            }
            trait.stand();
            npc.removeTrait(SittingTrait.class);
            Messaging.send(sender, npc.getName() + " is now standing.");
        }
        else if (npc.hasTrait(SneakingTrait.class)) {
            SneakingTrait trait = npc.getOrAddTrait(SneakingTrait.class);
            if (!trait.isSneaking()) {
                npc.removeTrait(SneakingTrait.class);
                Messaging.sendError(sender, npc.getName() + " is already standing!");
                return;
            }
            trait.stand();
            npc.removeTrait(SneakingTrait.class);
            Messaging.send(sender, npc.getName() + " is now standing.");
        }
        else if (npc.hasTrait(SleepingTrait.class)) {
            SleepingTrait trait = npc.getOrAddTrait(SleepingTrait.class);
            if (!trait.isSleeping()) {
                npc.removeTrait(SleepingTrait.class);
                Messaging.sendError(sender, npc.getName() + " is already standing!");
                return;
            }
            trait.wakeUp();
            npc.removeTrait(SleepingTrait.class);
            Messaging.send(sender, npc.getName() + " is now standing.");
        }
        else {
            Messaging.sendError(sender, npc.getName() + " is already standing!");
        }
    }

    @Command(
            aliases = {"npc"}, usage = "sleep (--location x,y,z,world) (--anchor anchor_name)",
            desc = "Makes the NPC sleep.", modifiers = {"sleep" },
            min = 1, max = 3, permission = "denizen.npc.sleep")
    @Requirements(selected = true, ownership = true, types = { EntityType.VILLAGER, EntityType.PLAYER })
    public void sleeping(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc.hasTrait(SneakingTrait.class)) {
            npc.getOrAddTrait(SneakingTrait.class).stand();
            npc.removeTrait(SneakingTrait.class);
        }
        if (npc.hasTrait(SittingTrait.class)) {
            npc.getOrAddTrait(SittingTrait.class).stand();
            npc.removeTrait(SittingTrait.class);
        }
        SleepingTrait trait = npc.getOrAddTrait(SleepingTrait.class);
        if (trait.isSleeping()) {
            Messaging.send(sender, npc.getName() + " was already sleeping, and is now standing!");
            trait.wakeUp();
            npc.removeTrait(SleepingTrait.class);
            return;
        }
        if (args.hasValueFlag("location")) {
            LocationTag location = LocationTag.valueOf(args.getFlag("location"), CoreUtilities.basicContext);
            if (location == null) {
                Messaging.sendError(sender, "Usage: /npc sleep --location x,y,z,world");
                return;
            }
            trait.toSleep(location);
        }
        else if (args.hasValueFlag("anchor")) {
            if (npc.hasTrait(Anchors.class)) {
                Anchors anchors = npc.getOrAddTrait(Anchors.class);
                if (anchors.getAnchor(args.getFlag("anchor")) != null) {
                    trait.toSleep(anchors.getAnchor(args.getFlag("anchor")).getLocation());
                    Messaging.send(sender, npc.getName() + " is now sleeping.");
                    return;
                }
            }
            Messaging.sendError(sender, "NPC " + npc.getName() + "<f> does not have the anchor '" + args.getFlag("anchor") + "'!");
            return;
        }
        else {
            trait.toSleep();
        }
        if (!trait.isSleeping()) {
            npc.removeTrait(SleepingTrait.class);
        }
        Messaging.send(sender, npc.getName() + " is now sleeping.");
    }

    @Command(
            aliases = {"npc"}, usage = "wakeup",
            desc = "Makes the NPC wake up.", modifiers = {"wakeup"},
            min = 1, max = 1, permission = "denizen.npc.sleep")
    @Requirements(selected = true, ownership = true)
    public void wakingup(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        SleepingTrait trait = npc.getOrAddTrait(SleepingTrait.class);
        if (!trait.isSleeping()) {
            npc.removeTrait(SleepingTrait.class);
            Messaging.sendError(sender, npc.getName() + " is already awake!");
            return;
        }
        trait.wakeUp();
        npc.removeTrait(SleepingTrait.class);
        Messaging.send(sender, npc.getName() + " is no longer sleeping.");
    }

    @Command(
            aliases = {"npc"}, usage = "fish (--location x,y,z,world) (--anchor anchor_name) (-c) (--reel_time <duration>) (--cast_time <duration>)",
            desc = "Makes the NPC fish, casting at the given location.", flags = "c", modifiers = {"fish"},
            min = 1, max = 3, permission = "denizen.npc.fish")
    @Requirements(selected = true, ownership = true)
    public void startFishing(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        FishingTrait trait = npc.getOrAddTrait(FishingTrait.class);
        if (trait.isFishing()) {
            Messaging.sendError(sender, npc.getName() + " is already fishing! Use '/npc stopfishing' to stop.");
            return;
        }
        if (args.hasValueFlag("percent")) {
            trait.setCatchPercent(args.getFlagInteger("percent"));
            Messaging.send(sender, npc.getName() + " will now catch " + args.getFlagInteger("percent") + "% of the fish.");
        }
        if (args.hasValueFlag("reel_time")) {
            DurationTag duration = DurationTag.valueOf(args.getFlag("reel_time"), CoreUtilities.basicContext);
            if (duration == null) {
                Messaging.sendError(sender, "Invalid reel duration!");
                return;
            }
            trait.reelTickRate = duration.getTicksAsInt();
            Messaging.send(sender, "Set reel rate to " + duration.formatted(true));
        }
        if (args.hasValueFlag("cast_time")) {
            DurationTag duration = DurationTag.valueOf(args.getFlag("cast_time"), CoreUtilities.basicContext);
            if (duration == null) {
                Messaging.sendError(sender, "Invalid cast duration!");
                return;
            }
            trait.reelTickRate = duration.getTicksAsInt();
            Messaging.send(sender, "Set cast rate to " + duration.formatted(true) + ".");
        }
        if (args.hasFlag('c')) {
            trait.startFishing(args.getSenderTargetBlockLocation());
            Messaging.send(sender, npc.getName() + " is now fishing at your cursor.");
            return;
        }
        else if (args.hasValueFlag("location")) {
            String[] argsArray = args.getFlag("location").split(",");
            if (argsArray.length != 4) {
                Messaging.sendError(sender, "Usage: /npc fish --location x,y,z,world");
                return;
            }
            trait.startFishing(LocationTag.valueOf(argsArray[0] + "," + argsArray[1] + "," + argsArray[2] + "," + argsArray[3], CoreUtilities.basicContext));
        }
        else if (args.hasValueFlag("anchor")) {
            if (npc.hasTrait(Anchors.class)) {
                Anchors anchors = npc.getOrAddTrait(Anchors.class);
                if (anchors.getAnchor(args.getFlag("anchor")) != null) {
                    trait.startFishing(anchors.getAnchor(args.getFlag("anchor")).getLocation());
                } else {
                    Messaging.sendError(sender, "Anchor '" + args.getFlag("anchor") + "' is invalid! Did you make a typo?");
                    return;
                }
            } else {
                Messaging.sendError(sender, "NPC " + npc.getName() + "<f> does not have the anchor '" + args.getFlag("anchor") + "'!");
                return;
            }
        }
        else {
            trait.startFishing();
        }
        Messaging.send(sender, npc.getName() + " is now fishing.");
    }

    @Command(
            aliases = {"npc"}, usage = "stopfishing",
            desc = "Makes the NPC stop fishing.", modifiers = {"stopfishing"},
            min = 1, max = 1, permission = "denizen.npc.fish")
    @Requirements(selected = true, ownership = true)
    public void stopFishing(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        FishingTrait trait = npc.getOrAddTrait(FishingTrait.class);
        if (!trait.isFishing()) {
            npc.removeTrait(FishingTrait.class);
            Messaging.sendError(sender, npc.getName() + " isn't currently fishing!");
            return;
        }
        trait.stopFishing();
        npc.removeTrait(FishingTrait.class);
        Messaging.send(sender, npc.getName() + " is no longer fishing.");
    }

    @Command(
            aliases = {"npc"}, usage = "sneak",
            desc = "Makes the NPC crouch.", modifiers = {"sneak", "crouch"},
            min = 1, max = 1, permission = "denizen.npc.sneak")
    @Requirements(selected = true, ownership = true)
    public void sneaking(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc.hasTrait(SleepingTrait.class)) {
            npc.getOrAddTrait(SleepingTrait.class).wakeUp();
            npc.removeTrait(SleepingTrait.class);
        }
        if (npc.hasTrait(SleepingTrait.class)) {
            npc.getOrAddTrait(SleepingTrait.class).wakeUp();
            npc.removeTrait(SleepingTrait.class);
        }
        if (npc.getEntity().getType() != EntityType.PLAYER) {
            Messaging.sendError(sender, npc.getName() + " needs to be a Player type NPC to sneak!");
            return;
        }
        SneakingTrait trait = npc.getOrAddTrait(SneakingTrait.class);
        if (trait.isSneaking()) {
            trait.stand();
            Messaging.send(sender, npc.getName() + " was already sneaking, and is now standing.");
        }
        else {
            trait.sneak();
            Messaging.send(sender, npc.getName() + " is now sneaking.");
        }
    }

    @Command(
            aliases = {"npc"}, usage = "mirrorskin",
            desc = "Makes the NPC mirror the skin of the player looking at it.", modifiers = {"mirrorskin", "mirror"},
            min = 1, max = 1, permission = "denizen.npc.mirror")
    @Requirements(selected = true, ownership = true)
    public void mirror(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc.getEntity().getType() != EntityType.PLAYER) {
            Messaging.sendError(sender, npc.getName() + " needs to be a Player type NPC to be a skin-mirror!");
            return;
        }
        if (!npc.hasTrait(MirrorTrait.class)) {
            npc.getOrAddTrait(MirrorTrait.class).enableMirror();
            Messaging.send(sender, npc.getName() + " is now mirroring player skins.");
            return;
        }
        MirrorTrait trait = npc.getOrAddTrait(MirrorTrait.class);
        if (trait.mirror) {
            trait.disableMirror();
            npc.removeTrait(MirrorTrait.class);
            Messaging.send(sender, npc.getName() + " is no longer mirroring player skins.");
        }
        else {
            trait.enableMirror();
            Messaging.send(sender, npc.getName() + " is now mirroring player skins.");
        }
    }

    @Command(
            aliases = {"npc"}, usage = "mirrorname",
            desc = "Makes the NPC mirror the username of the player looking at it.", modifiers = {"mirrorname"},
            min = 1, max = 1, permission = "denizen.npc.mirror")
    @Requirements(selected = true, ownership = true)
    public void mirrorName(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(MirrorNameTrait.class)) {
            npc.getOrAddTrait(MirrorNameTrait.class).enableMirror();
            Messaging.send(sender, npc.getName() + " is now mirroring player names.");
            return;
        }
        MirrorNameTrait trait = npc.getOrAddTrait(MirrorNameTrait.class);
        if (trait.mirror) {
            trait.disableMirror();
            npc.removeTrait(MirrorNameTrait.class);
            Messaging.send(sender, npc.getName() + " is no longer mirroring player names.");
        }
        else {
            trait.enableMirror();
            Messaging.send(sender, npc.getName() + " is now mirroring player names.");
        }
    }

    @Command(
            aliases = {"npc"}, usage = "mirrorequipment",
            desc = "Makes the NPC mirror the equipment of the player looking at it.", modifiers = {"mirrorequipment", "mirrorequip"},
            min = 1, max = 1, permission = "denizen.npc.mirror")
    @Requirements(selected = true, ownership = true)
    public void mirrorEquipment(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(MirrorEquipmentTrait.class)) {
            npc.getOrAddTrait(MirrorEquipmentTrait.class).enableMirror();
            Messaging.send(sender, npc.getName() + " is now mirroring player equipment.");
            return;
        }
        MirrorEquipmentTrait trait = npc.getOrAddTrait(MirrorEquipmentTrait.class);
        if (trait.mirror) {
            trait.disableMirror();
            npc.removeTrait(MirrorEquipmentTrait.class);
            Messaging.send(sender, npc.getName() + " is no longer mirroring player equipment.");
        }
        else {
            trait.enableMirror();
            Messaging.send(sender, npc.getName() + " is now mirroring player equipment.");
        }
    }

    @Command(
            aliases = {"npc"}, usage = "invisible",
            desc = "Turns the NPC invisible.", modifiers = {"invisible"},
            min = 1, max = 3, permission = "denizen.npc.invisible")
    @Requirements(selected = true, ownership = true)
    public void invisible(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(InvisibleTrait.class)) {
            npc.getOrAddTrait(InvisibleTrait.class).setInvisible(true);
            Messaging.send(sender, npc.getName() + " is now invisible.");
            return;
        }
        InvisibleTrait trait = npc.getOrAddTrait(InvisibleTrait.class);
        trait.toggle();
        if (trait.isInvisible()) {
            Messaging.send(sender, npc.getName() + " is now invisible.");
        }
        else {
            Messaging.send(sender, npc.getName() + " is no longer invisible.");
        }
    }

    @Command(
            aliases = {"npc"}, usage = "health (--set #) (--max #) (-r)",
            desc = "Sets the max health for an NPC.", modifiers = {"health", "he", "hp"},
            min = 1, max = 3, permission = "denizen.npc.health", flags = "sra")
    @Requirements(selected = true, ownership = true)
    public void health(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        HealthTrait trait = npc.getOrAddTrait(HealthTrait.class);
        boolean showMore = true;
        if (args.hasValueFlag("max")) {
            trait.setMaxhealth(args.getFlagInteger("max"));
            trait.setHealth();
            Messaging.send(sender, npc.getName() + "'s health maximum is now " + trait.getMaxhealth() + ".");
            showMore = false;
        }
        if (args.hasValueFlag("set")) {
            trait.setHealth(args.getFlagInteger("set"));
        }
        if (args.hasValueFlag("respawndelay")) {
            trait.setRespawnDelay(args.getFlag("respawndelay"));
            Messaging.send(sender, npc.getName() + "'s respawn delay now " + trait.getRespawnDelay()
                    + (trait.isRespawnable() ? "." : ", but is not currently auto-respawnable upon death."));
            showMore = false;
        }
        if (args.hasValueFlag("respawnlocation")) {
            trait.setRespawnLocation(args.getFlag("respawnlocation"));
            Messaging.send(sender, npc.getName() + "'s respawn location now " + trait.getRespawnLocationAsString()
                    + (trait.isRespawnable() ? "." : ", but is not currently auto-respawnable upon death."));
            showMore = false;
        }
        if (args.hasFlag('s')) {
            trait.setRespawnable(!trait.isRespawnable());
            Messaging.send(sender, npc.getName() + (trait.isRespawnable()
                    ? " will now auto-respawn on death after " + trait.getRespawnDelay() + " seconds."
                    : " will no longer auto-respawn on death."));
            showMore = false;
        }
        if (args.hasFlag('a')) {
            trait.animateOnDeath(!trait.animatesOnDeath());
            Messaging.send(sender, npc.getName() + (trait.animatesOnDeath()
                    ? " will now animate on death."
                    : " will no longer animate on death."));
            showMore = false;
        }
        else if (args.hasFlag('r')) {
            trait.setHealth();
            Messaging.send(sender, npc.getName() + "'s health reset to " + trait.getMaxhealth() + ".");
            showMore = false;
        }
        if (showMore) {
            Messaging.sendInfo(sender, npc.getName() + "'s health is '" + trait.getHealth() + "/" + trait.getMaxhealth() + "'.");
        }
    }
}
