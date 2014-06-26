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
import java.nio.ByteBuffer;

import javax.slee.resource.ActivityHandle;
import javax.slee.resource.FireableEventType;
import javax.slee.resource.Marshaler;

/**
 * The marshaler knows how to efficiently marshal and unmarshal events and activity handles on behalf of the SLEE.
 */
class TcpMarshaler
    implements Marshaler
{

    /**
     * {@inheritDoc}
     */
    public int getEstimatedEventSize( FireableEventType eventType, Object event )
    {
        return MessageEventImpl.getEstimatedEventSize();
    }

    /**
     * {@inheritDoc}
     */
    public int getEstimatedHandleSize( ActivityHandle handle )
    {
        return ConnectionID.getEstimatedHandleSize();
    }

    /**
     * {@inheritDoc}
     */
    public ByteBuffer getEventBuffer( FireableEventType eventType, Object event )
    {
        // Do nothing...
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public void marshalEvent( FireableEventType eventType, Object event, DataOutput out )
        throws IOException
    {
        ( (MessageEventImpl) event ).toStream( out );
    }

    /**
     * {@inheritDoc}
     */
    public void marshalHandle( ActivityHandle handle, DataOutput out )
        throws IOException
    {
        ( (ConnectionID) handle ).toStream( out );
    }

    /**
     * {@inheritDoc}
     */
    public void releaseEventBuffer( FireableEventType eventType, Object event, ByteBuffer buffer )
    {
        // Do nothing...
    }

    /**
     * {@inheritDoc}
     */
    public Object unmarshalEvent( FireableEventType eventType, DataInput in )
        throws IOException
    {
        return new MessageEventImpl( in );
    }

    /**
     * {@inheritDoc}
     */
    public ActivityHandle unmarshalHandle( DataInput in )
        throws IOException
    {
        return new ConnectionID( in );
    }

}
