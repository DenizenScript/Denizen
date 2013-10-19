package net.aufdemrand.denizen.objects;

public class Mechanism {

    boolean fulfilled;
    String raw_mechanism;
    String outcome = null;

    public Mechanism(String string) {
        fulfilled = false;
        raw_mechanism = string;
    }

    public void fulfill(String outcome) {
        fulfilled = true;
        outcome = outcome;
    }

    public boolean matches(String string) {
        return (string.equalsIgnoreCase(raw_mechanism));
    }

    public boolean fulfilled() {
        return fulfilled;
    }

}