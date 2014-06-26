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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.slee.facilities.Tracer;

/**
 * Handles a TCP connection and implements the activity interface. Lines of text read from the connection are passed up
 * to the SLEE as events, and SBBs can send messages on the connection using {@link #sendMessage(java.lang.String)}.
 */
abstract class ConnectionHandler
    extends Thread
{    
    protected ConnectionHandler( Tracer tracer, TcpResourceAdaptor ra, ConnectionID id, Socket socket )
        throws IOException
    {
        super( "ConnectionHandler thread for " + id );
        this.tracer = tracer;
        this.ra = ra;
        this.id = id;
        this.socket = socket;
        this.reader = new BufferedReader( new InputStreamReader( socket.getInputStream() ) );
        this.writer = new PrintWriter( socket.getOutputStream() );
        tracer.info( "New connection with " + socket.getRemoteSocketAddress() );

        // Inform the RA of this new connection
        ra.connectionOpened( this );
    }

    // Called from SBB
    public void sendMessage( String message )
        throws IOException
    {
        tracer.fine( "Sending message: " + message );
        writer.println( message );
        writer.flush();
        
    }

    // Called from SBB
    public void close()
    {
        shutdown();
    }

    // Called from SBB
    public boolean isOpen()
    {
        return socket.isConnected();
       
    }

    /**
     * {@inheritDoc}
     */
    public void run()
    {
        while ( running )
        {
        	
        	
    		Socket echoSocket = null;
			try {
				echoSocket = new Socket("192.168.0.123", 2003);
				
				BufferedReader in = new BufferedReader(
			    new InputStreamReader(echoSocket.getInputStream()));
				//in.read();
			
				tracer.fine(in.read()+ ">>>>>>>>>>>>>>>>>>>>>>read out message>>>>>>>>>>>>>>>>>>>>");
				
				break;
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				tracer.info(">>>>>>>>>>>>>>>>error while openning port 2003>>>>>>>>>>" );
			}
		
            try
            {
                String message = reader.readLine();
                if ( message == null )
                {
                    tracer.info( "connection closed by remote host" );
                    break;
                }
                tracer.fine( "received message: " + message );
                fireEvent(message);
            }
            catch ( Exception e )
            {
                tracer.info( "connection closed by remote host" );
                break;
            }
        }
        shutdown();
    }

    public ConnectionID getConnectionID()
    {
        return id;
    }

    private synchronized void shutdown()
    {
        if ( running )
        {
            running = false;
            tracer.fine( "shutting down" );
            try
            {
                if ( !socket.isClosed() ) {
                    socket.close();
                }
                ra.connectionClosed( id );
            }
            catch ( Exception e )
            {
                tracer.warning( "error while shutting down", e );
            }
        }
    }
    
    abstract void fireEvent (String message);

    private volatile boolean running = true;

    private final TcpResourceAdaptor ra;

    private final ConnectionID id;

    private final Socket socket;

    private final BufferedReader reader;

    private final PrintWriter writer;

    private final Tracer tracer;
}
