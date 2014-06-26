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
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

//import com.opencloud.slee.devportal.resources.tcp.MessageEvent;
import de.EnOcean.jslee.event.*;
//import test.enoceanra.org.ratype.*;

/**
 * The implementation of the {@link MessageEvent} event.
 */
class MessageEventImpl
    implements MessageEvent, Serializable
{

    /**
     * Constructs a message from a string
     * 
     * @param message
     */
    MessageEventImpl( String message )
    {
        if ( message == null )
            throw new IllegalArgumentException();
        this.message = message;
    }

    /**
     * Construct message from an input stream, for unmarshalling.
     * 
     * @param in
     * @throws IOException
     */
    MessageEventImpl( DataInput in )
        throws IOException
    {
        message = in.readUTF();
    }

    /**
     * Write message to an output stream, for marshalling.
     * 
     * @param out
     * @throws IOException
     */
    void toStream( DataOutput out )
        throws IOException
    {
        out.writeUTF( message );
    }

    /**
     * Used by {@link TcpMarshaler}.
     * 
     * @return the estimated size of a MessageEnvent when marshalled.
     */
    static int getEstimatedEventSize()
    {
        return 8;
    }

    /**
     * {@inheritDoc}
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * {@inheritDoc}
     */
    public String toString()
    {
        return "MessageEvent[\"" + message + "\"]";
    }

    private static final long serialVersionUID = 1L;

    private final String message;
}
