package net.aufdemrand.denizen;

import java.lang.reflect.Array;
import java.util.*;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;


public class DenizenListener implements Listener {

    static Denizen plugin;
    public DenizenListener(Denizen instance) { plugin = instance; }

    public enum RequirementMode {
        NONE, ALL, ANY;
    }

    public enum Requirement {
        NONE, QUEST, NAME, WEARING, INVINSIBLE, ITEM, HOLDING, TIME, PRECIPITATION,
        STORMY, SUNNY, HUNGER, WORLD, PERMISSION, LEVEL, SCRIPT, NOTABLE, GROUP, MONEY, POTIONEFFECT }

    public enum Trigger {
        CHAT, CLICK, RIGHT_CLICK, LEFT_CLICK, FINISH, START, FAIL, BOUNCE
    }

    public enum Command {
        DELAY, ZAP, ASSIGN, UNASSIGN, C2SCRIPT, SPAWN, CHANGE, WEATHER, EFFECT, GIVE, TAKE, HEAL, DAMAGE,
        POTION_EFFECT, TELEPORT, STRIKE, WALK, NOD, REMEMBER, BOUNCE, RESPAWN, PERMISS, EXECUTE, SHOUT,
        WHISPER, NARRARATE, CHAT, ANNOUNCE, GRANT
    }



    /* PlayerChatListener
      *
      * Called when the player chats.  Determines if player is near a Denizen, and if so, checks if there
      * are scripts to interact with.  Also handles the chat output for the Player talking to the Denizen.
      *
      * Calls GetDenizensWithinRange, TalkToNPC, GetInteractScript, ParseScript
      */

    @EventHandler
    public void PlayerChatListener(PlayerChatEvent event) {

        List<NPC> DenizenList = GetDenizensWithinRange(event.getPlayer().
                getLocation(), event.getPlayer().getWorld(), Denizen.PlayerChatRangeInBlocks);
        if (DenizenList.isEmpty()) return;

        event.setCancelled(true);
        for (NPC thisDenizen : DenizenList) {
            TalkToNPC(thisDenizen, event.getPlayer(), event.getMessage());
            String theScript = GetInteractScript(thisDenizen, event.getPlayer());
            if (theScript.equals("none")) thisDenizen.chat(event.getPlayer(), plugin.getConfig().
                    getString("Denizens." + thisDenizen.getId() + ".Texts.No Script Interact",
                            "I have nothing to say to you at this time."));
            else if (!theScript.equals("none")) ParseScript(thisDenizen, event.getPlayer(),
                    GetScriptName(theScript), event.getMessage(), Trigger.CHAT);
        }
    }



    /* GetDenizensWithinRange
      *
      * Requires Player Location, Player World, Range in blocks.
      * Compiles a list of NPCs with a character type of Denizen
      * within range of the Player.
      *
      * Returns DenizensWithinRange List<NPC>
      */

    public List<NPC> GetDenizensWithinRange (Location PlayerLocation,
                                             World PlayerWorld, int Range) {

        List<NPC> DenizensWithinRange = new ArrayList<NPC>();
        Collection<NPC> DenizenNPCs = CitizensAPI.getNPCManager().
                getNPCs(DenizenCharacter.class);
        if (DenizenNPCs.isEmpty()) return DenizensWithinRange;
        List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
        for (NPC aDenizenList : DenizenList) {
            if (aDenizenList.getBukkitEntity().getWorld().equals(PlayerWorld)) {
                if (aDenizenList.getBukkitEntity().getLocation().distance(PlayerLocation) < Range)
                    DenizensWithinRange.add(aDenizenList);
            }
        }
        return DenizensWithinRange;
    }



    /* TalkToNPC
      *
      * Requires the NPC Denizen, Player, and the Message to relay.
      * Sends the message from Player to Denizen with the formatting
      * as specified in the config.yml talk_to_npc_string.
      *
      * <NPC> and <TEXT> are replaced with corresponding information.
      */

    public void TalkToNPC(NPC theDenizen, Player thePlayer, String theMessage)
    {
        thePlayer.sendMessage(Denizen.ChatToNPCString.replace("<NPC>", theDenizen.getName()).
                replace("<TEXT>", theMessage));
    }


    /* GetInteractScript
      *
      * Requires the Denizen and the Player
      * Checks the Denizens scripts and returns the script that meets requirements and has
      * the highest weight.  If no script matches, returns "none".
      *
      * Returns theScript
      * Calls CheckRequirements
      */

    public String GetInteractScript(NPC thisDenizen, Player thisPlayer) {
        String theScript = "none";
        List<String> ScriptList = plugin.getConfig().getStringList("Denizens." + thisDenizen.getName()
                + ".Scripts");
        if (ScriptList.isEmpty()) { return theScript; }
        List<String> ScriptsThatMeetRequirements = new ArrayList<String>();
        // Get scripts that meet requirements
        for (String thisScript : ScriptList) {
            String [] thisScriptArray = thisScript.split(" ", 2);
            if (CheckRequirements(thisScriptArray[1], thisPlayer))
                ScriptsThatMeetRequirements.add(thisScript);

        }
        // Get highest scoring script
        if (ScriptsThatMeetRequirements.size() > 1) {

            int ScriptPriority = -1;

            for (String thisScript : ScriptsThatMeetRequirements) {
                String [] thisScriptArray = thisScript.split(" ", 2);
                if (Integer.parseInt(thisScriptArray[0]) > ScriptPriority) {ScriptPriority =
                        Integer.parseInt(thisScriptArray[0]); theScript = thisScriptArray[1]; }
            }
        }
        else if (ScriptsThatMeetRequirements.size() == 1) theScript = ScriptsThatMeetRequirements.get(0);

        return theScript;
    }



    /* ParseScript
      *
      * Requires the Player, the Script Name, the chat message (if Chat Trigger, otherwise send null),
      * and the Trigger ENUM type.
      * Sends out methods that take action based on the Trigger types.
      *
      * case CHAT calls GetCurrentStep, GetChatTriggers, TriggerChatToQue
      * case CLICK,RIGHT_CLICK,LEFT_CLICK calls
      * case FINISH calls
      * case START calls
      * case FAIL calls
      */


    public void ParseScript(NPC theDenizen, Player thePlayer, String theScript,
                            String theMessage,  Trigger theTrigger) {

        switch (theTrigger) {

            case CHAT:
                int CurrentStep = GetCurrentStep(thePlayer, theScript);
                List<String> ChatTriggerList = GetChatTriggers(theScript, CurrentStep);
                for (int l=0; l < ChatTriggerList.size(); l++ ) {
                    if (theMessage.matches(ChatTriggerList.get(l))) {
                        TriggerChatToQue(theScript, CurrentStep, l, thePlayer, theDenizen);
                    }
                }
                return;

            case CLICK:
                // get current progression
                // send script
                return;

            case FINISH:
                // get current progressions
                // send script

        }
    }



    /* TriggerChatToQue
      *
      * Requires the Script, the Current Step, the Chat Trigger to trigger, and the Player
      * Triggers the script for the chat trigger of the step and script specified.
      *
      * Calls ScriptHandler to handle the commands in the script. ScriptHandler returns any
      * raw text that needs to be sent to the player which is put in the PlayerQue for
      * output.
      */

    public void TriggerChatToQue(String theScript, int CurrentStep, int ChatTrigger, Player thePlayer,
                                 NPC theDenizen) {

        List<String> CurrentPlayerQue = new ArrayList<String>();
        if (Denizen.PlayerQue.get(thePlayer) != null) CurrentPlayerQue = Denizen.PlayerQue.get(thePlayer);
        Denizen.PlayerQue.remove(thePlayer);  // Should keep the talk queue from triggering mid-add
        List<String> AddedToPlayerQue = plugin.getConfig().getStringList("Scripts." + theScript + ".Steps."
                + CurrentStep + ".Interact.Chat Trigger." + ChatTrigger + ".Script");

        if (!AddedToPlayerQue.isEmpty()) {

            for (String theCommand : AddedToPlayerQue) {


                // Longer than 40, probably a long chat that needs multiline formatting.
                if (theCommand.length() > 40) {

                    String[] theCommandText;
                    theCommandText = theCommand.split(" ");

                    switch (Command.valueOf(theCommandText[0].toUpperCase())) {
                        case SHOUT:
                        case CHAT:
                        case WHISPER:
                        case ANNOUNCE:
                        case NARRARATE:
                            int word = 1;
                            int line = 0;
                            String[] multiLineCommand = new String[0];
                            multiLineCommand[0] = theCommandText[0];

                            while (word < theCommandText.length) {

                                if (multiLineCommand[line].length() + theCommandText[word].length() < 40) {
                                    multiLineCommand[line] = multiLineCommand[line] + " " + theCommandText[word];
                                    word++;
                                }
                                else {
                                    line++; multiLineCommand[line] = theCommandText[0];
                                }
                            }

                            for (String eachCommand : multiLineCommand) {
                                CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";"
                                        + Integer.toString(CurrentStep) + ";CHAT;" + eachCommand);
                            }
                    }
                }

                else CurrentPlayerQue.add(Integer.toString(theDenizen.getId()) + ";" + theScript + ";"
                        + Integer.toString(CurrentStep) + ";CHAT;" + theCommand);
            }
            Denizen.PlayerQue.put(thePlayer, CurrentPlayerQue);
        }
    }




    public static void CommandExecuter(Player thePlayer, String theStep) {

        // Syntax of theStep
        // 0 Denizen ID; 1 Script Name; 2 Step Number; 3 Trigger Type; 4 Command

        String[] splitArgs = theStep.split(";");
        String[] splitCommand = splitArgs[4].split(" ");

        switch (Command.valueOf(splitCommand[0].toUpperCase())) {

            // SCRIPT INTERACTION

            case ZAP:  // ZAP [Optional Step # to advance to]
                if (splitCommand.length == 1)  { plugin.getConfig().set("Players." + thePlayer.getDisplayName()
                        + "." + splitArgs[1] + ".Current Step", Integer.parseInt(splitArgs[2]) + 1);
                    plugin.saveConfig();
                }
                else { plugin.getConfig().set("Players." + thePlayer.getDisplayName() + "." + splitArgs[1]
                        + ".Current Step", Integer.parseInt(splitCommand[1])); plugin.saveConfig(); }
                break;

            case ASSIGN:  // ASSIGN [ME|Denizen Name] [ALL|Player Name] [Priority] [Script Name]
            case UNASSIGN:  // DEASSIGN [ME|Denizen Name] [ALL|Player Name] [Script Name]
            case C2SCRIPT:  // Runs a CitizenScript


                // WORLD INTERACTION

            case SPAWN:  // SPAWN [MOB NAME] [AMOUNT] (Location Bookmark)
            case CHANGE:  // CHANGE [Block State Bookmark]
            case WEATHER:  // WEATHER [Sunny|Stormy|Rainy] (Duration for Stormy/Rainy)
                if (splitCommand[1].equalsIgnoreCase("sunny")) { thePlayer.getWorld().setStorm(false); }
                else if (splitCommand[1].equalsIgnoreCase("stormy")) { thePlayer.getWorld().setThundering(true); }
                else if (splitCommand[1].equalsIgnoreCase("rainy")) { thePlayer.getWorld().setStorm(true); }
                break;

            case EFFECT:  // EFFECT [EFFECT_TYPE] (Location Bookmark)


                // PLAYER INTERACTION

            case GIVE:  // GIVE [Item:Data] [Amount] [ENCHANTMENT_TYPE]
            case TAKE:  // TAKE [Item] [Amount]   or  TAKE ITEM_IN_HAND  or  TAKE MONEY [Amount]
                // or  TAKE ENCHANTMENT  or  TAKE INVENTORY
            case HEAL:
            case DAMAGE:
            case POTION_EFFECT:
            case TELEPORT:  // TELEPORT [Location Notable] (Effect)
                // or TELEPORT [X,Y,Z] (World Name) (Effect)
            case STRIKE:  // STRIKE    Strikes lightning on the player, with damage.
                thePlayer.getWorld().strikeLightning(thePlayer.getLocation());
                break;
            // DENIZEN INTERACTION

            case WALK:  // MOVE [ME|Denizen Name] [Location Notable]
            case NOD:  // NOD [ME]
            case REMEMBER:  // REMEMBER [CHAT|LOCATION|INVENTORY]
            case BOUNCE:  // BOUNCE PLAYER
            case RESPAWN:  // RESPAWN [ME|Denizen Name] [Location Notable]
            case PERMISS:  // PERMISS [Optional Step # to advance to]

            case EXECUTE:  // EXECUTE [Optional Step # to advance to]
                thePlayer.getServer().dispatchCommand(null, splitArgs[4].split(" ", 2)[1]);
                break;

            // SHOUT can be heard by players within 100 blocks.
            // WHISPER can only be heard by the player interacting with.
            // CHAT can be heard by the player, and players within 5 blocks.
            // NARRARATE can only be heard by the player and is not branded by the NPC.
            // ANNOUNCE can be heard by the entire server.

            case WHISPER:  // ZAP [Optional Step # to advance to]


            case NARRARATE:  // ZAP [Optional Step # to advance to]



            case SHOUT:  // ZAP [Optional Step # to advance to]
            case CHAT:  // CHAT [Message]
                thePlayer.sendMessage(splitArgs[4].split(" ", 2)[1]);
                break;
            case ANNOUNCE: // ANNOUNCE [Message]



                // NOTABLES

            case GRANT:  // NOTABLE [Name of Notable to Grant]

        }
    }




    /* GetCurrentStep
      *
      * Requires the Player and the Script.
      * Reads the config.yml to find the current step that the player is on
      * for the specified script.
      *
      * Returns currentStep
      */

    public int GetCurrentStep(Player thePlayer, String theScript) {
        int currentStep = 0;
        if (plugin.getConfig().getString("Players." + thePlayer + "." + theScript + "." + "Current Step")
                != null) currentStep =  plugin.getConfig().getInt("Players." + thePlayer + "." + theScript
                + "." + "Current Step");
        return currentStep;
    }


    /* GetScriptCompletes
      *
      * Requires the Player and the Script.
      * Reads the config.yml to find if the player has completed
      * the specified script.
      *
      * Returns number of times script has been completed.
      */

    public int GetScriptCompletes(Player thePlayer, String theScript) {
        int ScriptCompletes = 0;
        if (plugin.getConfig().getString("Players." + thePlayer + "." + theScript + "." + "Completes")
                != null) ScriptCompletes =  plugin.getConfig().getInt("Players." + thePlayer + "."
                + theScript + "." + "Completes");
        return ScriptCompletes;
    }


    /* GetNotableCompletion
      *
      * Requires the Player and the Script.
      * Reads the config.yml to find if the player has completed
      * the specified script.
      *
      * Returns number of times script has been completed.
      */

    public boolean GetNotableCompletion(Player thePlayer, String theNotable) {
        return plugin.getConfig().getStringList("Notables.Players." + thePlayer + "." + theNotable).
                contains(theNotable);
    }


    /* GetChatTriggers
      *
      * Requires the Script and the Current Step.
      * Gets a list of Chat Triggers for the step of the script specified.
      * Chat Triggers are words required to trigger one of the chat scripts.
      *
      * Returns ChatTriggers
      */

    public List<String> GetChatTriggers(String theScript, Integer currentStep) {
        List<String> ChatTriggers = new ArrayList<String>();
        int currentTrigger = 0;
        // Add triggers to list
        for (int x=0; currentTrigger >= 0; x++) {
            String theChatTrigger = plugin.getConfig().getString("Scripts." + theScript + ".Steps."
                    + currentStep + ".Interact.Chat Trigger." + String.valueOf(currentTrigger) + ".Trigger");
            if (theChatTrigger != null) { ChatTriggers.add(theChatTrigger); currentTrigger = x + 1; }
            else currentTrigger = -1;
        }
        return ChatTriggers;
    }



    /* GetScriptName
      *
      * Requires the raw script entry from the config.
      * Strips the priority number from the beginning of the script name.
      *
      * Returns the Script Name
      */

    public String GetScriptName(String thisScript) {
        if (thisScript.equals("none")) { return thisScript; }
        else {
            String [] thisScriptArray = thisScript.split(" ", 2);
            return thisScriptArray[1]; }
    }



    // CHECK REQUIREMENTS  (Checks if the requirements of a script are met when given Script/Player)

    public boolean CheckRequirements(String thisScript, Player thisPlayer) {

        String RequirementsMode = plugin.getConfig().getString("Scripts." + thisScript + ".Requirements.Mode");

        List<String> RequirementsList = plugin.getConfig().getStringList("Scripts." + thisScript
                + ".Requirements.List");
        if (RequirementsList.isEmpty()) { return true; }
        int MetReqs = 0;
        boolean negReq;
        for (String RequirementArgs : RequirementsList) {
            if (RequirementArgs.startsWith("-")) { negReq = true; RequirementArgs = RequirementArgs.substring(1); }
            else negReq = false;
            String[] splitArgs = RequirementArgs.split(" ", 2);
            switch (Requirement.valueOf(splitArgs[0].toUpperCase())) {

                case NONE:
                    return true;

                case TIME: // (-)TIME DAY   or  (-)TIME NIGHT    Note: DAY = 0, NIGHT = 16000
                    // or (-)TIME [At least this Time 0-23999] [But no more than this Time 1-24000]

                    if (negReq) {
                        if (splitArgs[1].equalsIgnoreCase("DAY")) if (thisPlayer.getWorld().getTime() > 16000)
                            MetReqs++;
                        if (splitArgs[1].equalsIgnoreCase("NIGHT")) if (thisPlayer.getWorld().getTime() < 16000)
                            MetReqs++;
                        else {
                            String[] theseTimes = splitArgs[1].split(" ");
                            if (thisPlayer.getWorld().getTime() < Integer.parseInt(theseTimes[0]) && thisPlayer.
                                    getWorld().getTime() > Integer.parseInt(theseTimes[1])) MetReqs++;
                        }
                    } else {
                        if (splitArgs[1].equalsIgnoreCase("DAY")) if (thisPlayer.getWorld().getTime() < 16000)
                            MetReqs++;
                        if (splitArgs[1].equalsIgnoreCase("NIGHT")) if (thisPlayer.getWorld().getTime() > 16000)
                            MetReqs++;
                        else {
                            String[] theseTimes = splitArgs[1].split(" ");
                            if (thisPlayer.getWorld().getTime() >= Integer.parseInt(theseTimes[0]) && thisPlayer.
                                    getWorld().getTime() <= Integer.parseInt(theseTimes[1])) MetReqs++;
                        }
                    }
                    break;

                case PERMISSION:  // (-)PERMISSION [this.permission.node]
                    if (negReq) if (!Denizen.perms.playerHas(thisPlayer.getWorld(), thisPlayer.toString(),
                            splitArgs[1])) MetReqs++;
                    else if (Denizen.perms.playerHas(thisPlayer.getWorld(), thisPlayer.toString(),
                            splitArgs[1])) MetReqs++;
                    break;

                case PRECIPITATION:  // (-)PRECIPITATION
                    if (negReq) if (!thisPlayer.getWorld().hasStorm()) MetReqs++;
                    else if (thisPlayer.getWorld().hasStorm()) MetReqs++;
                    break;

                case HUNGER:  // (-)HUNGER FULL  or  (-)HUNGER HUNGRY  or  (-)HUNGER STARVING
                    if (negReq) {
                        if (splitArgs[1].equalsIgnoreCase("FULL")) if (thisPlayer.getFoodLevel() < 20) MetReqs++;
                        if (splitArgs[1].equalsIgnoreCase("HUNGRY")) if (thisPlayer.getFoodLevel() >= 20) MetReqs++;
                        if (splitArgs[1].equalsIgnoreCase("STARVING")) if (thisPlayer.getFoodLevel() > 1) MetReqs++;
                    } else {
                        if (splitArgs[1].equalsIgnoreCase("FULL")) if (thisPlayer.getFoodLevel() >= 20) MetReqs++;
                        if (splitArgs[1].equalsIgnoreCase("HUNGRY")) if (thisPlayer.getFoodLevel() < 18) MetReqs++;
                        if (splitArgs[1].equalsIgnoreCase("STARVING")) if (thisPlayer.getFoodLevel() < 1) MetReqs++;
                    }
                    break;

                case LEVEL:  // (-)LEVEL [This Level # or higher]
                    // or  (-)LEVEL [At least this Level #] [But no more than this Level #]
                    if (negReq) {
                        if (Array.getLength(splitArgs[1].split(" ")) == 1) {
                            if (thisPlayer.getLevel() < Integer.parseInt(splitArgs[1])) MetReqs++;
                        } else {
                            String[] theseLevels = splitArgs[1].split(" ");
                            if (thisPlayer.getLevel() < Integer.parseInt(theseLevels[0]) && thisPlayer.getLevel()
                                    > Integer.parseInt(theseLevels[1])) MetReqs++;
                        }
                    } else {
                        if (Array.getLength(splitArgs[1].split(" ")) == 1) {
                            if (thisPlayer.getLevel() >= Integer.parseInt(splitArgs[1])) MetReqs++;
                        } else {
                            String[] theseLevels = splitArgs[1].split(" ");
                            if (thisPlayer.getLevel() >= Integer.parseInt(theseLevels[0]) && thisPlayer.getLevel()
                                    <= Integer.parseInt(theseLevels[1])) MetReqs++;
                        }
                    }
                    break;

                case NOTABLE: // (-)NOTABLE [Name of Notable]
                    if (negReq) if (!GetNotableCompletion(thisPlayer, splitArgs[1])) MetReqs++;
                    else if (GetNotableCompletion(thisPlayer, splitArgs[1])) MetReqs++;
                    break;

                case WORLD:  // (-)WORLD [World Name] [or this World Name] [or this World...]
                    String[] theseWorlds = splitArgs[1].split(" ");
                    if (negReq) {
                        boolean tempMet = true;
                        for (String thisWorld : theseWorlds) {
                            if (thisPlayer.getWorld().getName().equalsIgnoreCase(thisWorld)) tempMet = false;
                        }
                        if (tempMet) MetReqs++;
                    } else {
                        for (String thisWorld : theseWorlds) {
                            if (thisPlayer.getWorld().getName().equalsIgnoreCase(thisWorld)) MetReqs++;
                        }
                    }
                    break;

                case NAME:  // (-)Name [Name] [or this Name] [or this Name, etc...]
                    String[] theseNames = splitArgs[1].split(" ");
                    if (negReq) {
                        boolean tempMet = true;
                        for (String thisName : theseNames) {
                            if (thisPlayer.getName().equalsIgnoreCase(thisName)) tempMet = false;
                        }
                        if (tempMet) MetReqs++;
                    } else {
                        for (String thisName : theseNames) {
                            if (thisPlayer.getName().equalsIgnoreCase(thisName)) MetReqs++;
                        }
                    }
                    break;

                case STORMY:  // (-)STORMY     - Note that it can still be raining and this will trigger
                    if (negReq) if (!thisPlayer.getWorld().isThundering()) MetReqs++;
                    else if (thisPlayer.getWorld().isThundering()) MetReqs++;
                    break;

                case SUNNY:  // (-)SUNNY    - Negative would trigger on Raining or Storming
                    if (negReq) if (thisPlayer.getWorld().hasStorm()) MetReqs++;
                    else if (!thisPlayer.getWorld().hasStorm()) MetReqs++;
                    break;

                case MONEY: // (-)MONEY [Amount of Money, or more]
                    if (negReq) if (!Denizen.econ.has(thisPlayer.toString(), Integer.parseInt(splitArgs[1]))) MetReqs++;
                    else if (Denizen.econ.has(thisPlayer.toString(), Integer.parseInt(splitArgs[1]))) MetReqs++;
                    break;

                case ITEM: // (-)ITEM [ITEM_NAME] [# of that item, or more] [ENCHANTMENT_TYPE]
                    String[] theseItemArgs = splitArgs[1].split(" ");
                    ItemStack thisItem = new ItemStack(Material.getMaterial(theseItemArgs[0]),
                            Integer.parseInt(theseItemArgs[1]));
                    Map<Material, Integer> PlayerInv = new HashMap<Material, Integer>();
                    Map<Material, Boolean> isEnchanted = new HashMap<Material, Boolean>();

                    for (ItemStack invItem : thisPlayer.getInventory()) {
                        if (PlayerInv.containsKey(invItem.getType())) {
                            int t = PlayerInv.get(invItem.getType());
                            t = t + invItem.getAmount(); PlayerInv.put(invItem.getType(), t);
                        }
                        else PlayerInv.put(invItem.getType(), invItem.getAmount());
                        if (!theseItemArgs[2].isEmpty())
                            if (invItem.containsEnchantment(Enchantment.getByName(theseItemArgs[2])))
                                isEnchanted.put(invItem.getType(), true);
                    }

                    if (negReq) {
                        if (PlayerInv.containsKey(thisItem.getType()) && theseItemArgs[2].isEmpty())
                            if (PlayerInv.get(thisItem.getType()) < thisItem.getAmount()) MetReqs++;
                            else if (PlayerInv.containsKey(thisItem.getType()) && isEnchanted.get(thisItem.getType()))
                                if (PlayerInv.get(thisItem.getType()) < thisItem.getAmount()) MetReqs++;
                    }
                    else {
                        if (PlayerInv.containsKey(thisItem.getType()) && theseItemArgs[2].isEmpty())
                            if (PlayerInv.get(thisItem.getType()) >= thisItem.getAmount()) MetReqs++;
                            else if (PlayerInv.containsKey(thisItem.getType()) && isEnchanted.get(thisItem.getType()))
                                if (PlayerInv.get(thisItem.getType()) >= thisItem.getAmount()) MetReqs++;
                    }
                    break;

                case HOLDING: // (-)HOLDING [ITEM_NAME] [ENCHANTMENT_TYPE]
                    String[] itemArgs = splitArgs[1].split(" ");
                    if (negReq) if (!thisPlayer.getItemInHand().getType().equals(Material.getMaterial(itemArgs[0]))) {
                        if (itemArgs[1] == null) MetReqs++;
                        else if (!thisPlayer.getItemInHand().getEnchantments().containsKey(Enchantment.getByName(itemArgs[1])))
                            MetReqs++;
                    } else {
                        if (itemArgs[1] == null) MetReqs++;
                        else if (thisPlayer.getItemInHand().getEnchantments().containsKey(Enchantment.getByName(itemArgs[1])))
                            MetReqs++;
                    }
                    break;

                case POTIONEFFECT: // (-)POTIONEFFECT [POTION_EFFECT_TYPE]
                    if (negReq) if (!thisPlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) MetReqs++;
                    else if (thisPlayer.hasPotionEffect(PotionEffectType.getByName(splitArgs[1]))) MetReqs++;
                    break;

                case SCRIPT: // (-)SCRIPT [Script Name] [Number of times completed, or more]
                    if (negReq) if (GetScriptCompletes(thisPlayer, splitArgs[1])
                            > Integer.parseInt(splitArgs[2])) MetReqs++;
                    else if (GetScriptCompletes(thisPlayer, splitArgs[1])
                            <= Integer.parseInt(splitArgs[2])) MetReqs++;
                    break;

                case GROUP:
                    if (negReq) if (!Denizen.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.toString(),
                            splitArgs[1])) MetReqs++;
                    else if (Denizen.perms.playerInGroup(thisPlayer.getWorld(), thisPlayer.toString(),
                            splitArgs[1])) MetReqs++;
                    break;
            }
        }
        if (RequirementsMode.equalsIgnoreCase("all") && MetReqs == RequirementsList.size()) return true;
        String[] ModeArgs = RequirementsMode.split(" ");
        if (ModeArgs[0].equalsIgnoreCase("any") && MetReqs >= Integer.parseInt(ModeArgs[1])) return true;

        return false;
    }


}