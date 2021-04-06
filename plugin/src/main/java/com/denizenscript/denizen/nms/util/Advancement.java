package com.denizenscript.denizen.nms.util;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

public class Advancement {

    public enum Frame {TASK, CHALLENGE, GOAL}

    public boolean temporary;
    public NamespacedKey key;
    public NamespacedKey parent;
    public ItemStack icon;
    public String title;
    public String description;
    public NamespacedKey background;
    public Frame frame;
    public boolean toast;
    public boolean announceToChat;
    public boolean hidden;

    public float xOffset;
    public float yOffset;

    public int length;

    public boolean registered;

    public Advancement(boolean temporary, NamespacedKey key, NamespacedKey parent, ItemStack icon,
                       String title, String description, NamespacedKey background, Frame frame,
                       boolean toast, boolean announceToChat, boolean hidden, float xOffset, float yOffset, int length) {
        this.temporary = temporary;
        this.key = key;
        this.parent = parent;
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.background = background;
        this.frame = frame;
        this.toast = toast;
        this.announceToChat = announceToChat;
        this.hidden = hidden;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.length = length;
    }
}
