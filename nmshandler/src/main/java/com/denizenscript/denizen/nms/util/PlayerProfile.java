package com.denizenscript.denizen.nms.util;

import java.util.UUID;

public class PlayerProfile {

    private String name;
    private UUID uuid;
    private String texture;
    private String textureSignature;

    public PlayerProfile(String name, UUID uuid) {
        this(name, uuid, null, null);
    }

    public PlayerProfile(String name, UUID uuid, String texture) {
        this(name, uuid, texture, null);
    }

    public PlayerProfile(String name, UUID uuid, String texture, String textureSignature) {
        this.name = name;
        this.uuid = uuid;
        this.texture = texture;
        this.textureSignature = textureSignature;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public void setUniqueId(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTexture() {
        return texture;
    }

    public void setTexture(String texture) {
        this.texture = texture;
    }

    public boolean hasTexture() {
        return texture != null;
    }

    public String getTextureSignature() {
        return textureSignature;
    }

    public void setTextureSignature(String textureSignature) {
        this.textureSignature = textureSignature;
    }

    public boolean hasTextureSignature() {
        return textureSignature != null;
    }
}
