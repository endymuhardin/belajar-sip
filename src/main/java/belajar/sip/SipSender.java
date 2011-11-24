/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package belajar.sip;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sip.ClientTransaction;
import javax.sip.Dialog;
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
import javax.sip.message.Response;

/**
 *
 * @author endy
 */
public class SipSender {
    private SipStack sipStack;
    private SipFactory sipFactory;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private MessageFactory messageFactory;
    private ListeningPoint listeningPointUdp;
    private SipProvider sipProvider;
    
    private String ipLocal = "127.0.0.1";
    private Integer portLocal = 5070;
    private String protocol = "udp";
    
    private String addressOfRecord = "sip:endy@localhost";
    private String contactAddress = "sip:endy@"+ipLocal+":"+portLocal;
    private static String registrar = "sip:endy@127.0.0.1:5060";
    
    public SipSender() throws Exception {
        System.out.println("Inisialisasi SIP Factory");
        sipFactory = SipFactory.getInstance();
        
        System.out.println("Gunakan implementasi RI");
        sipFactory.setPathName("gov.nist");
        
        System.out.println("Konfigurasi");
        Properties prop = new Properties();
        
        // yang mandatory cuma satu, yaitu STACK_NAME
        prop.setProperty("javax.sip.STACK_NAME", "pengirim");
        
        // konfigurasi optional, khusus untuk implementasi gov.nist
        prop.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        prop.setProperty("gov.nist.javax.sip.DEBUG_LOG", "sipsenderdebug.txt");
        prop.setProperty("gov.nist.javax.sip.SERVER_LOG", "sipsenderserver.txt");
        
        System.out.println("Inisialisasi SIP Stack");
        sipStack = sipFactory.createSipStack(prop);
        System.out.println("SIP Stack siap : "+sipStack);
        
        listeningPointUdp = sipStack.createListeningPoint(ipLocal, portLocal, protocol);
        System.out.println("Inisialisasi SIP Provider");
        sipProvider = sipStack.createSipProvider(listeningPointUdp);
        sipProvider.addSipListener(new MySipListener());
        System.out.println("SIP Provider siap : "+sipProvider);
        
        System.out.println("Inisialisasi factory lainnya");
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();
    }
    
    public void kirimRegister(String tujuan) throws Exception {
        
        String command = "REGISTER";
        Request req = createRequest(tujuan, command);
        kirimRequest(req);
    }
    
    public void kirimInvite(String tujuan) throws Exception {
        String command = "INVITE";
        Request req = createRequest(tujuan, command);
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
    
    public static void main(String[] args) throws Exception {
        SipSender sender = new SipSender();
        //sender.kirimRegister(registrar);
        sender.kirimInvite(registrar);
    }
    
    class MySipListener implements SipListener{

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
            
            // hanya response invite 200 yang di-ACK, ringing tidak usah
            if(Request.INVITE.equals(original.getMethod())  
                    && Response.ACCEPTED == re.getResponse().getStatusCode()) {
                try {
                    System.out.println("Terima 200 dari INVITE, harus kirim ACK");
                    Dialog dialog = clientTransaction.getDialog();
                    
                    Request ackRequest = dialog.createAck(
                            ((CSeqHeader) re.getResponse()
                            .getHeader(CSeqHeader.NAME)).getSeqNumber());
  
                    // location kita yang asli disertakan, 
                    // supaya next request/response bisa langsung
                    // tanpa lewat proxy
                    ContactHeader myContactHeader = 
                            headerFactory.createContactHeader(
                            addressFactory.createAddress(contactAddress));
                    ackRequest.addHeader(myContactHeader);
                    System.out.println("Mengirim ACK");
                    dialog.sendAck(ackRequest);
                    System.out.println("ACK terkirim");
                } catch (Exception ex) {
                    Logger.getLogger(SipSender.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
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
