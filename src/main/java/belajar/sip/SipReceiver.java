/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package belajar.sip;

import java.util.Properties;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

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
            System.out.println("Terima request "+re);
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
