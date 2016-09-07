package net.aufdemrand.denizen.scripts.requirements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequirementsMode {


    public enum Mode {ALL, NONE, ANY_NUM, FIRST_AND_ANY_NUM}

    private static Pattern intsOnly = Pattern.compile("(\\d+)");

    protected Mode mode;
    protected int modeInt;

    public Mode getMode() {
        return mode;
    }

    /**
     * <p>Uses a dScript Requirements Mode argument to build this object.</p>
     * <p/>
     * Valid: <br/>
     * ALL <br/>
     * NONE <br/>
     * ANY # <br/>
     * FIRST AND ANY # <br/>
     *
     * @param arg the value of the Requirements.Mode yaml-key from the script
     */
    public RequirementsMode(String arg) {
        arg = arg.toUpperCase();
        if (arg.equals("NONE")) {
            mode = Mode.NONE;
        }
        else if (arg.equals("ALL")) {
            mode = Mode.ALL;
        }
        else if (arg.contains("FIRST")) {
            mode = Mode.FIRST_AND_ANY_NUM;
        }
        else if (arg.contains("ANY")) {
            mode = Mode.ANY_NUM;
        }

        Matcher findInt = intsOnly.matcher(arg);
        if (findInt.matches()) {
            modeInt = Integer.valueOf(findInt.group(1));
        }
        else {
            modeInt = 1;
        }
    }

    public RequirementsMode(Mode mode) {
        this(mode, 1);
    }

    public RequirementsMode(Mode mode, int num) {
        this.mode = mode;
        this.modeInt = num;
    }
}
