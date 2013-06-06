package net.aufdemrand.denizen.objects;

import net.aufdemrand.denizen.tags.Attribute;

public interface dObject {

    /**
     * Retrieves the dScript argument prefix. dObjects should provide a default
     * prefix if nothing else has been specified.
     *
     * @return
     */
    public String getPrefix();


    /**
     * <p>Gets a standard dB representation of this argument. All dObjects should follow
     * suit.</p>
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
     * Determines if this argument object is unique. This typically stipulates
     * that this object has been named, or has some unique identifier that
     * Denizen can use to recall it.
     *
     * @return  true if this object is unique, false if it is a 'singleton generic argument/object'
     */
    public boolean isUnique();


    /**
     * Returns the string type of the object. This is fairly verbose and crude, but used with
     * a basic dScriptArg attribute.
     *
     * @return  a straight-up string description of the type of dScriptArg. ie. dList, dLocation
     */
    public String getType();


    /**
     * Gets an ugly, but exact, string representation of this dObject.
     * While not specified in the dObject Interface, this value should be
     * able to be used with a static valueOf(String) method to reconstruct the object.
     *
     * @return  a single-line string representation of this argument
     */
    public String identify();


    /**
     * Sets the prefix for this argument, otherwise uses the default.
     *
     * @return  the dObject
     */
    public dObject setPrefix(String prefix);


    /**
     * Gets a specific attribute using this object to fetch the necessary data.
     *
     * @param attribute  the name of the attribute
     * @return  a string result of the fetched attribute
     */
    public String getAttribute(Attribute attribute);


}
