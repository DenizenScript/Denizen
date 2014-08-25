package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.CommandScriptContainer;
import net.aufdemrand.denizen.utilities.depends.Depends;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DenizenCommand extends Command {

    private CommandScriptContainer script;

    public DenizenCommand(CommandScriptContainer script) {
        super(script.getCommandName(), script.getDescription(), script.getUsage(), script.getAliases());
        this.script = script;
    }

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] strings) {
        dPlayer player = null;
        dNPC npc = null;
        if (commandSender instanceof Player) {
            Player pl = (Player) commandSender;
            if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC(pl))
                npc = dNPC.fromEntity(pl);
            else
                player = dPlayer.mirrorBukkitPlayer(pl);
        }
        if (Depends.citizens != null && npc == null) {
            NPC citizen = CitizensAPI.getDefaultNPCSelector().getSelected(commandSender);
            if (citizen != null)
                npc = dNPC.mirrorCitizensNPC(citizen);
        }
        script.runCommandScript(player, npc);
        return true;
    }

    @Override
    public boolean isRegistered() {
        return true;
    }

}
