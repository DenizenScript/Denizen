package net.aufdemrand.denizen.objects;

public class Mechanism {

    boolean fulfilled;
    String raw_mechanism;
    String outcome = null;

    public Mechanism(String string) {
        fulfilled = false;
        raw_mechanism = string;
    }

    public void fulfill(String _outcome) {
        fulfilled = true;
        outcome = _outcome;
    }

    public boolean matches(String string) {
        return (string.equalsIgnoreCase(raw_mechanism));
    }

    public boolean fulfilled() {
        return fulfilled;
    }

}
