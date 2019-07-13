package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.scripts.containers.core.CommandScriptContainer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.tags.TagManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DenizenCommand extends Command {

    private CommandScriptContainer script;

    public DenizenCommand(CommandScriptContainer script) {
        super(script.getCommandName(), script.getDescription(), script.getUsage(), script.getAliases());
        String permission = script.getPermission();
        if (permission != null && !permission.equals("")) {
            this.setPermission(permission);
            String permissionMessage = script.getPermissionMessage();
            if (permissionMessage != null && !permissionMessage.equals("")) {
                this.setPermissionMessage(permissionMessage);
            }
        }
        this.script = script;
    }

    public boolean canSeeHelp(CommandSender commandSender) {
        if (!script.hasAllowedHelpProcedure()) {
            return true;
        }
        if (!testPermissionSilent(commandSender)) {
            return false;
        }
        Map<String, ObjectTag> context = new HashMap<>();
        dPlayer player = null;
        dNPC npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (!dEntity.isNPC(pl)) {
                player = dPlayer.mirrorBukkitPlayer(pl);
            }
            context.put("server", new ElementTag(false));
        }
        else {
            context.put("server", new ElementTag(true));
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
        }
        else if (permissionMessage.length() != 0) {
            dPlayer player = null;
            dNPC npc = null;
            if (target instanceof Player) {
                Player pl = (Player) target;
                if (dEntity.isCitizensNPC(pl)) {
                    npc = dNPC.fromEntity(pl);
                }
                else {
                    player = dPlayer.mirrorBukkitPlayer(pl);
                }
            }
            if (Depends.citizens != null && npc == null) {
                NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(target);
                if (citizen != null) {
                    npc = dNPC.mirrorCitizensNPC(citizen);
                }
            }
            // <permission> is built into Bukkit... let's keep it here
            for (String line : TagManager.tag(permissionMessage.replace("<permission>", getPermission()),
                    new BukkitTagContext(player, npc, false, null, false, new ScriptTag(script))).split("\n")) {
                target.sendMessage(line);
            }
        }

        return false;
    }

    @Override
    public boolean execute(CommandSender commandSender, String commandLabel, String[] arguments) {
        if (!testPermission(commandSender)) {
            return true;
        }
        Map<String, ObjectTag> context = new HashMap<>();
        String raw_args = "";
        if (arguments.length > 0) {
            StringBuilder rawArgsBuilder = new StringBuilder();
            for (String arg : arguments) {
                rawArgsBuilder.append(arg).append(' ');
            }
            raw_args = rawArgsBuilder.substring(0, rawArgsBuilder.length() - 1);
        }
        List<String> args = Arrays.asList(ArgumentHelper.buildArgs(raw_args));
        context.put("args", new ListTag(args));
        context.put("raw_args", new ElementTag(raw_args));
        context.put("alias", new ElementTag(commandLabel));
        dPlayer player = null;
        dNPC npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (dEntity.isCitizensNPC(pl)) {
                npc = dNPC.fromEntity(pl);
            }
            else {
                player = dPlayer.mirrorBukkitPlayer(pl);
            }
            context.put("server", new ElementTag(false));
        }
        else {
            context.put("server", new ElementTag(true));
            if (commandSender instanceof BlockCommandSender) {
                context.put("command_block_location", new dLocation(((BlockCommandSender) commandSender).getBlock().getLocation()));
            }
            else if (commandSender instanceof CommandMinecart) {
                context.put("command_minecart", new dEntity((CommandMinecart) commandSender));
            }
        }
        if (Depends.citizens != null && npc == null) {
            NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(commandSender);
            if (citizen != null) {
                npc = dNPC.mirrorCitizensNPC(citizen);
            }
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
        if (!script.hasTabCompleteProcedure()) {
            return super.tabComplete(commandSender, alias, arguments);
        }
        Map<String, ObjectTag> context = new HashMap<>();
        String raw_args = "";
        if (arguments.length > 0) {
            StringBuilder rawArgsBuilder = new StringBuilder();
            for (String arg : arguments) {
                rawArgsBuilder.append(arg).append(' ');
            }
            raw_args = rawArgsBuilder.substring(0, rawArgsBuilder.length() - 1);
        }
        List<String> args = Arrays.asList(ArgumentHelper.buildArgs(raw_args));
        context.put("args", new ListTag(args));
        context.put("raw_args", new ElementTag(raw_args));
        context.put("alias", new ElementTag(alias));
        dPlayer player = null;
        dNPC npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (dEntity.isCitizensNPC(pl)) {
                npc = dNPC.fromEntity(pl);
            }
            else {
                player = dPlayer.mirrorBukkitPlayer(pl);
            }
            context.put("server", new ElementTag(false));
        }
        else {
            context.put("server", new ElementTag(true));
        }
        if (Depends.citizens != null && npc == null) {
            NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(commandSender);
            if (citizen != null) {
                npc = dNPC.mirrorCitizensNPC(citizen);
            }
        }
        return script.runTabCompleteProcedure(player, npc, context);
    }
}
