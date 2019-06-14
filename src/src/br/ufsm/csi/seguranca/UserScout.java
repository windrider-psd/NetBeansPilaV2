package br.ufsm.csi.seguranca;

import br.ufsm.csi.seguranca.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.model.Mensagem;
import br.ufsm.csi.seguranca.pila.model.PersonalCertificate;
import sun.plugin2.message.Message;

import javax.sound.midi.Receiver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;

public class UserScout implements Runnable
{

    private static String id = "christian";
    private class UserReceiver implements Runnable
    {
        public DatagramSocket datagramSocket;
        public UserReceiver(DatagramSocket datagramSocket) {
            this.datagramSocket = datagramSocket;
        }

        @Override
        public void run() {
            while (true){
                try
                {
                    byte[] receiveBuffer = new byte[5012];

                    DatagramPacket datagramPacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                    datagramSocket.receive(datagramPacket);
                    System.out.println("User scouting message has been received");

                    byte[] data = datagramPacket.getData();
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(data);
                    Mensagem mensagem = (Mensagem) SerializationUtils.DeserializeObject(byteArrayInputStream);
                    byteArrayInputStream.close();

                    if(IsValidMessage(mensagem))
                    {
                        System.out.println("Found user: " + mensagem.getIdOrigem());
                        User user = new User(mensagem.getIdOrigem(), mensagem.getEndereco(), mensagem.getChavePublica());
                        CallObservers(user);
                    }

                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    continue;
                }
            }
        }
        private boolean IsValidMessage(Mensagem message)
        {
            return !message.getIdOrigem().equals(id) && message.getTipo() == Mensagem.TipoMensagem.DISCOVER
                    && message.getIdOrigem() != null && message.isMaster() == false
                    && message.getEndereco() != null && message.getChavePublica() != null;
        }
    }

    private DatagramSocket datagramSocket;
    private static UserScout instance;
    private int port = 4001;

    private Set<UserScoutObserver> userScoutObserverSet = new HashSet<>();
    private Mensagem broadcastMessage;

    public UserScout() throws IOException
    {
        datagramSocket = new DatagramSocket(4001);
        datagramSocket.setBroadcast(true);

        broadcastMessage = new Mensagem();
        broadcastMessage.setMaster(false);
        broadcastMessage.setPorta(5800);
        broadcastMessage.setTipo(Mensagem.TipoMensagem.DISCOVER);
        broadcastMessage.setEndereco(datagramSocket.getInetAddress());
        broadcastMessage.setIdOrigem("christian");
        broadcastMessage.setChavePublica(PersonalCertificate.getInstance().getPublicKey());
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


    @Override
    public void run() {

        UserReceiver receiver = new UserReceiver(this.datagramSocket);

        Thread thread = new Thread(receiver);
        thread.start();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            SerializationUtils.SerializeObject(broadcastMessage, byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        byte[] encodedMessage = byteArrayOutputStream.toByteArray();

        while(true)
        {
            try
            {
                DatagramPacket datagramPacket = new DatagramPacket(encodedMessage, encodedMessage.length, InetAddress.getByName("255.255.255.255"), 4001);
                datagramSocket.send(datagramPacket);
                System.out.println("User scout message has been sent");
                Thread.sleep(15000);
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
            }
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

}
