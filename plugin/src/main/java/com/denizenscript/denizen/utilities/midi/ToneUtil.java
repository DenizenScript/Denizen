package com.denizenscript.denizen.utilities.midi;

import javax.sound.midi.ShortMessage;

/**
 * Utility for converting between different representations of a tone.
 * Calculations provided by github.com/sk89q/craftbook
 *
 * @author authorblues
 */

public class ToneUtil {

    public static double noteToPitch(byte note) {

        return (float) Math.pow(2.0D, (note - 12) / 12.0D);
    }

    // converts midi events into Note objects
    public static byte midiToNote(ShortMessage smsg) {

        int semitone = smsg.getData1();

        if (semitone < 54) {
            return (byte) ((semitone - 6) % (18 - 6));
        }
        else if (semitone > 78) {
            return (byte) ((semitone - 6) % (18 - 6) + 12);
        }
        else {
            return (byte) (semitone - 54);
        }
    }

    // converts midi events into pitch
    public static double midiToPitch(ShortMessage smsg) {

        return noteToPitch(midiToNote(smsg));
    }
}
