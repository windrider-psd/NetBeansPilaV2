package br.ufsm.csi.seguranca.pila.validation;

import br.ufsm.csi.seguranca.pila.scouting.MasterScout;
import br.ufsm.csi.seguranca.pila.scouting.MasterScoutObserver;
import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.ObjetoTroca;
import br.ufsm.csi.seguranca.pila.model.PersonalCertificate;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.util.RSAUtil;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class PilaCoinValidator implements Runnable, MasterScoutObserver {

    private PilaCoin pilaCoin;
    private InetSocketAddress socketAddress;
    private static Set<PilaCoinValidatorObserver> observers = new HashSet<>();

    private InetAddress masterInetAddress;
    private int masterPort;
    private boolean ready = false;

    @Override
    public void run() {

        try
        {
            PublicKey masterPublicKey = RSAUtil.getMasterPublicKey();

            //Criar uma chave de sessão e criptografá-la com a chave pública do servidor;
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            SecretKey secretKey;
            synchronized (keyGenerator)
            {
                keyGenerator.init(128);
                secretKey = keyGenerator.generateKey();
            }

            Cipher cipher = Cipher.getInstance("RSA");
            byte[] secretKeyEncryptedBytes;
            synchronized (cipher)
            {
                cipher.init(Cipher.ENCRYPT_MODE, masterPublicKey);
                secretKeyEncryptedBytes = cipher.doFinal(secretKey.getEncoded());
            }
            //----------------------------------


            // - Serializar o pila e criptografá-lo com a chave de sessão.
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            SerializationUtils.SerializeObject(pilaCoin, byteArrayOutputStream);
            byte[] pilaBytes = byteArrayOutputStream.toByteArray();
            byte[] encodedPilaBytes;

            cipher = Cipher.getInstance("AES");
            synchronized (cipher)
            {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                encodedPilaBytes = cipher.doFinal(pilaBytes);
            }
            //---------------------------------------------


            //Assinatura
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] pilaHash = digest.digest(pilaBytes);
            byte[] signature;

            cipher = Cipher.getInstance("RSA");
            synchronized (cipher)
            {
                cipher.init(Cipher.ENCRYPT_MODE, PersonalCertificate.getInstance().getPrivateKey());
                signature = cipher.doFinal(pilaHash);
            }

            //----------


            ObjetoTroca objetoTroca = new ObjetoTroca();
            objetoTroca.setChaveSessao(secretKeyEncryptedBytes);
            objetoTroca.setObjetoSerializadoCriptografado(encodedPilaBytes);
            objetoTroca.setAssinatura(signature);
            objetoTroca.setChavePublica(PersonalCertificate.getInstance().getPublicKey());

            //cipher.init(Cipher.DECRYPT_MODE, PersonalCertificate.getInstance().getPublicKey());
           // byte[] hashAssinatura = cipher.doFinal(objetoTroca.getAssinatura());

            
            while (!ready)
            {
            }

            //System.out.println("Creating socket: " + masterInetAddress.toString() + ":" + masterPort);
            
            Socket socket = new Socket(masterInetAddress, masterPort);

            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(objetoTroca);

            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
            Object object = objectInputStream.readObject();
            while (object == null)
            {
                Thread.sleep(1000);
            }
            /*Mensagem mensagem = (Mensagem) object;
            System.out.println(mensagem.getErro());

            BigInteger bigInteger = new BigInteger(1, pilaHash);
            System.out.println(bigInteger.compareTo(new BigInteger("99999998000000000000000000000000000000000000000000000000000000000000000000")) < 0);*/

            ObjetoTroca masterObjetoTroca = (ObjetoTroca)object;

            byte[] cryptoObject = masterObjetoTroca.getObjetoSerializadoCriptografado();
            byte[] masterPilaBytes;
            Cipher cipherAES = Cipher.getInstance("AES");
            synchronized (cipherAES)
            {
                cipherAES.init(Cipher.DECRYPT_MODE, secretKey);
                masterPilaBytes = cipherAES.doFinal(cryptoObject);
            }

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(masterPilaBytes);
            PilaCoin validatedPilaCoin = (PilaCoin) SerializationUtils.DeserializeObject(byteArrayInputStream);
            
            /*
            
            ByteArrayOutputStream validatedOS = new ByteArrayOutputStream();
            SerializationUtils.SerializeObject(validatedPilaCoin, validatedOS);
            
            byte[] validatedPilaBytes = validatedOS.toByteArray();
            byte[] validatedPilaHash = digest.digest(validatedPilaBytes);
            
            byte[] validatedSignature = validatedPilaCoin.getAssinaturaMaster();
            Cipher cipherRSA = Cipher.getInstance("RSA");
            byte[] hashValidatedSignature;
            synchronized(cipherRSA)
            {
                cipherRSA.init(Cipher.DECRYPT_MODE, masterPublicKey);
                hashValidatedSignature = cipherRSA.doFinal(validatedSignature);
            }
            System.out.println(Arrays.equals(validatedPilaHash, hashValidatedSignature));*/
            
            this.pilaCoin.setId(validatedPilaCoin.getId());
            this.pilaCoin.setTransacoes(validatedPilaCoin.getTransacoes());
            this.CallObservers();

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public static void AddObserver(PilaCoinValidatorObserver observer)
    {
        observers.add(observer);
    }

    public static void RemoveObserver(PilaCoinValidatorObserver observer)
    {
        observers.remove(observer);
    }

    protected void CallObservers()
    {
        for(PilaCoinValidatorObserver pilaCoinValidatorObserver : observers)
        {
            pilaCoinValidatorObserver.OnPilaCoinValidatorReady(this);
        }
    }

    public PilaCoin getPilaCoin() {
        return pilaCoin;
    }

    public void setPilaCoin(PilaCoin pilaCoin) {
        this.pilaCoin = pilaCoin;
    }



    @Override
    public void OnMasterFound(InetAddress inetAddress, int port) {
        this.masterInetAddress = inetAddress;
        this.masterPort = port;
        ready = true;
    }

    @Override
    public void OnMasterError() {
        ready = false;
        this.masterInetAddress = null;
        this.masterPort = -1;
    }

    public PilaCoinValidator() {
        MasterScout.getInstance().AddObserver(this);

    }
}
