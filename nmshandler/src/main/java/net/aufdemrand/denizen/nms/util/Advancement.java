package net.aufdemrand.denizen.nms.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;

public class Advancement {

    public enum Frame {TASK, CHALLENGE, GOAL}

    public boolean temporary;
    public NamespacedKey key;
    public NamespacedKey parent;
    public Material iconMaterial;
    public byte iconData;
    public String title;
    public String description;
    public NamespacedKey background;
    public Frame frame;
    public boolean toast;
    public boolean announceToChat;
    public boolean hidden;

    public float xOffset;
    public float yOffset;

    public boolean registered;

    public Advancement(boolean temporary, NamespacedKey key, NamespacedKey parent, Material iconMaterial,
                       byte iconData, String title, String description, NamespacedKey background, Frame frame,
                       boolean toast, boolean announceToChat, boolean hidden, float xOffset, float yOffset) {
        this.temporary = temporary;
        this.key = key;
        this.parent = parent;
        this.iconMaterial = iconMaterial;
        this.iconData = iconData;
        this.title = title;
        this.description = description;
        this.background = background;
        this.frame = frame;
        this.toast = toast;
        this.announceToChat = announceToChat;
        this.hidden = hidden;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }
}
