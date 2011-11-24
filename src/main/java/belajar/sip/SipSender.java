/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package belajar.sip;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.sip.ListeningPoint;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;
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
    private String ipTujuan = "127.0.0.1";
    private Integer portTujuan = 5060;
    private String protocol = "udp";
    
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
        System.out.println("SIP Provider siap : "+sipProvider);
        
        System.out.println("Inisialisasi factory lainnya");
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();
    }
    
    public void kirimRegister() throws Exception {
        String registrar = "sip:"+ipTujuan;
        String addressOfRecord = "sip:endy@localhost";
        String contactAddress = "sip:endy@"+ipLocal;
        
        Address alamatRegistrar = addressFactory.createAddress(registrar);
        Address addrAddressOfRecord = addressFactory.createAddress(addressOfRecord);
        Address addContactAddress = addressFactory.createAddress(contactAddress);
        URI registrarUri = alamatRegistrar.getURI();
        
        System.out.println("Membuat header");
        List<ViaHeader> viaHeaderList = new ArrayList<ViaHeader>();
        
        System.out.println("Message via "+ipLocal
                +" port " +portLocal
                +" dengan protokol "+protocol
                +" branch id dihardcode abc123");
        viaHeaderList.add(headerFactory.createViaHeader(
                ipLocal, 
                portLocal, 
                protocol, 
                "abc123"));
        
        System.out.println("Maksimal 10 kali forward");
        MaxForwardsHeader mfh = headerFactory.createMaxForwardsHeader(10);
        
        System.out.println("Karena register, tujuannya diri sendiri");
        ToHeader tujuan = headerFactory.createToHeader(addContactAddress, "null");
        
        System.out.println("Alamat pengirim, tag id dihardcode 123");
        FromHeader asal = headerFactory.createFromHeader(addrAddressOfRecord, "123");
        
        System.out.println("Call id autogenerated");
        CallIdHeader callId = sipProvider.getNewCallId();
        
        System.out.println("Command sequence dihardcode, nantinya autogenerated");
        String command = "REGISTER";
        CSeqHeader cmdSequence = headerFactory.createCSeqHeader(1L, command);
        
        Request req = messageFactory.createRequest(
                registrarUri, 
                command, 
                callId, 
                cmdSequence, 
                asal, 
                tujuan, 
                viaHeaderList, mfh);
        
        ContactHeader contactHeader = headerFactory.createContactHeader(addContactAddress);
        req.addHeader(contactHeader);
        
        System.out.println("Selesai membuat register request message");
        System.out.println("Isi message : "+req);
        System.out.println("Mengirim message");
        sipProvider.sendRequest(req);
        System.out.println("Message terkirim");
    }
    
    public static void main(String[] args) throws Exception {
        SipSender sender = new SipSender();
        sender.kirimRegister();
    }
    
}
