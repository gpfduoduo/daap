/*
 * DaapResponseFactory.java
 *
 * Created on April 5, 2004, 6:40 PM
 */

package de.kapsi.net.daap;

import java.io.IOException;
import java.io.FileInputStream;

/**
 * This Factory interface is used to create either NIO
 * or classic OutputStream based DaapResponses.
 *
 * @author  roger
 */
public interface DaapResponseFactory {
    
    /**
     *
     * @param connection
     * @return
     */    
    public DaapResponse createAuthResponse(DaapRequest request);
    
    /**
     *
     * @param connection
     * @param data
     * @return
     */    
    public DaapResponse createChunkResponse(DaapRequest request, byte[] data);
    
    /**
     *
     * @return
     * @param end
     * @param in
     * @param connection
     * @param pos
     * @throws IOException
     */    
    public DaapResponse createAudioResponse(DaapRequest request, Song song, FileInputStream in, int pos, int end) throws IOException;
}
