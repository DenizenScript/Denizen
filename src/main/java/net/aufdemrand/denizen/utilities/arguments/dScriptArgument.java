package net.aufdemrand.denizen.utilities.arguments;

/**
 * Created with IntelliJ IDEA.
 * User: Press
 * Date: 1/26/13
 * Time: 10:32 PM
 * To change this template use File | Settings | File Templates.
 */
public interface dScriptArgument {

    /**
     * <p>Retrieves the default dScript argument prefix.</p>
     *
     * Example: <br/>
     * <tt>
     * location: <br/>
     * duration: <br/>
     * item: <br/>
     * </tt>
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

    public String dScriptArg();

    /**
     * Gets the dScript argument's value. This is a valid dScript representation
     * of the argument minus the indicated prefix.
     *
     * @return  dScript argument value
     */
    public String dScriptArgValue();

    /**
     * <p>Gets an ugly, but exact, string representation of this dScriptArgument.
     * While not specified in the dScriptArgument Interface, this value should be
     * able to be used with a static valueOf(String) method to reconstruct the object.</p>
     *
     * @return  a single-line string representation of this argument
     */
    public String toString();

    /**
     * Sets the prefix for this argument, otherwise uses the default.
     *
     * @return  the dScriptArgument
     */
    public dScriptArgument setPrefix();
}
