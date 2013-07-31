package belajar.sip.tls;

import belajar.sip.SipReceiver;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;

public class SipsServer {
    
    private SipStack sipStack;
    private SipFactory sipFactory;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private ListeningPoint listeningPointTls;
    private SipProvider sipProvider;
    
    private String ipLocal = "127.0.0.1";
    private Integer portLocal = 5061;
    private String protocol = "tls";
    
    public SipsServer() throws Exception {
        System.out.println("Inisialisasi SIP Factory");
        sipFactory = SipFactory.getInstance();
        
        System.out.println("Gunakan implementasi RI");
        sipFactory.setPathName("gov.nist");
        
        System.out.println("Konfigurasi");
        Properties prop = new Properties();
        
        // yang mandatory cuma satu, yaitu STACK_NAME
        prop.setProperty("javax.sip.STACK_NAME", "penerima-tls");
        
        // konfigurasi optional, khusus untuk implementasi gov.nist
        prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        prop.setProperty("gov.nist.javax.sip.DEBUG_LOG", "logs/sipsreceiverdebug.txt");
        prop.setProperty("gov.nist.javax.sip.SERVER_LOG", "logs/sipsreceiverserver.txt");
        
        // konfigurasi SSL
        System.setProperty( "javax.net.ssl.keyStore",  SipsSender.class.getResource("/server/server.jks").getPath() );
        System.setProperty( "javax.net.ssl.trustStore", SipsSender.class.getResource("/server/server.jks").getPath() );
        System.setProperty( "javax.net.ssl.keyStorePassword", "test1234" );
        System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
        prop.setProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS", "SSLv2Hello, TLSv1");
        
        System.out.println("Inisialisasi SIP Stack");
        sipStack = sipFactory.createSipStack(prop);
        System.out.println("SIP Stack siap : "+sipStack);
        
        listeningPointTls = sipStack.createListeningPoint(ipLocal, portLocal, protocol);
        System.out.println("Inisialisasi SIP Provider");
        sipProvider = sipStack.createSipProvider(listeningPointTls);
        System.out.println("SIP Provider siap : "+sipProvider);
        
        System.out.println("Inisialisasi factory lainnya");
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();
        
        System.out.println("Inisialisasi event handler");
        sipProvider.addSipListener(new SipsServer.MySipsListener());
        System.out.println("Siap menerima message");
    }
    
    private class MySipsListener implements SipListener {

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
                } else {
                    System.out.println("Maaf, baru bisa handle REGISTER. "+r.getMethod()+" belum disupport ;)");
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
    
    public static void main(String[] args) throws Exception{
        new SipsServer();
        System.out.println("Tekan tombol manapun untuk stop");
        System.in.read();
        System.exit(0);
    }
}
