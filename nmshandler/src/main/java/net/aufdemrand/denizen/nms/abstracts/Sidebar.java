package net.aufdemrand.denizen.nms.abstracts;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public abstract class Sidebar {

    protected final Player player;
    protected String title;
    protected String[] lines;
    protected int[] scores;
    protected int start;
    protected int increment;

    public Sidebar(Player player) {
        this.player = player;
        setTitle("");
        this.lines = new String[15];
        this.scores = new int[15];
        this.start = Integer.MIN_VALUE;
        this.increment = -1;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getLines() {
        return new ArrayList<String>(Arrays.asList(lines));
    }

    public int[] getScores() {
        return scores;
    }

    public int getStart() {
        return start;
    }

    public int getIncrement() {
        return increment;
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

    public void setStart(int start) {
        this.start = start;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    public void setLines(List<String> lines) {
        lines.removeAll(Collections.singleton((String) null));
        this.lines = new String[15];
        this.scores = new int[15];
        int score = this.start;
        if (score == Integer.MIN_VALUE) {
            score = lines.size();
        }
        for (int i = 0; i < lines.size() && i < this.lines.length; i++, score += this.increment) {
            String line = lines.get(i);
            if (line.length() > 40) {
                line = line.substring(0, 40);
            }
            this.lines[i] = line;
            this.scores[i] = score;
        }
    }

    public abstract void sendUpdate();

    public abstract void remove();
}
