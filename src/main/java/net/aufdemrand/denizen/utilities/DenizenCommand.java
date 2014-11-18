package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.containers.core.CommandScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class DenizenCommand extends Command {

    private CommandScriptContainer script;

    public DenizenCommand(CommandScriptContainer script) {
        super(script.getCommandName(), script.getDescription(), script.getUsage(), script.getAliases());
        String permission = script.getPermission();
        if (permission != null && !permission.equals("")) {
            this.setPermission(permission);
            String permissionMessage = script.getPermissionMessage();
            if (permissionMessage != null && !permissionMessage.equals(""))
                this.setPermissionMessage(permissionMessage);
        }
        this.script = script;
    }

    public boolean canSeeHelp(CommandSender commandSender) {
        if (!script.hasAllowedHelpProcedure()) return true;
        if (!testPermissionSilent(commandSender)) return false;
        Map<String, dObject> context = new HashMap<String, dObject>();
        dPlayer player = null;
        dNPC npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (Depends.citizens == null || !CitizensAPI.getNPCRegistry().isNPC(pl))
                player = dPlayer.mirrorBukkitPlayer(pl);
            context.put("server", Element.FALSE);
        }
        else {
            context.put("server", Element.TRUE);
        }
        return script.runAllowedHelpProcedure(player, npc, context);
    }

    @Override
    public boolean testPermission(CommandSender target) {
        if (testPermissionSilent(target)) {
            return true;
        }

        String permissionMessage = getPermissionMessage();
        if (permissionMessage == null) {
            target.sendMessage(ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. "
                    + "Please contact the server administrators if you believe that this is in error.");
        } else if (permissionMessage.length() != 0) {
            dPlayer player = null;
            dNPC npc = null;
            if (target instanceof Player) {
                Player pl = (Player) target;
                if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(pl))
                    npc = dNPC.fromEntity(pl);
                else
                    player = dPlayer.mirrorBukkitPlayer(pl);
            }
            if (Depends.citizens != null && npc == null) {
                NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(target);
                if (citizen != null)
                    npc = dNPC.mirrorCitizensNPC(citizen);
            }
            // <permission> is built into Bukkit... let's keep it here
            for (String line : TagManager.tag(player, npc, permissionMessage.replace("<permission>", getPermission()))
                    .split("\n")) {
                target.sendMessage(line);
            }
        }

        return false;
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
            context.put("server", Element.FALSE);
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

    @Override
    public List<String> tabComplete(CommandSender commandSender, String alias, String[] arguments) {
        if (!script.hasTabCompleteProcedure()) return super.tabComplete(commandSender, alias, arguments);
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
        context.put("alias", new Element(alias));
        dPlayer player = null;
        dNPC npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(pl))
                npc = dNPC.fromEntity(pl);
            else
                player = dPlayer.mirrorBukkitPlayer(pl);
            context.put("server", Element.FALSE);
        }
        else {
            context.put("server", Element.TRUE);
        }
        if (Depends.citizens != null && npc == null) {
            NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(commandSender);
            if (citizen != null)
                npc = dNPC.mirrorCitizensNPC(citizen);
        }
        return script.runTabCompleteProcedure(player, npc, context);
    }
}
