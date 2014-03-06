// =================== DO NOT EDIT THIS FILE ====================
// Generated by Modello 1.7,
// any modifications will be overwritten.
// ==============================================================

package org.apache.maven.model;

/**
 * Interface InputLocationTracker.
 * 
 * @version $Revision$ $Date$
 */
@SuppressWarnings( "all" )
public interface InputLocationTracker
{

      //-----------/
     //- Methods -/
    //-----------/

    /**
     * Gets the location of the specified field in the input
     * source.
     * 
     * @param field The key of the field, must not be
     * <code>null</code>.
     * @return The location of the field in the input source or
     * <code>null</code> if unknown.
     */
    public InputLocation getLocation( Object field );
    /**
     * Sets the location of the specified field.
     * 
     * @param field The key of the field, must not be
     * <code>null</code>.
     * @param location The location of the field, may be
     * <code>null</code>.
     */
    public void setLocation( Object field, InputLocation location );
}
