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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.slee.facilities.Tracer;

/**
 * Connector that creates new {@link ClientConnectionHandler} objects when a new
 * client connection is started
 */
class Connector
{
    Connector( Tracer tracer, TcpResourceAdaptor ra, int myNodeID )
    {
        this.tracer = tracer;
        this.ra = ra;
        this.myNodeID = myNodeID;
    }

    ConnectionID connectClient( InetSocketAddress client )
        throws IOException
    {
        tracer.info( "connecting to client " + client );
        Socket sock = new Socket( client.getHostName(), client.getPort() );
        // Create a unique ID for the connection - this is also the activity handle
        //ConnectionID id = new ConnectionID( ConnectionID.CLIENT, myNodeID, sequence++ );
        ConnectionID id = new ConnectionID( ConnectionID.ENOCEAN_AC, myNodeID, sequence++ );

        // Create a new connection handler thread and start it
        ConnectionHandler conn = new ClientConnectionHandler( tracer, ra, id, sock );
        conn.start();

        return id;
    }

    private final TcpResourceAdaptor ra;

    private final int myNodeID;

    private int sequence = 0;

    private final Tracer tracer;
}
