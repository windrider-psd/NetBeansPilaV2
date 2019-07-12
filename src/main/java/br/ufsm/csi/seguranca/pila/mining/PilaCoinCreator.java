package br.ufsm.csi.seguranca.pila.mining;

import br.ufsm.csi.seguranca.Main;
import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.PersonalCertificate;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.model.Transacao;
import br.ufsm.csi.seguranca.util.RSAUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;

public class PilaCoinCreator
{

    private static String idCriador;
    private PublicKey publicKey;
    private static final BigInteger maxMagicalNumber = new BigInteger("99999998000000000000000000000000000000000000000000000000000000000000000");
    private PilaCoin prototype;
    public static long magicalNumber = Long.MIN_VALUE;
    private static final HashSet<PilaCoinObserver> pilaCoinObservers = new HashSet<>();
    private boolean start = false;
    private Thread thread;
    
    private PilaCoinCreator instance;
    
    public void Start()
    {
        synchronized(this)
        {
            if(start)
            {
                return;
            }
            
            if(thread == null || !thread.isAlive())
            {
                thread = new Thread(this.runnable);
            }
            start = true;
            if(!thread.isAlive())
            {
                thread.start();
            }
        }
    }
    
    public void Stop()
    {
        synchronized(this)
        {
            start = false;
        }
    }
    
    private boolean getStart()
    {
        synchronized(this)
        {
            return start;
        }
    }
    
    public static void AddCreationObserver(PilaCoinObserver pilaCoinObserver)
    {
        synchronized (pilaCoinObservers)
        {
            pilaCoinObservers.add(pilaCoinObserver);
        }

    }

    public static void RemoveCreationObserver(PilaCoinObserver pilaCoinObserver)
    {
        synchronized (pilaCoinObservers)
        {
            pilaCoinObservers.remove(pilaCoinObserver);
        }

    }

    private static void CallObservers(PilaCoinCreator pilaCoinCreator, PilaCoin pilaCoin)
    {
        //System.out.println("observers size : " + pilaCoinObservers.size());
        synchronized (pilaCoinObservers)
        {
            pilaCoinObservers.forEach((pilaCoinObserver) ->
            {
                pilaCoinObserver.OnCreatedPilaCoin(pilaCoinCreator, pilaCoin);
            });
        }

    }

    public PilaCoinCreator(String idCriador, PublicKey publicKey)
    {
        this.idCriador = idCriador;
        this.prototype = CreatePrototype();
        this.publicKey = publicKey;
        this.instance = this;
    }

    private PilaCoin CreatePrototype()
    {
        PilaCoin pilaCoin = new PilaCoin();
        pilaCoin.setIdCriador(idCriador);
        pilaCoin.setTransacoes(new ArrayList<>());
        try
        {
            pilaCoin.setAssinaturaMaster(RSAUtil.getMasterPublicKey().getEncoded());
            pilaCoin.setChaveCriador(PersonalCertificate.getInstance().getPublicKey());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return pilaCoin;
    }

    private static synchronized long CreateMagicalNumber()
    {
        long magical = magicalNumber;
        magicalNumber++;
        return magical;
    }

    
    private final Runnable runnable = new Runnable(){
        @Override
        public void run()
        {
            while (getStart())
            {
                PilaCoin pilaCoin;
                byte[] hash = null;
                do
                {
                    Long magicalNumber = CreateMagicalNumber();
                    pilaCoin = (PilaCoin) prototype.clone();
                    pilaCoin.setDataCriacao(new Date());
                    pilaCoin.setNumeroMagico(magicalNumber);
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try
                    {
                        SerializationUtils.SerializeObject(pilaCoin, byteArrayOutputStream);

                        byte[] pilaBytes = byteArrayOutputStream.toByteArray();

                        MessageDigest digest = MessageDigest.getInstance("SHA-256");
                        synchronized (digest)
                        {
                            hash = digest.digest(pilaBytes);
                        }

                    }
                    catch (IOException | NoSuchAlgorithmException ex)
                    {
                        ex.printStackTrace();
                    }

                } while (new BigInteger(1, hash).compareTo(maxMagicalNumber) >= 0);

                System.out.println("Found: " + magicalNumber);
                CallObservers(instance, pilaCoin);
            }
        }
        
    };

    
   
}
