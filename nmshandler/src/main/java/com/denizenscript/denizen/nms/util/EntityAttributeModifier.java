package com.denizenscript.denizen.nms.util;

import java.util.UUID;

public class EntityAttributeModifier {

    private UUID uuid;
    private String name;
    private Operation operation;
    private double amount;

    public EntityAttributeModifier(String name, Operation operation, double amount) {
        this(UUID.randomUUID(), name, operation, amount);
    }

    public EntityAttributeModifier(UUID uuid, String name, Operation operation, double amount) {
        this.uuid = uuid;
        this.name = name;
        this.operation = operation;
        this.amount = amount;
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public Operation getOperation() {
        return operation;
    }

    public double getAmount() {
        return amount;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public enum Operation {
        ADD_NUMBER,
        ADD_SCALAR,
        MULTIPLY_SCALAR_1
    }
}
