/**
 * Copyright (c) 2009 OpenCloud
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the 
 *      documentation and/or other materials provided with the distribution.
 *   3. The name of the author may not be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *   4. The source code may not be used to create, develop, use or distribute 
 *      software for use on any platform other than the OpenCloud Rhino 
 *      platform or any successor products.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF 
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Visit OpenCloud Developer's Portal for how-to guides, examples, 
 * documentation, forums and more: http://developer.opencloud.com
 */
package de.EnOcean.jslee.ra;

import java.io.DataInput;
import java.io.IOException;
import java.io.DataOutput;

import javax.slee.resource.ActivityHandle;

/**
 * Unique ID for a connection, used as an activity handle by the SLEE. The activity handle must implement correct
 * {@link #hashCode()} and {@link #equals(java.lang.Object)} methods, because the SLEE will use these internally when
 * managing activities.
 */
class ConnectionID
    implements ActivityHandle
{
    /**
     * Cosntruct ID from the Rhino node ID and sequence number
     * @param nodeID
     * @param seq
     */
    ConnectionID( byte type, int nodeID, int seq )
    {
        this.type = type;
        this.typeString = typeToString( type );
        this.nodeID = nodeID;
        this.seq = seq;
    }

    /**
     * Construct ID from an input stream, for unmarshalling.
     * 
     * @param in
     * @throws IOException
     */
    ConnectionID( DataInput in )
        throws IOException
    {
        this.type = in.readByte();
        this.typeString = typeToString( type );
        this.nodeID = in.readInt();
        this.seq = in.readInt();
    }

    /**
     * Write ID to an output stream, for marshalling.
     * 
     * @param out
     * @throws IOException
     */
    void toStream( DataOutput out )
        throws IOException
    {
        out.writeByte( type );
        out.writeInt( nodeID );
        out.writeInt( seq );
 
    }

    /**
     * Used by {@link TcpMarshaler}.
     * 
     * @return the estimated size of a ConnectionID when marshalled.
     */
    static int getEstimatedHandleSize()
    {
        return 11;
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode()
    {
        return type ^ nodeID ^ seq;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals( Object o )
    {
        if ( o == null )
            return false;
        if ( o == this )
            return true;
        if ( o instanceof ConnectionID )
        {
            ConnectionID id = (ConnectionID) o;
            return ( type == id.type )
                && ( nodeID == id.nodeID )
                && ( seq == id.seq );
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        StringBuilder sb = new StringBuilder(32);
        sb.append( "ConnectionID[" )
            .append( typeString ).append( ':' )
            .append( nodeID ).append( ':' )
            .append( seq ).append( ']' );
        return sb.toString();
    }
    
    private static String typeToString(byte type)
    {
        switch ( type )
        {
            case ENOCEAN_AC:
                return "enoceanAC";
            case GATEWAY_AC:
                return "gatewayAC";
            default:
                return "unknown";
        }
    }
    
    /**
     * Connection types
     */
    public static final byte GATEWAY_AC = 0;
    public static final byte ENOCEAN_AC = 1;
    
    private final String typeString;
    
    private final byte type;

    private final int nodeID;

    private final int seq;
    
    
}
