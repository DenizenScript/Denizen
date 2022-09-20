package com.denizenscript.denizen.utilities.command.scripted;

import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.containers.core.CommandScriptContainer;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizencore.utilities.SimpleDefinitionProvider;
import com.denizenscript.denizencore.utilities.debugging.Debug;
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
        PlayerTag player = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (!EntityTag.isNPC(pl)) {
                player = PlayerTag.mirrorBukkitPlayer(pl);
            }
            context.put("server", new ElementTag(false));
        }
        else {
            context.put("server", new ElementTag(true));
        }
        Debug.push3ErrorContexts(script, "while reading Allowed Help procedure", player);
        try {
            return script.runAllowedHelpProcedure(player, null, context);
        }
        finally {
            Debug.popErrorContext(3);
        }
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
            PlayerTag player = null;
            NPCTag npc = null;
            if (target instanceof Player) {
                Player pl = (Player) target;
                if (EntityTag.isCitizensNPC(pl)) {
                    npc = NPCTag.fromEntity(pl);
                }
                else {
                    player = PlayerTag.mirrorBukkitPlayer(pl);
                }
            }
            if (Depends.citizens != null && npc == null) {
                NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(target);
                if (citizen != null) {
                    npc = new NPCTag(citizen);
                }
            }
            if (permissionMessage.contains("<permission>")) {
                BukkitImplDeprecations.pseudoTagBases.warn(script);
                permissionMessage = permissionMessage.replace("<permission>", getPermission());
            }
            BukkitTagContext context = new BukkitTagContext(player, npc, null, false, new ScriptTag(script));
            context.definitionProvider = new SimpleDefinitionProvider();
            context.definitionProvider.addDefinition("permission", new ElementTag(getPermission()));
            Debug.push3ErrorContexts(script, "while reading Permission Message", player);
            try {
                for (String line : TagManager.tag(permissionMessage, context).split("\n")) {
                    target.sendMessage(line);
                }
            }
            finally {
                Debug.popErrorContext(3);
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
        List<String> args = Arrays.asList(ArgumentHelper.buildArgs(raw_args, false));
        context.put("args", new ListTag(args, true));
        context.put("raw_args", new ElementTag(raw_args, true));
        context.put("alias", new ElementTag(commandLabel, true));
        PlayerTag player = null;
        NPCTag npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (EntityTag.isCitizensNPC(pl)) {
                npc = NPCTag.fromEntity(pl);
            }
            else {
                player = PlayerTag.mirrorBukkitPlayer(pl);
            }
            context.put("server", new ElementTag(false));
            context.put("source_type", new ElementTag("player"));
        }
        else {
            if (commandSender instanceof BlockCommandSender) {
                context.put("command_block_location", new LocationTag(((BlockCommandSender) commandSender).getBlock().getLocation()));
                context.put("server", new ElementTag(false));
                context.put("source_type", new ElementTag("command_block"));
            }
            else if (commandSender instanceof CommandMinecart) {
                context.put("command_minecart", new EntityTag((CommandMinecart) commandSender));
                context.put("server", new ElementTag(false));
                context.put("source_type", new ElementTag("command_minecart"));
            }
            else {
                context.put("server", new ElementTag(true));
                context.put("source_type", new ElementTag("server"));
            }
        }
        if (Depends.citizens != null && npc == null) {
            NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(commandSender);
            if (citizen != null) {
                npc = new NPCTag(citizen);
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
        List<String> args = Arrays.asList(ArgumentHelper.buildArgs(raw_args, false));
        context.put("args", new ListTag(args, true));
        context.put("raw_args", new ElementTag(raw_args, true));
        context.put("alias", new ElementTag(alias, true));
        PlayerTag player = null;
        NPCTag npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (EntityTag.isCitizensNPC(pl)) {
                npc = NPCTag.fromEntity(pl);
            }
            else {
                player = PlayerTag.mirrorBukkitPlayer(pl);
            }
            context.put("server", new ElementTag(false));
        }
        else {
            context.put("server", new ElementTag(true));
        }
        if (Depends.citizens != null && npc == null) {
            NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(commandSender);
            if (citizen != null) {
                npc = new NPCTag(citizen);
            }
        }
        Debug.push3ErrorContexts(script, "while reading tab completions", player);
        try {
            return script.runTabCompleteProcedure(player, npc, context, arguments);
        }
        finally {
            Debug.popErrorContext(3);
        }
    }
}
