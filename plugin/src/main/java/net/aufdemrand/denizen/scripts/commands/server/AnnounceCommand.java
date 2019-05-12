package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AnnounceCommand extends AbstractCommand {

    enum AnnounceType {ALL, TO_OPS, TO_FLAGGED, TO_CONSOLE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Users tend to forget quotes sometimes on commands like this, so
        // let's check if there are more argument than usual.
        if (scriptEntry.getArguments().size() > 3) {
            throw new InvalidArgumentsException("Too many arguments! Did you forget a 'quote'?");
        }

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("type")
                    && arg.matches("to_ops")) {
                scriptEntry.addObject("type", AnnounceType.TO_OPS);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("to_console")) {
                scriptEntry.addObject("type", AnnounceType.TO_CONSOLE);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matchesPrefix("to_flagged")) {
                scriptEntry.addObject("type", AnnounceType.TO_FLAGGED);
                scriptEntry.addObject("flag", arg.asElement());
            }
            else if (!scriptEntry.hasObject("format")
                    && arg.matchesPrefix("format")) {
                FormatScriptContainer format = null;
                String formatStr = arg.getValue();
                format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    dB.echoError("Could not find format script matching '" + formatStr + '\'');
                }
                scriptEntry.addObject("format", format);
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new Element(arg.raw_value));
            }

        }

        // If text is missing, alert the console.
        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Missing text argument!");
        }

        scriptEntry.defaultObject("type", AnnounceType.ALL);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Fetch objects
        Element text = scriptEntry.getElement("text");
        AnnounceType type = (AnnounceType) scriptEntry.getObject("type");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");
        Element flag = scriptEntry.getElement("flag");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    aH.debugObj("Message", text)
                            + (format != null ? aH.debugObj("Format", format.getName()) : "")
                            + aH.debugObj("Type", type.name())
                            + (flag != null ? aH.debugObj("Flag_Name", flag) : ""));
        }

        String message = format != null ? format.getFormattedText(scriptEntry) : text.asString();

        // Use Bukkit to broadcast the message to everybody in the server.
        if (type == AnnounceType.ALL) {
            DenizenAPI.getCurrentInstance().getServer().broadcastMessage(message);
        }
        else if (type == AnnounceType.TO_OPS) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage(message);
                }
            }
        }
        else if (type == AnnounceType.TO_FLAGGED) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (FlagManager.playerHasFlag(dPlayer.mirrorBukkitPlayer(player), flag.asString())) {
                    player.sendMessage(message);
                }
            }
        }
        else if (type == AnnounceType.TO_CONSOLE) {
            Bukkit.getServer().getConsoleSender().sendMessage(message);
        }
    }
}


