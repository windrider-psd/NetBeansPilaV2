/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinStorage;
import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.model.PersonalCertificate;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.model.Transacao;
import br.ufsm.csi.seguranca.pila.model.User;
import br.ufsm.csi.seguranca.pila.model.Usuario;
import br.ufsm.csi.seguranca.pila.scouting.MasterScoutObserver;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidatorManagerObserver;
import br.ufsm.csi.seguranca.pilacoin.PilaDHTClient;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;

/**
 *
 * @author politecnico
 */
public class PilaDHTClientManager implements UDPListenerObserver, MasterScoutObserver, PilaCoinValidatorManagerObserver 
{
    private PilaDHTClient client;
    private String id;
    private PilaCoinStorage pilaCoinStorage;
    
    private Usuario usuario;
    private Socket socket; 

    private UDPBroadcaster userUDPBroadcaster;

    
    
    private final Set<PilaDHTClientManagerObserver> observers = new HashSet<>();
    
    private PilaDHTClientManager() {
    }
    
    public void SetUp(String id, PilaCoinStorage pilaCoinStorage, Usuario usuario, UDPBroadcaster userUDPBroadcaster) {
        this.id = id;
        this.pilaCoinStorage = pilaCoinStorage;
        this.usuario = usuario;
        this.userUDPBroadcaster = userUDPBroadcaster;
    }
    
    private static PilaDHTClientManager instance = null;
    
    public static synchronized PilaDHTClientManager getInstance()
    {
        if(instance == null)
        {
            instance = new PilaDHTClientManager();
        }
        return instance;
    }
    
  
    
    @Override
    public void OnPacket(DatagramPacket datagramPacket) {
        try
        {
            byte[] data = datagramPacket.getData();             
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
            Mensagem mensagem = (Mensagem) SerializationUtils.DeserializeObject(byteArrayInputStream);    
            byteArrayInputStream.close();
            
            if(mensagem.getTipo() == Mensagem.TipoMensagem.PILA_TRANSF && !mensagem.getIdOrigem().equals(id))
            {
               if(mensagem.getPilaCoin().getTransacoes() == null)
               {
                   mensagem.getPilaCoin().setTransacoes(new ArrayList<>());
               }
                
               List<Transacao> transacoes = mensagem.getPilaCoin().getTransacoes();
               
               int lastIndex = transacoes.size() - 1;
               
               if(transacoes.get(lastIndex).getIdNovoDono().equals(id))
               {
                   if(pilaCoinStorage.Contains(mensagem.getPilaCoin()))
                   {
                       pilaCoinStorage.Update(mensagem.getPilaCoin());
                   }
                   else
                   {
                       pilaCoinStorage.Add(mensagem.getPilaCoin());
                   }
               }
            }
        }
        catch(Exception ex)
        {
            
        }
    }
    
    public void SellPilaCoin(Long pilaCoinId, String targetId) throws IOException, Exception
    {
        try {
            PilaCoin pilaCoin = GetPilaCoinFromClient(pilaCoinId);
            if(pilaCoin != null)
            {
                Transacao transacao = new Transacao();
                transacao.setAssinaturaDono(CreateSignature(pilaCoin));
                transacao.setDataTransacao(new Date());
                transacao.setIdNovoDono(targetId);
               
                pilaCoin.getTransacoes().add(transacao);
                
                Mensagem mensagem = new Mensagem();
                mensagem.setChavePublica(PersonalCertificate.getInstance().getPublicKey());
                mensagem.setIdOrigem(id);
                mensagem.setPilaCoin(pilaCoin);
                mensagem.setTipo(Mensagem.TipoMensagem.PILA_TRANSF);
                
                userUDPBroadcaster.BroadcastSingle(mensagem, true);
                CallObserversSell(pilaCoin);
                this.pilaCoinStorage.Remove(pilaCoin);
            }
            else
            {
                throw new Exception("PilaCoin not found in wallet");
            }
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(PilaDHTClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private PilaCoin GetPilaCoinFromClient(Long pilaCoinId)
    {
        try
        {
            Set<PilaCoin> meusPilas = this.client.getUsuario(id).getMeusPilas();
            for (PilaCoin pilaCoin : meusPilas)
            {
                if(Objects.equals(pilaCoin.getId(), pilaCoinId))
                {
                    return pilaCoin;
                }
            }
            return null;
        }
        catch (IOException | ClassNotFoundException ex)
        {
            Logger.getLogger(PilaDHTClientManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
    }
    private byte[] CreateSignature(PilaCoin pilaCoin) throws IOException
    {
       try
       {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            SerializationUtils.SerializeObject(pilaCoin, byteArrayOutputStream);
            byte[] buffer = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(buffer);
            Cipher cipher = Cipher.getInstance("RSA");
            byte[] signature;
            synchronized(cipher)
            {
                cipher.init(Cipher.ENCRYPT_MODE, PersonalCertificate.getInstance().getPrivateKey());
                signature = cipher.doFinal(hash);
            }
            return signature;
       }
       catch(Exception ex)
       {
            return null;
       }
    }

    @Override
    public void OnMasterFound(InetAddress inetAddress, int port) {
        try {
            this.client = new PilaDHTClient(inetAddress.toString().substring(1), 4001, this.usuario);
            
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(PilaDHTClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void OnMasterError() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void OnFinishedValidation(PilaCoin pilaCoin) {
        try {
            this.client.setPilaCoin(pilaCoin);
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(PilaDHTClientManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void AddObserver(PilaDHTClientManagerObserver observer)
    {
        this.observers.add(observer);
    }
    public void RemoveObserver(PilaDHTClientManagerObserver observer)
    {
        this.observers.remove(observer);
    }
    
    private void CallObserversSell(PilaCoin pilaCoin)
    {
        this.observers.forEach((t) ->
        {
            t.OnSoldPilaCoin(pilaCoin);
        });
    }
    
    public PilaDHTClient getClient() {
        return client;
    }

    public void setClient(PilaDHTClient client) {
        this.client = client;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PilaCoinStorage getPilaCoinStorage() {
        return pilaCoinStorage;
    }

    public void setPilaCoinStorage(PilaCoinStorage pilaCoinStorage) {
        this.pilaCoinStorage = pilaCoinStorage;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public UDPBroadcaster getUserUDPBroadcaster() {
        return userUDPBroadcaster;
    }

    public void setUserUDPBroadcaster(UDPBroadcaster userUDPBroadcaster) {
        this.userUDPBroadcaster = userUDPBroadcaster;
    }
    
    
    
    
    
}
