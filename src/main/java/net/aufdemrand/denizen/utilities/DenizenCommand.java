package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.containers.core.CommandScriptContainer;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class DenizenCommand extends Command {

    private CommandScriptContainer script;

    public DenizenCommand(CommandScriptContainer script) {
        super(script.getCommandName(), script.getDescription(), script.getUsage(), script.getAliases());
        this.script = script;
    }

    public boolean canSeeHelp(CommandSender commandSender) {
        if (!script.hasAllowedHelpProcedure()) return true;
        Map<String, dObject> context = new HashMap<String, dObject>();
        dPlayer player = null;
        dNPC npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (Depends.citizens == null || !CitizensAPI.getNPCRegistry().isNPC(pl))
                player = dPlayer.mirrorBukkitPlayer(pl);
        }
        else {
            context.put("server", Element.TRUE);
        }
        return script.runAllowedHelpProcedure(player, npc, context);
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] arguments) {
        Map<String, dObject> context = new HashMap<String, dObject>();
        String raw_args = "";
        if (arguments.length > 0) {
            StringBuilder rawArgsBuilder = new StringBuilder();
            for (String arg : arguments) {
                rawArgsBuilder.append(arg).append(' ');
            }
            raw_args = rawArgsBuilder.substring(0, rawArgsBuilder.length() - 1);
        }
        List<String> args = Arrays.asList(aH.buildArgs(raw_args));
        context.put("args", new dList(args));
        context.put("raw_args", new Element(raw_args));
        dPlayer player = null;
        dNPC npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(pl))
                npc = dNPC.fromEntity(pl);
            else
                player = dPlayer.mirrorBukkitPlayer(pl);
        }
        else {
            context.put("server", Element.TRUE);
        }
        if (Depends.citizens != null && npc == null) {
            NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(commandSender);
            if (citizen != null)
                npc = dNPC.mirrorCitizensNPC(citizen);
        }
        script.runCommandScript(player, npc, context);
        return true;
    }

    @Override
    public boolean isRegistered() {
        return true;
    }

}
