/*
 * DaapAudioResponseImpl.java
 *
 * Created on April 5, 2004, 9:47 PM
 */

package de.kapsi.net.daap.classic;

import java.io.IOException;
import java.io.FileInputStream;
import java.io.OutputStream;

import de.kapsi.net.daap.DaapUtil;
import de.kapsi.net.daap.Song;
import de.kapsi.net.daap.DaapAudioResponse;
import de.kapsi.net.daap.DaapConnection;

/**
 *
 * @author  roger
 */
public class DaapAudioResponseImpl extends DaapAudioResponse {
    
    private boolean headerWritten = false;
    private boolean audioWritten = false;
    
    private OutputStream out;
    
    /** Creates a new instance of DaapAudioResponse */
    public DaapAudioResponseImpl(DaapConnection connection, Song song, FileInputStream in, int pos, int end) throws IOException {
        super(connection, song, in, pos, end);
        
        out = ((DaapConnectionImpl)connection).getOutputStream();
    }
    
    public boolean hasRemainig() {
        return !(headerWritten && audioWritten);
    }
    
    public boolean write() throws IOException {
             
        if (!headerWritten) {
            
            try {
                
                out.write(header, 0, header.length);
                out.flush();
                headerWritten = true;
                
            } catch (IOException err) {
                in.close();
                throw err;
            }
        }
        
        return stream();
    }
    
    private boolean stream() throws IOException {
        
        try {
            
            // DO NOT SET THIS TOO HIGH AS IT CAUSES RE-BUFFERING
            // AT THE BEGINNING OF HIGH BIT RATE SONGS (WAV AND AIFF) !!!
            byte[] buffer = new byte[512];
            
            int total = 0;
            int len = -1;
            
            if (pos != 0) {
                in.skip(pos);
            }
            
            while((len = in.read(buffer, 0, buffer.length)) != -1 && total < end) {
                out.write(buffer, 0, len);
                
                // DO NOT FLUSH AS IT CAUSES RE-BUFFERING AT THE
                // BEGINNING OF HIGH BIT RATE SONGS (WAV AND AIFF) !!!
                
                total += len;
            }
            
            out.flush();
            in.close();
           
        } finally {
            in.close();
        }
        
        return true;
    }
}