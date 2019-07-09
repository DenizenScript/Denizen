package net.aufdemrand.denizen.nms.impl;

import net.aufdemrand.denizen.nms.abstracts.Sidebar;
import net.aufdemrand.denizen.nms.helpers.PacketHelper_v1_14_R1;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.entity.Player;

public class Sidebar_v1_14_R1 extends Sidebar {

    private static final Scoreboard dummyScoreboard = new Scoreboard();
    private static final IScoreboardCriteria dummyCriteria = new IScoreboardCriteria("dummy"); // what

    private ScoreboardObjective obj1;
    private ScoreboardObjective obj2;

    public Sidebar_v1_14_R1(Player player) {
        super(player);
        IChatBaseComponent chatComponentTitle = new ChatComponentText(title);
        this.obj1 = new ScoreboardObjective(dummyScoreboard, "dummy_1", dummyCriteria, chatComponentTitle, IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
        this.obj2 = new ScoreboardObjective(dummyScoreboard, "dummy_2", dummyCriteria, chatComponentTitle, IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
    }

    @Override
    protected void setDisplayName(String title) {
        if (this.obj1 != null) {
            IChatBaseComponent chatComponentTitle = new ChatComponentText(title);
            this.obj1.setDisplayName(chatComponentTitle);
            this.obj2.setDisplayName(chatComponentTitle);
        }
    }

    @Override
    public void sendUpdate() {
        PacketHelper_v1_14_R1.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj1, 0));
        for (int i = 0; i < this.lines.length; i++) {
            String line = this.lines[i];
            if (line == null) {
                break;
            }
            ScoreboardScore score = new ScoreboardScore(dummyScoreboard, this.obj1, line);
            score.setScore(this.scores[i]);
            // CraftScoreboardManager setPlayerBoard
            PacketHelper_v1_14_R1.sendPacket(player, new PacketPlayOutScoreboardScore(ScoreboardServer.Action.CHANGE, score.getObjective().getName(), score.getPlayerName(), score.getScore()));
        }
        PacketHelper_v1_14_R1.sendPacket(player, new PacketPlayOutScoreboardDisplayObjective(1, this.obj1));
        PacketHelper_v1_14_R1.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
        ScoreboardObjective temp = this.obj2;
        this.obj2 = this.obj1;
        this.obj1 = temp;
    }

    @Override
    public void remove() {
        PacketHelper_v1_14_R1.sendPacket(player, new PacketPlayOutScoreboardObjective(this.obj2, 1));
    }
}
