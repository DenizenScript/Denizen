package com.denizenscript.denizen.nms.v1_16.impl;

import com.denizenscript.denizen.nms.v1_16.Handler;
import com.denizenscript.denizen.nms.v1_16.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.abstracts.Sidebar;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Utilities;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SidebarImpl extends Sidebar {

    public static final Scoreboard dummyScoreboard = new Scoreboard();
    public static final IScoreboardCriteria dummyCriteria = new IScoreboardCriteria("dummy"); // what

    private ScoreboardObjective obj1;
    private ScoreboardObjective obj2;

    public SidebarImpl(Player player) {
        super(player);
        IChatBaseComponent chatComponentTitle = Handler.componentToNMS(FormattedTextHelper.parse(title, ChatColor.WHITE));
        this.obj1 = new ScoreboardObjective(dummyScoreboard, "dummy_1", dummyCriteria, chatComponentTitle, IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
        this.obj2 = new ScoreboardObjective(dummyScoreboard, "dummy_2", dummyCriteria, chatComponentTitle, IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
    }

    @Override
    protected void setDisplayName(String title) {
        if (this.obj1 != null) {
            IChatBaseComponent chatComponentTitle = Handler.componentToNMS(FormattedTextHelper.parse(title, ChatColor.WHITE));
            this.obj1.setDisplayName(chatComponentTitle);
            this.obj2.setDisplayName(chatComponentTitle);
        }
    }

    public List<ScoreboardTeam> generatedTeams = new ArrayList<>();

    @Override
    public void sendUpdate() {
        List<ScoreboardTeam> oldTeams = generatedTeams;
        generatedTeams = new ArrayList<>();
        PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj1, 0));
        for (int i = 0; i < this.lines.length; i++) {
            String line = this.lines[i];
            if (line == null) {
                break;
            }
            String lineId = Utilities.generateRandomColors(8);
            ScoreboardTeam team = new ScoreboardTeam(dummyScoreboard, lineId);
            team.getPlayerNameSet().add(lineId);
            team.setPrefix(Handler.componentToNMS(FormattedTextHelper.parse(line, ChatColor.WHITE)));
            generatedTeams.add(team);
            PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardTeam(team, 0));
            PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, obj1.getName(), lineId, this.scores[i]));
        }
        PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardDisplayObjective(1, this.obj1));
        PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
        ScoreboardObjective temp = this.obj2;
        this.obj2 = this.obj1;
        this.obj1 = temp;
        for (ScoreboardTeam team : oldTeams) {
            PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardTeam(team, 1));
        }
    }

    @Override
    public void remove() {
        for (ScoreboardTeam team : generatedTeams) {
            PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardTeam(team, 1));
        }
        generatedTeams.clear();
        PacketHelperImpl.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
    }
}
