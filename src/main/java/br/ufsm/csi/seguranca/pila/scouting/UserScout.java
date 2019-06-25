package br.ufsm.csi.seguranca.pila.scouting;

import br.ufsm.csi.seguranca.pila.model.User;
import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.network.UDPBroadcasterObserver;
import br.ufsm.csi.seguranca.pila.network.UDPListenerObserver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserScout implements UDPListenerObserver, UDPBroadcasterObserver
{

    private String id;
    
    @Override
    public void OnPacket(DatagramPacket datagramPacket) {
        try {
            System.out.println("User scouting message has been received");
            
            byte[] data = datagramPacket.getData();
            Mensagem mensagem;
            try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data)) {
                mensagem = (Mensagem) SerializationUtils.DeserializeObject(byteArrayInputStream);
            }
            
            if(IsValidMessage(mensagem))
            {
                System.out.println("Found user: " + mensagem.getIdOrigem());
                User user = new User(mensagem.getIdOrigem(), mensagem.getEndereco(), mensagem.getChavePublica());
                CallObservers(user);
            }
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(UserScout.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private static UserScout instance;

    private Set<UserScoutObserver> userScoutObserverSet = new HashSet<>();

    public UserScout()
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

    public  static UserScout getInstance()
    {
        try
        {
            if(instance == null)
            {
                instance = new UserScout();
            }
            return instance;
        }
        catch (Exception ex)
        {
            return null;
        }
    }


    public void AddObserver(UserScoutObserver userScoutObserver)
    {
        userScoutObserverSet.add(userScoutObserver);
    }

    public void RemoveObserver(UserScoutObserver userScoutObserver)
    {
        userScoutObserverSet.remove(userScoutObserver);
    }

    private void CallObservers(User user)
    {
        for (UserScoutObserver u : userScoutObserverSet) {
            try {
                u.OnUserFound(user);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
        }
    }
    private boolean IsValidMessage(Mensagem message)
    {        
        return !message.getIdOrigem().equals(id) && message.getTipo() == Mensagem.TipoMensagem.DISCOVER
                && message.getIdOrigem() != null && message.isMaster() == false
                && message.getEndereco() != null && message.getChavePublica() != null;
    }

    @Override
    public void OnMessageSent(DatagramPacket datagramPacket) {
        System.out.println("A broadcast message has been sent to users");
    }

    public void setId(String id) {
        this.id = id;
    }
    
    
}
