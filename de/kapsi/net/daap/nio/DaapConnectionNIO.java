/*
 * DaapConnection.java
 *
 * Created on April 2, 2004, 2:25 PM
 */

package de.kapsi.net.daap.nio;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

import de.kapsi.net.daap.Library;
import de.kapsi.net.daap.DaapRequest;
import de.kapsi.net.daap.DaapResponse;
import de.kapsi.net.daap.DaapSession;

import de.kapsi.net.daap.DaapServer;
import de.kapsi.net.daap.DaapConnection;
import de.kapsi.net.daap.DaapResponseFactory;
import de.kapsi.net.daap.DaapRequestProcessor;
import de.kapsi.net.daap.DaapResponseWriter;

/**
 *
 * @author  roger
 */
public class DaapConnectionNIO implements DaapConnection {
    
    private static final DaapResponseFactory FACTORY = new DaapResponseFactoryNIO();
    private static final DaapRequestProcessor PROCESSOR = new DaapRequestProcessor(FACTORY);
        
    private DaapServerNIO server;
    private SocketChannel channel;
    
    private DaapRequestReaderNIO reader;
    private DaapResponseWriter writer;
    private DaapSession session;
    
    private int type = DaapConnection.UNDEF;
    
    /** Creates a new instance of DaapConnection */
    public DaapConnectionNIO(DaapServerNIO server, SocketChannel channel) {
        this.server = server;
        this.channel = channel;
        
        reader = new DaapRequestReaderNIO(this);
        writer = new DaapResponseWriter();
    }
    
    /**
     *
     * @param create
     * @return
     */    
    public DaapSession getSession(boolean create) {
        
        if (session == null && create) {
            session = new DaapSession(server.createSessionId());
        }
        
        return session;
    }
    
    /**
     * Returns <tt>true</tt> if this is an Audio Stream
     *
     * @return
     */    
    public boolean isAudioStream() {
        return (type==DaapConnection.AUDIO);
    }
    
    /**
     * Returns <tt>true</tt> if this is a normal connection
     * 
     * @return
     */    
    public boolean isNormal() {
        return (type==DaapConnection.NORMAL);
    }
    
    /**
     * Returns <tt>true</tt> if this is an indetermined
     * connection
     *
     * @return
     */    
    public boolean isUndef() {
        return (type==DaapConnection.UNDEF);
    }
    
    /**
     * What do you do next?
     *
     * @return
     */    
    public int interrestOps() {
        
        if (isUndef()) {
            return SelectionKey.OP_READ;
        
        } else if (isNormal()) {
            
            int op = SelectionKey.OP_READ;
            
            if (!writer.isEmpty())
                op |= SelectionKey.OP_WRITE;
            
            return op;
        
        } else {
            // isAudioStream
            return SelectionKey.OP_WRITE;
        }
    }
    
    /**
     *
     * @return
     */    
    public DaapServer getServer() {
        return server;
    }
    
    /**
     *
     * @return
     */    
    public SocketChannel getChannel() {
        return channel;
    }
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean read() throws IOException {
        
        if (!isAudioStream()) {
            
            DaapRequest request = reader.read();
            
            if (request != null) {
                
                if (isUndef()) {
                    
                    if (request.isSongRequest()) {
                        type = DaapConnection.AUDIO;
                        
                        // AudioStreams have a session-id and we must check the id
                        Integer sid = new Integer(request.getSessionId());
                        if (server.isSessionIdValid(sid) == false) {
                            throw new IOException("Unknown Session-ID: " + sid);
                        }
                        
                        server.registerConnection(this);
                        
                    } else if (request.isServerInfoRequest()) {
                        type = DaapConnection.NORMAL;
                        server.registerConnection(this);
                    
                    } else {
                        
                        // disconnect as the first request must be
                        // either a song or server-info request!
                        throw new IOException("Illegal first request: " + request);
                    }
                }
                
                DaapResponse response = PROCESSOR.process(request);
               
                if (response != null) {
                    writer.add(response);
                }
                
                return true;
            }
            
        }
        
        return false;
    }
    
    /**
     *
     * @throws IOException
     * @return
     */    
    public boolean write() throws IOException {
        
        if (writer.write()) {
            
            if (isAudioStream()) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     *
     * @throws IOException
     */    
    public void update() throws IOException {
        
        if (isNormal()) {
            DaapSession session = getSession(false);

            if (session != null) {

                Integer sessionId = session.getSessionId();
                Integer delta = (Integer)session.getAttribute("DELTA");
                Integer revisionNumber = (Integer)session.getAttribute("REVISION-NUMBER");

                if (delta != null && revisionNumber != null) {

                    DaapRequest request =
                        new DaapRequest(this, sessionId.intValue(),
                            revisionNumber.intValue(), delta.intValue());

                    DaapResponse response = PROCESSOR.process(request);

                    if (response != null) {
                        writer.add(response);
                    }
                }
            }
        }
    }
    
    public void close() {
        
        if (session != null) {
            session.invalidate();
        }
        
        session = null;
        reader = null;
        writer = null;
    }
    
    /**
     *
     * @return
     */    
    public String toString() {
        return channel.toString();
    }
}
