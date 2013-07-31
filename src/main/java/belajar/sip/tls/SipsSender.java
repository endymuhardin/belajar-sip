/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package belajar.sip.tls;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sip.ClientTransaction;
import javax.sip.DialogTerminatedEvent;
import javax.sip.IOExceptionEvent;
import javax.sip.InvalidArgumentException;
import javax.sip.ListeningPoint;
import javax.sip.RequestEvent;
import javax.sip.ResponseEvent;
import javax.sip.SipException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.TimeoutEvent;
import javax.sip.TransactionTerminatedEvent;
import javax.sip.TransactionUnavailableException;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.URI;
import javax.sip.header.CSeqHeader;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.FromHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.MaxForwardsHeader;
import javax.sip.header.ToHeader;
import javax.sip.header.ViaHeader;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;

/**
 *
 * @author endy
 */
public class SipsSender {
    
    private SipStack sipStack;
    private SipFactory sipFactory;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private ListeningPoint listeningPointTls;
    private SipProvider sipProvider;
    
    private String ipLocal = "127.0.0.1";
    private Integer portLocal = 5071;
    private String protocol = "tls";
    
    private String addressOfRecord = "sips:endy@localhost";
    private String contactAddress = "sips:endy@"+ipLocal+":"+portLocal;

    public SipsSender() throws Exception {
        System.out.println("Inisialisasi SIP Factory");
        sipFactory = SipFactory.getInstance();
        
        System.out.println("Gunakan implementasi RI");
        sipFactory.setPathName("gov.nist");
        
        System.out.println("Konfigurasi");
        Properties prop = new Properties();
        
        // yang mandatory cuma satu, yaitu STACK_NAME
        prop.setProperty("javax.sip.STACK_NAME", "pengirim-tls");
        
        // konfigurasi optional, khusus untuk implementasi gov.nist
        prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        prop.setProperty("gov.nist.javax.sip.DEBUG_LOG", "logs/sipssenderdebug.txt");
        prop.setProperty("gov.nist.javax.sip.SERVER_LOG", "logs/sipssenderserver.txt");
        
        // konfigurasi SSL
        System.setProperty( "javax.net.ssl.keyStore",  SipsSender.class.getResource("/client/client.jks").getPath() );
        System.setProperty( "javax.net.ssl.trustStore", SipsSender.class.getResource("/client/client.jks").getPath() );
        System.setProperty( "javax.net.ssl.keyStorePassword", "test1234" );
        System.setProperty( "javax.net.ssl.keyStoreType", "jks" );
        prop.setProperty("gov.nist.javax.sip.TLS_CLIENT_PROTOCOLS", "SSLv2Hello, TLSv1");
        
        System.out.println("Inisialisasi SIP Stack");
        sipStack = sipFactory.createSipStack(prop);
        System.out.println("SIP Stack siap : "+sipStack);
        
        listeningPointTls = sipStack.createListeningPoint(ipLocal, portLocal, protocol);
        System.out.println("Inisialisasi SIP Provider");
        sipProvider = sipStack.createSipProvider(listeningPointTls);
        sipProvider.addSipListener(new SipsSender.SipsResponseListener());
        System.out.println("SIP Provider siap : "+sipProvider);
        
        System.out.println("Inisialisasi factory lainnya");
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();
    }
    
    public void register(String registrar) throws Exception {
        Request req = createRequest(registrar, Request.REGISTER);
        kirimRequest(req);
    }
    
    private void kirimRequest(Request req) throws SipException, TransactionUnavailableException {
        System.out.println("Selesai membuat "+req.getMethod()+" request message");
        System.out.println("Isi message : ");
        System.out.println(req);
        
        System.out.println("Pakai transaction supaya bisa request-response");
        ClientTransaction clientTransaction
                = sipProvider.getNewClientTransaction(req);
        
        System.out.println("Mengirim message");
        clientTransaction.sendRequest();
        System.out.println("Message terkirim");
    }
    
    

    private Request createRequest(String tujuan, String method) throws ParseException, InvalidArgumentException {
        Address addrTujuan = addressFactory.createAddress(tujuan);
        Address addrAddressOfRecord = addressFactory.createAddress(addressOfRecord);
        Address addContactAddress = addressFactory.createAddress(contactAddress);
        URI registrarUri = addrTujuan.getURI();
        System.out.println("Membuat header");
        List<ViaHeader> viaHeaderList = new ArrayList<ViaHeader>();
        System.out.println("Message via "+ipLocal
                +" port " +portLocal
                +" dengan protokol "+protocol
                +" branch id autogenerated");
        viaHeaderList.add(headerFactory.createViaHeader(
                ipLocal, 
                portLocal, 
                protocol, 
                null));
        System.out.println("Maksimal 10 kali forward");
        MaxForwardsHeader mfh = headerFactory.createMaxForwardsHeader(10);
        System.out.println("Karena register, tujuannya diri sendiri");
        ToHeader toHeader = headerFactory.createToHeader(addContactAddress, null);
        System.out.println("Alamat pengirim, tag id dihardcode 123");
        FromHeader asal = headerFactory.createFromHeader(addrAddressOfRecord, "123");
        System.out.println("Call id autogenerated");
        CallIdHeader callId = sipProvider.getNewCallId();
        System.out.println("Command sequence dihardcode, nantinya autogenerated");
        CSeqHeader cmdSequence = headerFactory.createCSeqHeader(1L, method);
        Request req = messageFactory.createRequest(
                registrarUri, 
                method, 
                callId, 
                cmdSequence, 
                asal, 
                toHeader, 
                viaHeaderList, mfh);
        ContactHeader contactHeader = headerFactory.createContactHeader(addContactAddress);
        req.addHeader(contactHeader);
        return req;
    }
    
    private class SipsResponseListener implements SipListener{

        @Override
        public void processRequest(RequestEvent re) {
            throw new UnsupportedOperationException("Not supported yet."); 
        }

        @Override
        public void processResponse(ResponseEvent re) {
            System.out.println("Terima response");
            System.out.println(re.getResponse());
            
            ClientTransaction clientTransaction = re.getClientTransaction();
            Request original = clientTransaction.getRequest();
            System.out.println("Response dari request "+original.getMethod());
            System.out.println("Status Code "+re.getResponse().getStatusCode());
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
    
    public static void main(String[] args) throws Exception {
        SipsSender sender = new SipsSender();
        sender.register("sips:endy@127.0.0.1:5061");
    }
}
