package br.ufsm.csi.seguranca;

import br.ufsm.csi.seguranca.node.NodeListener;
import br.ufsm.csi.seguranca.pila.scouting.UserScout;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidatorManager;
import br.ufsm.csi.seguranca.pila.scouting.MasterScout;
import br.ufsm.csi.seguranca.pila.mining.PilaCoinCreator;
import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinStorageSaver;
import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinBinaryStorage;
import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinStorage;
import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.*;
import br.ufsm.csi.seguranca.pila.network.TCPServer;
import br.ufsm.csi.seguranca.pila.network.UDPBroadcaster;
import br.ufsm.csi.seguranca.pila.network.UDPListener;
import br.ufsm.csi.seguranca.util.RandomString;
import br.ufsm.csi.seguranca.pila.network.PilaDHTClientManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main 
{

    private static String id;
    private static Usuario usuario;
    
    private static final int numberOfCreatorThreads = 3;
    private static final Thread[] pilaCoinCreatorThreads = new Thread[numberOfCreatorThreads];

    private static final String certificateFileName = "certificate.per";
    private static final File certificateFile = new File(certificateFileName);
    
    private static UDPBroadcaster udpMasterBroadcaster;
    private static UDPListener udpMasterListener;
    
    private static UDPBroadcaster udpUserBroadcaster;
    private static UDPListener udpUserListener;
    
    private static PilaCoinStorage pilaCoinStorage;
    private static TCPServer tCPServer;
    
    private static InetAddress localhost;
    
    private static PilaDHTClientManager clientManager; 
    
    public static void main(String[] args) throws Exception
    {
        
        
        
        NodeListener nodelistener = new NodeListener();
        CreateId();
  
        CreateCertificate();
        
        SetUpSockets();
        
        SetUpStorage();

        SetUpDHT();
        
        PilaCoinStorageSaver pilaCoinStorageSaver = new PilaCoinStorageSaver(pilaCoinStorage);
        
        SetUpValidator();
        
        SetUpMining();
        
        
        System.out.println("User Id: " + id);
        System.out.println("TCPServer: " + tCPServer.getServerSocket().getInetAddress() + ":" + tCPServer.getServerSocket().getLocalPort());
        System.out.println("UDPMasterBroadcaster: " + udpMasterBroadcaster.getDatagramSocket().getLocalAddress() + ":" + udpMasterBroadcaster.getDatagramSocket().getLocalPort());
        System.out.println("UDPUserBroadcaster: " + udpUserBroadcaster.getDatagramSocket().getLocalAddress() + ":" + udpUserBroadcaster.getDatagramSocket().getLocalPort());
    }

    public static void CreateCertificate()
    {
        try {
            KeyPairGenerator keyPairGenerator = null;
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
           
            
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            PersonalCertificate.getInstance().setPublicKey(keyPair.getPublic());
            PersonalCertificate.getInstance().setPrivateKey(keyPair.getPrivate());
            
            try (FileOutputStream fileOutputStream = new FileOutputStream(certificateFile)) {
                SerializationUtils.SerializeObject(PersonalCertificate.getInstance(), fileOutputStream);
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static void SetUpSockets() throws Exception
    {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(getLocalHost(), 0));
        
        tCPServer = new TCPServer(serverSocket, 5012, 99999);
        
        UserScout.getInstance().setId(id);
        
        DatagramSocket userDatagramSocket = new DatagramSocket(5001);
        DatagramSocket masterDatagramSocket = new DatagramSocket();
        
        MasterScout.getInstance().setPublicKey(PersonalCertificate.getInstance().getPublicKey());
        
        udpMasterListener = new UDPListener(masterDatagramSocket, 5012);
        Mensagem masterMessage = MasterScout.getInstance().CreateMessage(id, masterDatagramSocket.getLocalAddress(), PersonalCertificate.getInstance().getPublicKey(), masterDatagramSocket.getLocalPort());
        udpMasterBroadcaster = new UDPBroadcaster(masterDatagramSocket, masterMessage, InetAddress.getByName("192.168.82.163"), 3333, 15000);
        
        udpMasterListener.AddObserver(MasterScout.getInstance());
        udpMasterBroadcaster.AddObserver(MasterScout.getInstance());
        
        udpMasterListener.Start();
        udpMasterBroadcaster.Start();
        
        //------------------------------------//
        
        udpUserListener = new UDPListener(userDatagramSocket, 5012);
        Mensagem userMessage = UserScout.getInstance().CreateMessage(id, tCPServer.getServerSocket().getInetAddress(), PersonalCertificate.getInstance().getPublicKey(), tCPServer.getServerSocket().getLocalPort());
        udpUserBroadcaster = new UDPBroadcaster(userDatagramSocket, userMessage, InetAddress.getByName("255.255.255.255"), userDatagramSocket.getLocalPort(), 15000);
        
        udpUserListener.AddObserver(UserScout.getInstance());
        udpMasterBroadcaster.AddObserver(UserScout.getInstance());
        
        udpUserListener.Start();
        udpUserBroadcaster.Start();        
        
        tCPServer.StartListening();
    }
    
    public static void SetUpDHT()
    {
        usuario = new Usuario();
        usuario.setChavePublica(PersonalCertificate.getInstance().getPublicKey());
        usuario.setId(id);
        usuario.setMeusPilas(new HashSet<>(Arrays.asList(pilaCoinStorage.GetAll())));
        usuario.setEndereco(tCPServer.getServerSocket().getInetAddress());
        
        clientManager = new PilaDHTClientManager(id, pilaCoinStorage, usuario, udpUserBroadcaster);
        MasterScout.getInstance().AddObserver(clientManager);
        PilaCoinValidatorManager.getInstance().AddObserver(clientManager);
        udpUserListener.AddObserver(clientManager);
        
    }
    
    public static void SetUpStorage()
    {
        try {
            pilaCoinStorage = new PilaCoinBinaryStorage("pila_coin_storage.pc", true);
            pilaCoinStorage.Clear();
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static void SetUpMining()
    {
        for(int i = 0; i < numberOfCreatorThreads; i++)
        {
            pilaCoinCreatorThreads[i] = new Thread(new PilaCoinCreator(id, PersonalCertificate.getInstance().getPublicKey()));
        }
        for(int i = 0; i < numberOfCreatorThreads; i++)
        {
            pilaCoinCreatorThreads[i].start();
        }
    }
    
    private static void CreateId()
    {
        RandomString randomString = new RandomString(15, new Random());
        id = randomString.nextString();
    }
    
    private static void SetUpValidator()
    {
        MasterScout.getInstance().AddObserver(PilaCoinValidatorManager.getInstance());
        PilaCoinValidatorManager.getInstance().StartValidation();
    }
    
    private static synchronized InetAddress getLocalHost() throws SocketException {
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
}
