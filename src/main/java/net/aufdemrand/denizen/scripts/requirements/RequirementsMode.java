package net.aufdemrand.denizen.scripts.requirements;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequirementsMode {

    public enum Mode { ALL, NONE, ANY_NUM, FIRST_NUM_AND_ANY_NUM }

    private final static Pattern intsOnly = Pattern.compile("(\\d+)");

    protected final Mode mode;
    protected int[] modeInt;

    public Mode getMode() {
        return mode;
    }

    /**
     * <p>Uses a dScript Requirements Mode argument to build this object.</p>
     *
     * Valid: <br/>
     * ALL <br/>
     * NONE <br/>
     * ANY # <br/>
     * FIRST (#) AND ANY # (the first number is optional)<br />
     *
     * @param arg the value of the Requirements.Mode yaml-key from the script
     */
    public RequirementsMode(String arg) {
        arg = arg.trim().toUpperCase();

        if (arg.equals("NONE")) {
            mode = Mode.NONE;

        } else if (arg.equals("ALL")) {
            mode = Mode.ALL;

        } else {
            boolean first = arg.contains("FIRST");
            boolean any = arg.contains("ANY");

            Matcher found = intsOnly.matcher(arg);

            if(!any) {
                mode = Mode.NONE;

            } else if(!first) {
                mode = Mode.ANY_NUM;

                if(found.matches()) {
                    modeInt = new int[] { Integer.valueOf(found.group(1)) };

                } else {
                    modeInt = new int[] { 1 };
                }
            } else {
                mode = Mode.FIRST_NUM_AND_ANY_NUM;

                int count = found.groupCount();
                modeInt = new int[2];

                if(count >= 2) {
                    modeInt[0] = Integer.valueOf(found.group(1));
                    modeInt[1] = Integer.valueOf(found.group(2));

                } else if(count == 1) {
                    int index = 1;

                    // Check if the "FIRST" argument has a number
                    if(arg.startsWith("FIRST AND")) {
                        modeInt[0] = 1;

                    } else {
                        modeInt[0] = Integer.valueOf(found.group(index));
                        index++;
                    }

                    // Check if we end with a number or not
                    if(arg.endsWith("ANY")) {
                        modeInt[1] = 1;
                    } else {
                        modeInt[1] = Integer.valueOf(found.group(index));
                    }
                }
            }
        }
    }

    public RequirementsMode(Mode mode) {
        this(mode, 1);
    }

    public RequirementsMode(Mode mode, int num) {
        this.mode = mode;
        this.modeInt = new int[] { num };
    }

}
