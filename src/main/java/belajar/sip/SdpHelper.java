/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package belajar.sip;

import java.util.Date;
import java.util.Vector;
import javax.sdp.Connection;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.Origin;
import javax.sdp.SdpException;
import javax.sdp.SdpFactory;
import javax.sdp.SdpParseException;
import javax.sdp.SessionDescription;
import javax.sdp.SessionName;
import javax.sdp.Time;
import javax.sdp.Version;

/**
 *
 * @author endy
 */
public class SdpHelper {
    public static byte[] createSdpData(SdpFactory sf, String ipLocal, 
            Vector<MediaDescription> daftarMedia) throws Exception {
        
        // inisialisasi SDP message
            
            Version v = sf.createVersion(0);
            
            long ss = SdpFactory.getNtpTime(new Date());
            Origin origin = sf.createOrigin("-", ss, ss, "IN", "IP4", ipLocal);
            
            SessionName sessionName = sf.createSessionName("-");
            Connection conn = sf.createConnection("IN", "IP4", ipLocal);
            
            Vector<Time> vt = new Vector<Time>();
            vt.add(sf.createTime());
            
            SessionDescription sdp = sf.createSessionDescription();
            sdp.setVersion(v);
            sdp.setOrigin(origin);
            sdp.setSessionName(sessionName);
            sdp.setConnection(conn);
            sdp.setTimeDescriptions(vt);
            sdp.setMediaDescriptions(daftarMedia);
            return sdp.toString().getBytes();
    }
    
    public static void displaySdp(SdpFactory sf, byte[] content) throws SdpException, SdpParseException {
            SessionDescription sdpData 
                    = sf.createSessionDescription(new String(content));
            Vector<MediaDescription> daftarMediaYangDitawarkan 
                    = sdpData.getMediaDescriptions(false);
            
            
            System.out.println("Daftar media yang ditawarkan");
            for (MediaDescription mediaDescription : daftarMediaYangDitawarkan) {
                System.out.println("Media Info : " + mediaDescription);
                Media media = mediaDescription.getMedia();
                System.out.println("Media Type : "+media.getMediaType());
                System.out.println("Media Encoding : "+media.getMediaFormats(false));
                System.out.println("Media Port : "+media.getMediaPort());
            }
        }
}
