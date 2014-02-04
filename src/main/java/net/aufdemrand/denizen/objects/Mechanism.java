package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.utilities.debugging.dB;

public class Mechanism {

    private boolean fulfilled;
    private String raw_mechanism;
    private Element value;
    private String outcome = null;

    public Mechanism(Element mechanism, Element value) {
        fulfilled = false;
        raw_mechanism = mechanism.asString();
        this.value = value;
    }

    public void fulfill(String _outcome) {
        fulfilled = true;
        outcome = _outcome; // TODO: Return outcome somewhere?
    }

    public boolean fulfilled() {
        return fulfilled;
    }

    public String getName() {
        return raw_mechanism;
    }

    public Element getValue() {
        return value;
    }

    public boolean hasValue() {
        return value != null && value.asString().length() > 0;
    }

    public boolean matches(String string) {
        if (string.equalsIgnoreCase(raw_mechanism)) {
            fulfill("");
            return true;
        }
        return false;
    }

    public boolean requireBoolean() {
        return requireBoolean("Invalid boolean. Must specify TRUE or FALSE.");
    }

    public boolean requireDouble() {
        return requireDouble("Invalid double specified.");
    }

    public boolean requireEnum(boolean allowInt, Enum<?>... values) {
        // TODO: Remove getSimpleName(), or simplify somehow.
        return requireEnum("Invalid " + values[0].getDeclaringClass().getSimpleName() + "."
                + " Must specify a valid name" + (allowInt ? " or number" : "") + ".", allowInt, values);
    }

    public boolean requireFloat() {
        return requireFloat("Invalid float specified.");
    }

    public boolean requireInteger() {
        return requireInteger("Invalid integer specified.");
    }

    public <T extends dObject> boolean requireObject(Class<T> type) {
        // TODO: Remove getSimpleName(), or simplify somehow.
        return requireObject("Invalid " + type.getSimpleName() + " specified.", type);
    }

    public boolean requireBoolean(String error) {
        if (value.isBoolean())
            return true;
        dB.echoError(error);
        return false;
    }

    public boolean requireDouble(String error) {
        if (value.isDouble())
            return true;
        dB.echoError(error);
        return false;
    }

    public boolean requireEnum(String error, boolean allowInt, Enum<?>... values) {
        if (allowInt && value.isInt() && value.asInt() < values.length)
            return true;
        if (value.isString()) {
            String raw_value = value.asString().toUpperCase();
            for (Enum<?> check_value : values) {
                if (raw_value.equals(check_value.name()))
                    return true;
            }
        }
        dB.echoError(error);
        return false;
    }

    public boolean requireFloat(String error) {
        if (value.isFloat())
            return true;
        dB.echoError(error);
        return false;
    }

    public boolean requireInteger(String error) {
        if (value.isInt())
            return true;
        dB.echoError(error);
        return false;
    }

    public <T extends dObject> boolean requireObject(String error, Class<T> type) {
        if (value.matchesType(type))
            return true;
        dB.echoError(error);
        return false;
    }

    public void reportInvalid() {
        dB.echoError("Invalid mechanism specified: " + raw_mechanism);
    }

}
