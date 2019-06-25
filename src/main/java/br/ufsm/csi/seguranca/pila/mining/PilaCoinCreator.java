package br.ufsm.csi.seguranca.pila.mining;

import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.PersonalCertificate;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.util.RSAUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Date;
import java.util.HashSet;

public class PilaCoinCreator implements Runnable
{

    private static String idCriador;
    private PublicKey publicKey;
    private static final BigInteger maxMagicalNumber = new BigInteger("99999998000000000000000000000000000000000000000000000000000000000000000");
    private static PilaCoin prototype = CreatePrototype();
    private static long magicalNumber = Long.MIN_VALUE;
    private static HashSet<PilaCoinObserver> pilaCoinObservers = new HashSet<>();

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
            for (PilaCoinObserver pilaCoinObserver : pilaCoinObservers)
            {
                pilaCoinObserver.OnCreatedPilaCoin(pilaCoinCreator, pilaCoin);
            }
        }

    }

    public PilaCoinCreator(String idCriador, PublicKey publicKey)
    {
        this.idCriador = idCriador;
        this.publicKey = publicKey;
    }

    private static PilaCoin CreatePrototype()
    {
        PilaCoin prototype = new PilaCoin();
        prototype.setIdCriador(idCriador);
        try
        {
            prototype.setAssinaturaMaster(RSAUtil.getMasterPublicKey().getEncoded());
            prototype.setChaveCriador(PersonalCertificate.getInstance().getPublicKey());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return prototype;
    }

    private static synchronized long CreateMagicalNumber()
    {
        long magical = magicalNumber;
        magicalNumber++;
        return magical;
    }

    @Override
    public void run()
    {
        while (true)
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
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
                catch (NoSuchAlgorithmException e)
                {
                    e.printStackTrace();
                }
            } while (new BigInteger(1, hash).compareTo(maxMagicalNumber) >= 0);
            System.out.println("Found: " + magicalNumber);
            CallObservers(this, pilaCoin);
        }
    }
}
