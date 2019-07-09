package net.aufdemrand.denizen.nms.abstracts;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class Sidebar {

    public static class SidebarLine {

        public SidebarLine(String _text, int _score) {
            text = _text;
            score = _score;
        }

        public String text;

        public int score;
    }

    public static final int MAX_LENGTH = 15;
    protected final Player player;
    protected String title;
    protected String[] lines = new String[MAX_LENGTH];
    protected int[] scores = new int[MAX_LENGTH];
    public int setCount = 0;

    public Sidebar(Player player) {
        this.player = player;
        setTitle("");
    }

    public String getTitle() {
        return title;
    }

    public List<SidebarLine> getLines() {
        List<SidebarLine> toReturn = new ArrayList<>(MAX_LENGTH);
        for (int i = 0; i < setCount; i++) {
            toReturn.add(new SidebarLine(lines[i], scores[i]));
        }
        return toReturn;
    }

    public List<String> getLinesText() {
        return new ArrayList<>(Arrays.asList(lines));
    }

    public int[] getScores() {
        return scores;
    }

    public final void setTitle(String title) {
        if (title.length() > 32) {
            title = title.substring(0, 32);
        }
        if (this.title == null || !this.title.equals(title)) {
            this.title = title;
            setDisplayName(title);
        }
    }

    protected abstract void setDisplayName(String title);

    public void setLines(List<SidebarLine> lines) {
        setCount = Math.min(lines.size(), MAX_LENGTH);
        for (int i = 0; i < setCount; i++) {
            String line = lines.get(i).text;
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }
            this.lines[i] = line;
            this.scores[i] = lines.get(i).score;
        }
        for (int i = setCount; i < MAX_LENGTH; i++) {
            this.lines[i] = null;
            this.scores[i] = 0;
        }
    }

    public abstract void sendUpdate();

    public abstract void remove();
}
