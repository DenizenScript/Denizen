package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.npc.traits.*;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.command.manager.messaging.Messaging;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import net.citizensnpcs.Citizens;
import net.citizensnpcs.api.command.Command;
import net.citizensnpcs.api.command.CommandContext;
import net.citizensnpcs.api.command.Requirements;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Anchors;
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
        if (!npc.hasTrait(PushableTrait.class)) {
            npc.addTrait(PushableTrait.class);
        }
        PushableTrait trait = npc.getTrait(PushableTrait.class);

        if (args.hasFlag('r') && !args.hasFlag('t')) {
            trait.setReturnable(!trait.isReturnable());
            Messaging.sendInfo(sender, npc.getName() + (trait.isReturnable() ? " will " : " will not ") + "return when being pushed"
                    + (!trait.isReturnable() || trait.isPushable() ? "." : ", but is currently not pushable."));
            return;

        }
        else if (args.hasValueFlag("delay") && !args.hasFlag('t')) {
            if (args.getFlag("delay").matches("\\d+") && args.getFlagInteger("delay") > 0) {
                trait.setDelay(Integer.valueOf(args.getFlag("delay")));
                trait.setReturnable(true);
                Messaging.sendInfo(sender, npc.getName() + " will return after '" + args.getFlag("delay") + "' seconds"
                        + (trait.isPushable() ? "." : ", but is currently not pushable."));
                return;
            }
            else {
                Messaging.sendError(sender, "Delay must be a valid number of seconds!");
                return;
            }

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
            Messaging.sendInfo(sender, npc.getName() + (trait.isPushable() ? " is" : " is not") + " currently pushable" +
                    (trait.isReturnable() && trait.isPushable() ? " and will return when pushed after '" + trait.getDelay() + "' seconds." : "."));
            return;

        }
        else if (args.length() > 2) {
            Messaging.send(sender, "");
            Messaging.send(sender, "<f>Use '-t' to toggle pushable state. <b>Example: /npc pushable -t");
            Messaging.send(sender, "<f>To have the NPC return when pushed, use '-r'.");
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
        if (!npc.hasTrait(ConstantsTrait.class)) {
            npc.addTrait(ConstantsTrait.class);
        }
        ConstantsTrait trait = npc.getTrait(ConstantsTrait.class);
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
            Messaging.sendInfo(sender, npc.getName() + " has removed constant '" + args.getFlag("remove") + "'.");
            return;

        }
        else if (args.length() > 2 && args.getInteger(1, 0) < 1) {
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

        try {
            trait.describe(sender, args.getInteger(1, 1));
        }
        catch (net.citizensnpcs.api.command.exception.CommandException e) {
            throw new CommandException(e.getMessage());
        }
    }

    /*
     * ASSIGNMENT
     */
    @Command(
            aliases = {"npc"}, usage = "assignment --set assignment_name (-r)",
            desc = "Controls the assignment for an NPC.", flags = "r", modifiers = {"assignment", "assign"},
            min = 1, max = 3, permission = "denizen.npc.assign")
    @Requirements(selected = true, ownership = true)
    public void assignment(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(AssignmentTrait.class)) {
            npc.addTrait(AssignmentTrait.class);
        }
        Player player = null;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        AssignmentTrait trait = npc.getTrait(AssignmentTrait.class);

        if (args.hasValueFlag("set")) {
            String script = args.getFlag("set").replace("\"", "");

            if (trait.setAssignment(script, PlayerTag.mirrorBukkitPlayer(player))) {
                if (trait.hasAssignment()) {
                    Messaging.sendInfo(sender, npc.getName() + "'s assignment is now: '" + trait.getAssignment().getName() + "'.");
                }
                else {
                    Messaging.sendInfo(sender, npc.getName() + "'s assignment was not able to be set.");
                }
            }
            else if (ScriptRegistry.containsScript(script)) {
                Messaging.sendError(sender, "A script with that name exists, but it is not an assignment script!");
            }
            else {
                Messaging.sendError(sender, "Invalid assignment! Has the script sucessfully loaded, or has it been mispelled?");
            }
            return;

        }
        else if (args.hasFlag('r')) {
            trait.removeAssignment(PlayerTag.mirrorBukkitPlayer(player));
            Messaging.sendInfo(sender, npc.getName() + "'s assignment has been removed.");
            return;

        }
        else if (args.length() > 2 && args.getInteger(1, 0) < 1) {
            Messaging.send(sender, "");
            Messaging.send(sender, "<f>Use '--set name' to set an assignment script to this NPC.");
            Messaging.send(sender, "<b>Example: /npc assignment --set \"Magic Shop\"");
            Messaging.send(sender, "<f>Remove an assignment with '-r'.");
            Messaging.send(sender, "<f>Note: Assigning a script will fire an 'On Assignment:' action.");
            Messaging.send(sender, "");
            return;
        }

        try {
            trait.describe(sender, args.getInteger(1, 1));
        }
        catch (net.citizensnpcs.api.command.exception.CommandException e) {
            throw new CommandException(e.getMessage());
        }
    }

    /*
     * TRIGGER
     */
    @Command(
            aliases = {"npc"}, usage = "trigger [trigger name] [(--cooldown [seconds])|(--radius [radius])|(-t)]",
            desc = "Controls the various triggers for an NPC.", flags = "t", modifiers = {"trigger", "tr"},
            min = 1, max = 3, permission = "denizen.npc.trigger")
    @Requirements(selected = true, ownership = true)
    public void trigger(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(TriggerTrait.class)) {
            npc.addTrait(TriggerTrait.class);
        }
        TriggerTrait trait = npc.getTrait(TriggerTrait.class);
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
            if (DenizenAPI.getCurrentInstance().getTriggerRegistry().get(triggerName) == null) {
                Messaging.sendError(sender, "'" + triggerName.toUpperCase() + "' trigger does not exist.");
                Messaging.send(sender, "<f>Usage: /npc trigger [trigger_name] [(--cooldown #)|(--radius #)|(-t)]");
                Messaging.send(sender, "");
                Messaging.send(sender, "<f>Use '--name trigger_name' to specify a specific trigger, and '-t' to toggle.");
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
                Messaging.sendInfo(sender, triggerName.toUpperCase() + " trigger radius now " + args.getFlag("radius") + ".");
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

    /*
     * NICKNAME
     */
    @Command(
            aliases = {"npc"}, usage = "nickname [--set nickname]",
            desc = "Gives the NPC a nickname, used with a Denizen-compatible Speech Engine.", modifiers = {"nickname", "nick", "ni"},
            min = 1, max = 3, permission = "denizen.npc.nickname")
    @Requirements(selected = true, ownership = true)
    public void nickname(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(NicknameTrait.class)) {
            npc.addTrait(NicknameTrait.class);
        }
        NicknameTrait trait = npc.getTrait(NicknameTrait.class);
        if (args.hasValueFlag("set")) {
            trait.setNickname(args.getFlag("set"));
            Messaging.send(sender, "Nickname set.");
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

    /*
     * Sit
     */
    @Command(
            aliases = {"npc"}, usage = "sit (--location x,y,z,world) (--anchor anchor_name) (-c)",
            desc = "Makes the NPC sit.", flags = "c", modifiers = {"sit"},
            min = 1, max = 3, permission = "denizen.npc.sit")
    @Requirements(selected = true, ownership = true)
    public void sitting(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc.getEntity().getType() != EntityType.PLAYER) {
            Messaging.sendError(sender, npc.getName() + " needs to be a Player type NPC to sit!");
            return;
        }

        if (!npc.hasTrait(SittingTrait.class)) {
            npc.addTrait(SittingTrait.class);
        }
        SittingTrait trait = npc.getTrait(SittingTrait.class);

        if (args.hasFlag('c')) {
            trait.sit(args.getSenderTargetBlockLocation());
        }
        else if (args.hasValueFlag("location")) {
            String[] argsArray = args.getFlag("location").split(",");
            if (argsArray.length != 4) {
                Messaging.sendError(sender, "Usage: /npc sit --location x,y,z,world");
                return;
            }
            trait.sit(LocationTag.valueOf(argsArray[0] + "," + argsArray[1] + "," + argsArray[2] + "," + argsArray[3]));
        }
        else if (args.hasValueFlag("anchor")) {
            if (npc.hasTrait(Anchors.class)) {
                Anchors anchors = npc.getTrait(Anchors.class);
                if (anchors.getAnchor(args.getFlag("anchor")) != null) {
                    trait.sit(anchors.getAnchor(args.getFlag("anchor")).getLocation());
                }
            }
            Messaging.sendError(sender, "The NPC does not have the specified anchor!");
        }
        else {
            trait.sit();
        }

    }

    /*
     * Stand
     */
    @Command(
            aliases = {"npc"}, usage = "stand",
            desc = "Makes the NPC stand.", modifiers = {"stand"},
            min = 1, max = 1, permission = "denizen.npc.stand")
    @Requirements(selected = true, ownership = true)
    public void standing(CommandContext args, CommandSender sender, NPC npc) throws CommandException {

        if (npc.hasTrait(SittingTrait.class)) {
            SittingTrait trait = npc.getTrait(SittingTrait.class);
            trait.stand();
            npc.removeTrait(SittingTrait.class);
        }
        else if (npc.hasTrait(SneakingTrait.class)) {
            SneakingTrait trait = npc.getTrait(SneakingTrait.class);
            if (!trait.isSneaking()) {
                npc.removeTrait(SittingTrait.class);
                Messaging.sendError(sender, npc.getName() + " is already standing!");
                return;
            }
            trait.stand();
            npc.removeTrait(SneakingTrait.class);
        }
    }

    /*
     * Sleep
     */
    @Command(
            aliases = {"npc"}, usage = "sleep (--location x,y,z,world) (--anchor anchor_name)",
            desc = "Makes the NPC sleep.", modifiers = {"sleep"},
            min = 1, max = 3, permission = "denizen.npc.sleep")
    @Requirements(selected = true, ownership = true)
    public void sleeping(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(SleepingTrait.class)) {
            npc.addTrait(SleepingTrait.class);
        }
        SleepingTrait trait = npc.getTrait(SleepingTrait.class);

        if (trait.isSleeping()) {
            Messaging.send(sender, npc.getName() + " was already sleeping, and is now standing!");
            trait.wakeUp();
            return;
        }

        if (args.hasValueFlag("location")) {
            String[] argsArray = args.getFlag("location").split(",");
            if (argsArray.length != 4) {
                Messaging.sendError(sender, "Usage: /npc sleep --location x,y,z,world");
                return;
            }
            trait.toSleep(LocationTag.valueOf(argsArray[0] + "," + argsArray[1] + "," + argsArray[2] + "," + argsArray[3]));
        }
        else if (args.hasValueFlag("anchor")) {
            if (npc.hasTrait(Anchors.class)) {
                Anchors anchors = npc.getTrait(Anchors.class);
                if (anchors.getAnchor(args.getFlag("anchor")) != null) {
                    trait.toSleep(anchors.getAnchor(args.getFlag("anchor")).getLocation());
                }
            }
            Messaging.sendError(sender, "The NPC does not have the specified anchor!");
        }
        else {
            trait.toSleep();
        }

    }

    /*
     * Wakeup
     */
    @Command(
            aliases = {"npc"}, usage = "wakeup",
            desc = "Makes the NPC wake up.", modifiers = {"wakeup"},
            min = 1, max = 1, permission = "denizen.npc.sleep")
    @Requirements(selected = true, ownership = true)
    public void wakingup(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(SleepingTrait.class)) {
            npc.addTrait(SleepingTrait.class);
        }
        SleepingTrait trait = npc.getTrait(SleepingTrait.class);

        if (!trait.isSleeping()) {
            npc.removeTrait(SleepingTrait.class);
            Messaging.sendError(sender, npc.getName() + " is already awake!");
            return;
        }

        trait.wakeUp();
        npc.removeTrait(SleepingTrait.class);
    }

    /*
     * Fish
     */
    @Command(
            aliases = {"npc"}, usage = "fish (--location x,y,z,world) (--anchor anchor_name) (-c)",
            desc = "Makes the NPC fish, casting at the given location.", flags = "c", modifiers = {"fish"},
            min = 1, max = 3, permission = "denizen.npc.fish")
    @Requirements(selected = true, ownership = true)
    public void startFishing(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(FishingTrait.class)) {
            npc.addTrait(FishingTrait.class);
        }
        FishingTrait trait = npc.getTrait(FishingTrait.class);

        if (trait.isFishing()) {
            Messaging.sendError(sender, npc.getName() + " is already fishing! Use '/npc stopfishing' to stop.");
            return;
        }

        if (args.hasFlag('c')) {
            trait.startFishing(args.getSenderTargetBlockLocation());
        }

        // TODO: Make command use new CatchTypes
        //if (args.hasFlag('f')) {
        //    trait.setCatchFish(true);
        //}

        if (args.hasValueFlag("percent")) {
            trait.setCatchPercent(args.getFlagInteger("percent"));
        }

        if (args.hasValueFlag("location")) {
            String[] argsArray = args.getFlag("location").split(",");
            if (argsArray.length != 4) {
                Messaging.sendError(sender, "Usage: /npc fish --location x,y,z,world");
                return;
            }
            trait.startFishing(LocationTag.valueOf(argsArray[0] + "," + argsArray[1] + "," + argsArray[2] + "," + argsArray[3]));
        }
        else if (args.hasValueFlag("anchor")) {
            if (npc.hasTrait(Anchors.class)) {
                Anchors anchors = npc.getTrait(Anchors.class);
                if (anchors.getAnchor(args.getFlag("anchor")) != null) {
                    trait.startFishing(anchors.getAnchor(args.getFlag("anchor")).getLocation());
                }
            }
            Messaging.sendError(sender, "The NPC does not have the specified anchor!");
        }
        else {
            trait.startFishing();
        }
        Messaging.send(sender, npc.getName() + " is now fishing.");
    }

    /*
     * Stopfishing
     */
    @Command(
            aliases = {"npc"}, usage = "stopfishing",
            desc = "Makes the NPC stop fishing.", modifiers = {"stopfishing"},
            min = 1, max = 1, permission = "denizen.npc.fish")
    @Requirements(selected = true, ownership = true)
    public void stopFishing(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(FishingTrait.class)) {
            npc.addTrait(FishingTrait.class);
        }
        FishingTrait trait = npc.getTrait(FishingTrait.class);

        if (!trait.isFishing()) {
            npc.removeTrait(FishingTrait.class);
            Messaging.sendError(sender, npc.getName() + " isn't fishing!");
            return;
        }

        trait.stopFishing();
        npc.removeTrait(FishingTrait.class);
        Messaging.send(sender, npc.getName() + " is no longer fishing.");
    }

    /*
     * Sneak
     */
    @Command(
            aliases = {"npc"}, usage = "sneak",
            desc = "Makes the NPC crouch.", flags = "", modifiers = {"sneak", "crouch"},
            min = 1, max = 1, permission = "denizen.npc.sneak")
    @Requirements(selected = true, ownership = true)
    public void sneaking(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc.getEntity().getType() != EntityType.PLAYER) {
            Messaging.sendError(sender, npc.getName() + " needs to be a Player type NPC to sneak!");
            return;
        }

        if (!npc.hasTrait(SneakingTrait.class)) {
            npc.addTrait(SneakingTrait.class);
        }
        SneakingTrait trait = npc.getTrait(SneakingTrait.class);

        if (trait.isSneaking()) {
            trait.stand();
            Messaging.send(sender, npc.getName() + " was already sneaking, and is now standing.");
        }
        else {
            trait.sneak();
            Messaging.send(sender, npc.getName() + " is now sneaking.");
        }

    }

    /*
     * Mirror
     */
    @Command(
            aliases = {"npc"}, usage = "mirror",
            desc = "Makes the NPC mirror the skin of the player looking at it.", flags = "", modifiers = {"mirror"},
            min = 1, max = 1, permission = "denizen.npc.mirror")
    @Requirements(selected = true, ownership = true)
    public void mirror(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (npc.getEntity().getType() != EntityType.PLAYER) {
            Messaging.sendError(sender, npc.getName() + " needs to be a Player type NPC to be a mirror!");
            return;
        }

        if (!npc.hasTrait(MirrorTrait.class)) {
            npc.addTrait(MirrorTrait.class);
            npc.getTrait(MirrorTrait.class).enableMirror();
            Messaging.send(sender, npc.getName() + " is now mirroring player skins.");
            return;
        }
        MirrorTrait trait = npc.getTrait(MirrorTrait.class);

        if (trait.mirror) {
            trait.disableMirror();
            Messaging.send(sender, npc.getName() + " is no longer mirroring player skins.");
        }
        else {
            trait.enableMirror();
            Messaging.send(sender, npc.getName() + " is now mirroring player skins.");
        }
    }

    /*
     * Invisible
     */
    @Command(
            aliases = {"npc"}, usage = "invisible",
            desc = "Turns the NPC invisible.", flags = "", modifiers = {"invisible"},
            min = 1, max = 3, permission = "denizen.npc.invisible")
    @Requirements(selected = true, ownership = true)
    public void invisible(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(InvisibleTrait.class)) {
            npc.addTrait(InvisibleTrait.class);
            npc.getTrait(InvisibleTrait.class).setInvisible(true);
            Messaging.send(sender, npc.getName() + " is now invisible.");
            return;
        }
        InvisibleTrait trait = npc.getTrait(InvisibleTrait.class);

        trait.toggle();
        if (trait.isInvisible()) {
            Messaging.send(sender, npc.getName() + " is now invisible.");
        }
        else {
            Messaging.send(sender, npc.getName() + " is no longer invisible.");
        }
    }

    /*
     * HEALTH
     */
    @Command(
            aliases = {"npc"}, usage = "health --set # (-r)",
            desc = "Sets the max health for an NPC.", modifiers = {"health", "he", "hp"},
            min = 1, max = 3, permission = "denizen.npc.health", flags = "sra")
    @Requirements(selected = true, ownership = true)
    public void health(CommandContext args, CommandSender sender, NPC npc) throws CommandException {
        if (!npc.hasTrait(HealthTrait.class)) {
            npc.addTrait(HealthTrait.class);
        }
        HealthTrait trait = npc.getTrait(HealthTrait.class);

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
