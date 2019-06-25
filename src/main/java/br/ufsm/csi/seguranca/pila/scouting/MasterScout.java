package br.ufsm.csi.seguranca.pila.scouting;

import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.network.UDPBroadcasterObserver;
import br.ufsm.csi.seguranca.pila.network.UDPListenerObserver;

import java.io.ByteArrayInputStream;
import java.net.*;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;

public class MasterScout implements UDPListenerObserver, UDPBroadcasterObserver
{

    private static MasterScout instance;
    private Set<MasterScoutObserver> observerSet = new HashSet<>();
    private boolean ready = false;

    private InetAddress masterAddress;
    private int masterPort;

    private PublicKey publicKey;

    private MasterScout()
    {

    }

    public Mensagem CreateMessage(String id, InetAddress inetAddress, PublicKey publicKey, int port)
    {
        Mensagem broadcastMessage = new Mensagem();
        broadcastMessage.setMaster(false);
        broadcastMessage.setPorta(port);
        broadcastMessage.setTipo(Mensagem.TipoMensagem.DISCOVER);
        broadcastMessage.setEndereco(inetAddress);
        broadcastMessage.setIdOrigem(id);
        broadcastMessage.setChavePublica(publicKey);
        return broadcastMessage;
    }

    public void AddObserver(MasterScoutObserver masterScoutObserver)
    {
        synchronized (observerSet)
        {
            observerSet.add(masterScoutObserver);
        }
        if (ready)
        {
            masterScoutObserver.OnMasterFound(masterAddress, masterPort);
        }
    }

    public void RemoveObserver(MasterScoutObserver masterScoutObserver)
    {
        synchronized (observerSet)
        {
            observerSet.remove(masterScoutObserver);
        }
    }

    public static MasterScout getInstance()
    {
        if (instance == null)
        {
            instance = new MasterScout();
        }
        return instance;
    }

    private void CallObservers()
    {
        synchronized (observerSet)
        {
            if (this.ready)
            {
                for (MasterScoutObserver masterScoutObserver : observerSet)
                {
                    masterScoutObserver.OnMasterFound(this.masterAddress, this.masterPort);
                }
            }
            else
            {
                for (MasterScoutObserver masterScoutObserver : observerSet)
                {
                    masterScoutObserver.OnMasterError();
                }
            }

        }
    }

    private boolean ValidateServer(Mensagem message)
    {
        return message.getTipo() == Mensagem.TipoMensagem.DISCOVER_RESP && message.isMaster();
    }

    private void setReady(boolean ready)
    {
        this.ready = ready;

        CallObservers();
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey)
    {
        this.publicKey = publicKey;
    }

    @Override
    public void OnPacket(DatagramPacket datagramPacket)
    {

        try
        {
            byte[] data = datagramPacket.getData();
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            Mensagem mensagem = (Mensagem) SerializationUtils.DeserializeObject(byteArrayInputStream);
            byteArrayInputStream.close();

            if (ValidateServer(mensagem))
            {
                this.masterAddress = mensagem.getEndereco();
                this.masterPort = mensagem.getPorta();
                if (!ready)
                {
                    setReady(true);
                }

            }
            else if (ready)
            {
                setReady(false);
            }
        }
        catch (Exception ex)
        {

        }

    }

    @Override
    public void OnMessageSent(DatagramPacket datagramPacket)
    {
        System.out.println("A message has been sent to master");
    }
}
