/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package belajar.sip;

import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sdp.Media;
import javax.sdp.MediaDescription;
import javax.sdp.SdpFactory;
import javax.sdp.SessionDescription;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.ServerTransaction;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

/**
 *
 * @author endy
 */
public class SipReceiver {

    private SipStack sipStack;
    private SipFactory sipFactory;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private ListeningPoint listeningPointUdp;
    private SipProvider sipProvider;
    
    private String ipLocal = "127.0.0.1";
    private Integer portLocal = 5060;
    private String protocol = "udp";
    
    public SipReceiver() throws Exception {
        System.out.println("Inisialisasi SIP Factory");
        sipFactory = SipFactory.getInstance();
        
        System.out.println("Gunakan implementasi RI");
        sipFactory.setPathName("gov.nist");
        
        System.out.println("Konfigurasi");
        Properties prop = new Properties();
        
        // yang mandatory cuma satu, yaitu STACK_NAME
        prop.setProperty("javax.sip.STACK_NAME", "penerima");
        
        // konfigurasi optional, khusus untuk implementasi gov.nist
        prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        prop.setProperty("gov.nist.javax.sip.DEBUG_LOG", "sipreceiverdebug.txt");
        prop.setProperty("gov.nist.javax.sip.SERVER_LOG", "sipreceiverserver.txt");
        
        System.out.println("Inisialisasi SIP Stack");
        sipStack = sipFactory.createSipStack(prop);
        System.out.println("SIP Stack siap : "+sipStack);
        
        listeningPointUdp = sipStack.createListeningPoint(ipLocal, portLocal, protocol);
        System.out.println("Inisialisasi SIP Provider");
        sipProvider = sipStack.createSipProvider(listeningPointUdp);
        System.out.println("SIP Provider siap : "+sipProvider);
        
        System.out.println("Inisialisasi factory lainnya");
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();
        
        System.out.println("Inisialisasi event handler");
        sipProvider.addSipListener(new MySipListener());
        System.out.println("Siap menerima message");
    }
    
    public static void main(String[] args) throws Exception {
        new SipReceiver();
        System.in.read();
    }
    
    class MySipListener implements SipListener{

        @Override
        public void processRequest(RequestEvent re) {
            try {
                System.out.print("Terima request ");
                Request r = re.getRequest();
                System.out.println(r.getMethod());
                System.out.println(r);
                
                
                
                if(Request.REGISTER.equals(r.getMethod())) {
                    ServerTransaction serverTransaction 
                        = sipProvider.getNewServerTransaction(r);
                    Response resp = messageFactory.createResponse(200, r);
                    kirimResponse(resp, serverTransaction);
                } else if(Request.INVITE.equals(r.getMethod())){
                    ServerTransaction serverTransaction 
                        = sipProvider.getNewServerTransaction(r);
                    Response ringingResponse = messageFactory.createResponse(180, r);
                    kirimResponse(ringingResponse, serverTransaction);
                    
                    Response diangkat = handleIncomingCall(serverTransaction);
                    kirimResponse(diangkat, serverTransaction);
                } else {
                    System.out.println("Jenis message ini belum dihandle");
                }
                
            } catch (Exception ex) {
                Logger.getLogger(SipReceiver.class.getName()).log(Level.SEVERE, null, ex);
            } 
        }

        private void kirimResponse(Response resp, ServerTransaction serverTransaction) throws SipException, InvalidArgumentException {
            System.out.println("Response : ");
            System.out.println(resp);
            System.out.println("Mengirim response");
            serverTransaction.sendResponse(resp);
            System.out.println("Response terkirim");
        }
        
        private Response handleIncomingCall(ServerTransaction serverTransaction) 
                throws Exception{
            Request r = serverTransaction.getRequest();
            Response diangkat = messageFactory.createResponse(200, r);
            
            Address addrPenerima = addressFactory
                    .createAddress("sip:"+ipLocal+":"+portLocal);
            
            diangkat.addHeader(headerFactory.createContactHeader(addrPenerima));
            
            // harus nge-set tag supaya bisa start dialog
            ToHeader toHeader = (ToHeader) diangkat.getHeader(ToHeader.NAME);
            toHeader.setTag("123456");
            
            // proses sdp, baca sdp offer
            SdpFactory sf = SdpFactory.getInstance();
            SessionDescription sdpData 
                    = sf.createSessionDescription(new String(r.getRawContent()));
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
            
            // create sdp answer
            
            
            // delay dulu 5 detik, pura2nya klik button
            Thread.sleep(5 * 1000);
            
            return diangkat;
        }

        @Override
        public void processResponse(ResponseEvent re) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void processTimeout(TimeoutEvent te) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void processIOException(IOExceptionEvent ioee) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void processTransactionTerminated(TransactionTerminatedEvent tte) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void processDialogTerminated(DialogTerminatedEvent dte) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    
}
