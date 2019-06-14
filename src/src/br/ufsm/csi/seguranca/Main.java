package br.ufsm.csi.seguranca;

import br.ufsm.csi.seguranca.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Main {

    private static int numberOfCreatorThreads = 3;
    private static Thread[] pilaCoinCreatorThreads = new Thread[numberOfCreatorThreads];

    private static String certificateFileName = "certificate.per";
    private static File certificateFile = new File(certificateFileName);
    private static String id = "christian";
    private static Thread userScoutThread;
    private static Thread masterScoutThread;
    private static Usuario usuario;



    public static void main(String[] args) throws Exception
    {
        CreateCertificate();

        PilaCoinStorage pilaCoinStorage = new PilaCoinBinaryStorage("pila_coin_storage.bin", true);
        pilaCoinStorage.Clear();


        usuario.setChavePublica(PersonalCertificate.getInstance().getPublicKey());
        usuario.setId(id);
        usuario.setMeusPilas(new HashSet<>(Arrays.asList(pilaCoinStorage.GetAll())));

        PilaCoinStorageSaver pilaCoinStorageSaver = new PilaCoinStorageSaver(pilaCoinStorage);


        masterScoutThread = new Thread(MasterScout.getInstance());
        MasterScout.getInstance().setPublicKey(PersonalCertificate.getInstance().getPublicKey());
        masterScoutThread.start();

        userScoutThread = new Thread(UserScout.getInstance());
        userScoutThread.start();


        SerializationUtils.SerializeObject(PersonalCertificate.getInstance(), new FileOutputStream(certificateFile));


        for(int i = 0; i < numberOfCreatorThreads; i++)
        {
            pilaCoinCreatorThreads[i] = new Thread(new PilaCoinCreator(id, PersonalCertificate.getInstance().getPublicKey()));
        }
        for(int i = 0; i < numberOfCreatorThreads; i++)
        {
            pilaCoinCreatorThreads[i].start();
        }



        PilaCoinValidatorManager.getInstance().StartValidation();
    }

    public static void CreateCertificate()
    {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(1024);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        PersonalCertificate.getInstance().setPublicKey(keyPair.getPublic());
        PersonalCertificate.getInstance().setPrivateKey(keyPair.getPrivate());
    }
}
