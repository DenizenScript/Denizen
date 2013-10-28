package net.aufdemrand.denizen.scripts.commands.entity;

import java.util.List;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.traits.NameplateTrait;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 * Modifies the nameplate of the given NPC
 *
 * @author spaceemotion
 */
public class NameplateCommand extends AbstractCommand {

    /* Example usage:
     * - NAMEPLATE COLOR:RED
     * - NAMEPLATE COLOR:GOLD PLAYER:Notch
     *
     * Arguments: [] - Required, () - Optional
     *
     * [COLOR] The color to set. See the Bukkit documentation for available colors.
     *
     * (PLAYER) The player to apply the change to (can be per-player!).
     *
     */

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        ChatColor color = null;
        boolean player = false;
        String text = null;

        List<String> args = scriptEntry.getArguments();

        // TODO: Update this command!

        for(String arg : args) {
            if(aH.matchesValueArg("COLOR", arg, ArgumentType.String))
                try { color = ChatColor.valueOf(aH.getStringFrom(arg).toUpperCase()); } catch( Exception e)  {
                    dB.echoDebug(scriptEntry, "...COLOR could not be set: '" + aH.getStringFrom(arg) + "' is an invalid color!"); }

            else if(aH.matchesValueArg("TARGET", arg, ArgumentType.Word)) {
                player = true;
                scriptEntry.setPlayer(aH.getPlayerFrom(arg));
            }

            else if (aH.matchesValueArg("SET", arg, ArgumentType.Custom))
                text = aH.getStringFrom(arg);
        }

        scriptEntry.addObject("color", color)
                .addObject("player", player)
                .addObject("text", text);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Boolean player = (Boolean) scriptEntry.getObject("player");
        ChatColor color = (ChatColor) scriptEntry.getObject("color");
        String text = (String) scriptEntry.getObject("text");




        if (text != null) {
            if (text.equalsIgnoreCase("none")) {
                scriptEntry.getNPC().getEntity().setCustomNameVisible(false);
                dB.echoDebug(scriptEntry, "none");
            } else {
                scriptEntry.getNPC().getEntity().setCustomNameVisible(true);
                scriptEntry.getNPC().getEntity().setCustomName(text);
                dB.echoDebug(scriptEntry, text);
            }

            if (scriptEntry.getNPC().getEntity() instanceof Player)
                ((Player) scriptEntry.getNPC().getEntity()).setDisplayName(text);
        }

        if(color != null) {
            if (!scriptEntry.getNPC().getCitizen().hasTrait(NameplateTrait.class))
                scriptEntry.getNPC().getCitizen().addTrait(NameplateTrait.class);
            NameplateTrait trait = scriptEntry.getNPC().getCitizen().getTrait(NameplateTrait.class);

            if (player) trait.setColor(color, scriptEntry.getPlayer().getName());
            else trait.setColor(color);
        }

    }

}
