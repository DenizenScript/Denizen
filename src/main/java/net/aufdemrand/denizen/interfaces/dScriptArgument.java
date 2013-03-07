package net.aufdemrand.denizen.interfaces;

import net.aufdemrand.denizen.tags.Attribute;

public interface dScriptArgument {

    /**
     * Retrieves the default dScript argument prefix.
     *
     * @return
     */
    public String getDefaultPrefix();

    /**
     * <p>Gets a standard dB representation of this argument.</p>
     *
     * Example: <br/>
     * <tt>
     * Location='x,y,z,world'
     * Location='unique_location(x,y,z,world)'
     * </tt>
     *
     *
     * @return
     */
    public String debug();

    /**
     * Gets the dScript argument, prefix and value. This is a valid dScript representation
     * of the argument.
     *
     * @return
     */
    public String as_dScriptArg();

    /**
     * Gets an ugly, but exact, string representation of this dScriptArgument.
     * While not specified in the dScriptArgument Interface, this value should be
     * able to be used with a static valueOf(String) method to reconstruct the object.
     *
     * @return  a single-line string representation of this argument
     */
    @Override
    public String toString();

    /**
     * Sets the prefix for this argument, otherwise uses the default.
     *
     * @return  the dScriptArgument
     */
    public dScriptArgument setPrefix(String prefix);

    /**
     * Gets a string-friendly specific attribute of this object.
     *
     * @param attribute  the name of the attribute
     * @return  a string attribute
     */
    public String getAttribute(Attribute attribute);

}
