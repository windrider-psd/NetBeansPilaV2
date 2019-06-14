package br.ufsm.csi.seguranca;

import br.ufsm.csi.seguranca.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.model.PersonalCertificate;
import br.ufsm.csi.seguranca.util.RSAUtil;
import jdk.nashorn.internal.runtime.Debug;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

public class MasterScout implements Runnable{

    private static MasterScout instance;
    private int port = 5000;
    private DatagramSocket datagramSocket;
    private Set<MasterScoutObserver> observerSet = new HashSet<>();
    private boolean ready = false;

    private InetAddress masterAddress;
    private int masterPort;

    private Mensagem broadcastMessage;
    private PublicKey publicKey;



    private MasterScout() throws SocketException {
        datagramSocket = new DatagramSocket(port);
        datagramSocket.setBroadcast(true);

        broadcastMessage = new Mensagem();
        broadcastMessage.setMaster(false);
        broadcastMessage.setPorta(port);
        broadcastMessage.setTipo(Mensagem.TipoMensagem.DISCOVER);
        broadcastMessage.setEndereco(datagramSocket.getInetAddress());
        broadcastMessage.setIdOrigem("christian");
        broadcastMessage.setChavePublica(PersonalCertificate.getInstance().getPublicKey());
    }

    public static MasterScout getInstance()
    {
        try {
            if(instance == null)
            {
                instance = new MasterScout();
            }
            return instance;
        }
        catch (Exception ex)
        {
            return  null;
        }

    }


    public void AddObserver(MasterScoutObserver masterScoutObserver)
    {
        synchronized (observerSet)
        {
            observerSet.add(masterScoutObserver);
        }
        if(ready)
        {
            masterScoutObserver.OnReady(masterAddress, masterPort);
        }
    }


    public void RemoveObserver(MasterScoutObserver masterScoutObserver)
    {
        synchronized (observerSet)
        {
            observerSet.remove(masterScoutObserver);
        }
    }

    private void CallObservers()
    {
        synchronized (observerSet)
        {
            if(this.ready)
            {
                for(MasterScoutObserver masterScoutObserver : observerSet)
                {
                     masterScoutObserver.OnReady(this.masterAddress, this.masterPort);
                }
            }
            else
            {
                for(MasterScoutObserver masterScoutObserver : observerSet)
                {
                    masterScoutObserver.OnError();
                }
            }

        }
    }


    @Override
    public void run() {
        broadcastMessage.setChavePublica(publicKey);


        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            SerializationUtils.SerializeObject(broadcastMessage, byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] encodedMessage = byteArrayOutputStream.toByteArray();

        while (true)
        {
            byte[] recieveBuffer = new byte[5012];
            try {
                DatagramPacket datagramPacket = new DatagramPacket(encodedMessage, encodedMessage.length, InetAddress.getByName("192.168.90.209"), 3333);

                datagramSocket.send(datagramPacket);
                System.out.println("Message to master has been sent");

                DatagramPacket incomingPacket = new DatagramPacket(recieveBuffer, recieveBuffer.length);
                datagramSocket.receive(incomingPacket);

                System.out.println("Message to master has been received");

                byte[] data = incomingPacket.getData();
                ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                Mensagem mensagem = (Mensagem) SerializationUtils.DeserializeObject(byteArrayInputStream);

                if(ValidateServer(mensagem))
                {
                    this.masterAddress = mensagem.getEndereco();
                    this.masterPort = mensagem.getPorta();
                    if(!ready)
                    {
                        setReady(true);
                    }

                }
                else if(ready) {
                    setReady(false);
                }

                Thread.sleep(15000);
            }
            catch (InterruptedException ex)
            {
                continue;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean ValidateServer(Mensagem message)
    {
        return message.getTipo() == Mensagem.TipoMensagem.DISCOVER_RESP && message.isMaster();
    }

    private void setReady(boolean ready) {
        this.ready = ready;

        CallObservers();
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }
}
