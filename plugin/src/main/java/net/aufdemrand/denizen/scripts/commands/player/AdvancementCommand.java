package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.AdvancementHelper;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdvancementCommand extends AbstractCommand {

    private enum Frame {CHALLENGE, GOAL, TASK}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("targets")
                    && arg.matchesPrefix("target", "targets", "t")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class));
            }
            else if (!scriptEntry.hasObject("description")
                    && arg.matchesPrefix("description", "d")) {
                scriptEntry.addObject("description", arg.asElement());
            }
            else if (!scriptEntry.hasObject("icon")
                    && arg.matchesPrefix("icon", "i")
                    && arg.matchesArgumentType(dMaterial.class)) {
                scriptEntry.addObject("icon", arg.asType(dMaterial.class));
            }
            else if (!scriptEntry.hasObject("frame")
                    && arg.matchesPrefix("frame", "f")
                    && arg.matchesEnum(Frame.values())) {
                scriptEntry.addObject("frame", arg.asElement());
            }
            else if (!scriptEntry.hasObject("toast")
                    && arg.matches("hide_toast")) {
                scriptEntry.addObject("toast", Element.FALSE);
            }
            else if (!scriptEntry.hasObject("announce")
                    && arg.matches("announce")) {
                scriptEntry.addObject("announce", Element.TRUE);
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new Element(arg.raw_value));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Must specify a message!");
        }

        if (!scriptEntry.hasObject("targets")) {
            BukkitScriptEntryData data = (BukkitScriptEntryData) scriptEntry.entryData;
            if (!data.hasPlayer()) {
                throw new InvalidArgumentsException("Must specify valid player targets!");
            }
            else {
                scriptEntry.addObject("targets",
                        Arrays.asList(data.getPlayer()));
            }
        }

        scriptEntry.defaultObject("icon", dMaterial.AIR);
        scriptEntry.defaultObject("description", new Element("A custom Denizen advancement."));
        scriptEntry.defaultObject("frame", new Element("task"));
        scriptEntry.defaultObject("toast", Element.TRUE);
        scriptEntry.defaultObject("announce", Element.FALSE);

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Element text = scriptEntry.getElement("text");
        Element description = scriptEntry.getElement("description");
        Element frame = scriptEntry.getElement("frame");
        Element toast = scriptEntry.getElement("toast");
        Element announce = scriptEntry.getElement("announce");
        dMaterial icon = scriptEntry.getdObject("icon");
        List<dPlayer> players = (List<dPlayer>) scriptEntry.getObject("targets");

        List<Player> bukkitPlayers = new ArrayList<Player>();
        for (dPlayer player : players) {
            bukkitPlayers.add(player.getPlayerEntity());
        }

        AdvancementHelper advancement = new AdvancementHelper(
                text.asString(), description.asString(), icon.getMaterial(),
                frame.asString(), announce.asBoolean(), toast.asBoolean());
        advancement.showTo(bukkitPlayers);
    }
}
