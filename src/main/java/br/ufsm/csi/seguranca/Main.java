package br.ufsm.csi.seguranca;

import br.ufsm.csi.seguranca.node.NodeJSListener;
import br.ufsm.csi.seguranca.node.observers.ValidationObserver;
import br.ufsm.csi.seguranca.pila.scouting.UserScout;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidatorManager;
import br.ufsm.csi.seguranca.pila.scouting.MasterScout;
import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinStorageSaver;
import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinBinaryStorage;
import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinStorage;
import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.mining.MiningManager;
import br.ufsm.csi.seguranca.pila.model.*;
import br.ufsm.csi.seguranca.pila.network.UDPBroadcaster;
import br.ufsm.csi.seguranca.pila.network.UDPListener;
import br.ufsm.csi.seguranca.pila.network.PilaDHTClientManager;
import br.ufsm.csi.seguranca.pila.scouting.UserDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main 
{

    public static String id;
    public static Usuario usuario;
    
    private final static int numberOfCreatorThreads = 1;

    private final static String certificateFileName = "certificate.per";
    public static File certificateFile = new File(certificateFileName);
    
    public static UDPBroadcaster udpMasterBroadcaster;
    public static UDPListener udpMasterListener;
    
    public static UDPBroadcaster udpUserBroadcaster;
    public static UDPListener udpUserListener;
    
    public static PilaCoinStorage pilaCoinStorage;
    
    public static InetAddress localhost;
    
    public static PilaDHTClientManager clientManager = PilaDHTClientManager.getInstance(); 
    
    public static User thisUser;
    
    public static void main(String[] args) throws Exception
    {
        
        CreateId();
  
        CreateUser();
        
        CreateCertificate();
        
        SetUpSockets();
        
        SetUpStorage();

        SetUpDHT();
        
        PilaCoinStorageSaver pilaCoinStorageSaver = new PilaCoinStorageSaver(pilaCoinStorage);
        
        SetUpValidator();
        
        SetUpMining();
        
        
        NodeJSListener nodelistener = new NodeJSListener("br.ufsm.csi.seguranca.node.controllers", getLocalHost().toString().substring(1), 1883);
        
        ValidationObserver validationObserver = new ValidationObserver(nodelistener);
        PilaCoinValidatorManager.getInstance().AddObserver(validationObserver);
       
        UserScout.getInstance().AddObserver(UserDatabase.getInstance());
        System.out.println("User Id: " + id);
        System.out.println("UDP-broadcaster: " + udpMasterBroadcaster.getDatagramSocket().getLocalAddress() + ":" + udpMasterBroadcaster.getDatagramSocket().getLocalPort());
        
        
    }

    public static void CreateCertificate()
    {
        try {
            if(!certificateFile.exists())
            {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
                keyPairGenerator.initialize(1024);


                KeyPair keyPair = keyPairGenerator.generateKeyPair();
                PersonalCertificate.getInstance().setPublicKey(keyPair.getPublic());
                PersonalCertificate.getInstance().setPrivateKey(keyPair.getPrivate());

                try (FileOutputStream fileOutputStream = new FileOutputStream(certificateFile)) {
                    SerializationUtils.SerializeObject(PersonalCertificate.getInstance(), fileOutputStream);
                }
            }
            else
            {
                FileInputStream fileInputStream = new FileInputStream(certificateFile);
                
                PersonalCertificate savedPersonalCertificate = (PersonalCertificate) SerializationUtils.DeserializeObject(fileInputStream);
                
                PersonalCertificate.getInstance().setPublicKey(savedPersonalCertificate.getPublicKey());
                PersonalCertificate.getInstance().setPrivateKey(savedPersonalCertificate.getPrivateKey());
                
                fileInputStream.close();
            }
 
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException | ClassNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void SetUpSockets() throws Exception
    {
        
        UserScout.getInstance().setId(id);
        
        DatagramSocket broadcastSocket = new DatagramSocket(3333);
        broadcastSocket.setBroadcast(true);
        
        MasterScout.getInstance().setPublicKey(PersonalCertificate.getInstance().getPublicKey());
        
        udpMasterListener = new UDPListener(broadcastSocket, 5012);
        Mensagem masterMessage = MasterScout.getInstance().CreateMessage(id, broadcastSocket.getLocalAddress(), PersonalCertificate.getInstance().getPublicKey(), broadcastSocket.getLocalPort());
        udpMasterBroadcaster = new UDPBroadcaster(broadcastSocket, masterMessage, InetAddress.getByName("192.168.90.194"), 3333, 15000);
        
        udpMasterListener.AddObserver(MasterScout.getInstance());
        udpMasterBroadcaster.AddObserver(MasterScout.getInstance());
        
        
        //------------------------------------//
        
       // udpUserListener = new UDPListener(userDatagramSocket, 5012);
        Mensagem userMessage = UserScout.getInstance().CreateMessage(id, getLocalHost(), PersonalCertificate.getInstance().getPublicKey(), 4000);
        udpUserBroadcaster = new UDPBroadcaster(broadcastSocket, userMessage, InetAddress.getByName("255.255.255.255"), 3333, 15000);
        
        udpMasterListener.AddObserver(UserScout.getInstance());
        udpUserBroadcaster.AddObserver(UserScout.getInstance());
        
         udpMasterListener.Start();
        //udpUserListener.Start();
        udpUserBroadcaster.Start();    
        udpMasterBroadcaster.Start();
    }
    
    private static void SetUpDHT()
    {
        try
        {
            usuario = new Usuario();
            usuario.setChavePublica(PersonalCertificate.getInstance().getPublicKey());
            usuario.setId(id);  
            usuario.setMeusPilas(new HashSet<>());
            usuario.setEndereco(getLocalHost());
            
            clientManager.SetUp(id, pilaCoinStorage, usuario, udpUserBroadcaster);
            
            MasterScout.getInstance().AddObserver(clientManager);
            PilaCoinValidatorManager.getInstance().AddObserver(clientManager);
            //udpUserListener.AddObserver(clientManager);
        }
        catch (SocketException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    private static void SetUpStorage()
    {
        try {
            pilaCoinStorage = new PilaCoinBinaryStorage("pila_coin_storage.pc", true);
            pilaCoinStorage.Clear();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void SetUpMining()
    {
        MiningManager.getInstance().SetUpCreators(1);
        MiningManager.getInstance().StartMining();
    }
    
    private static void CreateId()
    {
        //RandomString randomString = new RandomString(15, new Random());
        //id = randomString.nextString();
        id = "Christian<script>alert('kiss ( ˘ ³˘)♥')</script>";
    }
    
    private static void SetUpValidator()
    {
        MasterScout.getInstance().AddObserver(PilaCoinValidatorManager.getInstance());
        PilaCoinValidatorManager.getInstance().StartValidation();
    }
    
    public static synchronized InetAddress getLocalHost() throws SocketException {
        if (localhost == null) {
            InetAddress address = null;
            Enumeration en = NetworkInterface.getNetworkInterfaces();
            while(en.hasMoreElements())
            {
                NetworkInterface n = (NetworkInterface) en.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements())
                {
                    InetAddress i = (InetAddress) ee.nextElement();
                    if (i.isSiteLocalAddress() && !i.isLoopbackAddress()) {
                        address = i;
                    }
                }
            }
            localhost = address;
        }
        return localhost;
    }
    
    private static void CreateUser()
    {
        try
        {
            User user = new User();
            user.setId(id);
            user.setInetAddress(getLocalHost());
            user.setPublicKey(PersonalCertificate.getInstance().getPublicKey());
            Main.thisUser = user;
        }
        catch (SocketException ex)
        {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
