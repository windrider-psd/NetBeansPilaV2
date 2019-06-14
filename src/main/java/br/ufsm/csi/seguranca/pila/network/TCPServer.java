/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author politecnico
 */
public class TCPServer implements Runnable
{
   
    private class InputReader implements Runnable
    {
        
        private Thread thread;
        private boolean stop = true;

        public InputReader() {
            this.thread = new Thread(this);
        }
        
        public void Start()
        {
            synchronized(this)
            {
                stop = false;
                if(!thread.isAlive())
                {
                    thread = new Thread(this);
                    thread.start();
                }
                   
            }
        }
        
        private void Stop()
        {
            synchronized(this)
            {
                stop = true;
            }
        }
        
        public void run() {
           
            
            while(!CheckStop())
            {
                try
                {
                    
                    synchronized(clients)
                    {
                        Set<Socket> closedSockets = new HashSet<>();
                        for(Socket client : clients)
                        {
                            
                            if(!client.isConnected())
                            {
                                closedSockets.add(client);
                                continue;
                            }
                            
                            byte[] buf = new byte[clientBufferSize];
                            
                            if(client.getInputStream().read(buf) != 0)
                            {
                                Object object;
                                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buf)) {
                                    object = SerializationUtils.DeserializeObject(byteArrayInputStream);
                                }
                                System.out.println(object);
                                CallObserversMessageReceived(client, object);
                            }
                            
                            

                        }
                        
                        for(Socket socket : closedSockets)
                        {
                            clients.remove(socket);
                        }
                        
                    }
                    
                    Thread.sleep(1000);
                    
                    
                }
                catch(Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        
        private boolean CheckStop()
        {
            synchronized(this)
            {
                return this.stop;
            }
        }
        
    }
    
    private ServerSocket serverSocket;
    
    private boolean stopListening = true;
    private Thread thread;
    
    private Set<Socket> clients = new HashSet<>();
    
    private final Set<TCPServerObserver> observers = new HashSet<>();
    private final InputReader inputReader = new InputReader();
    private int clientBufferSize;
    
    public TCPServer(ServerSocket serverSocket, int clientBufferSize) 
    {
        this.serverSocket = serverSocket;
        this.clientBufferSize = clientBufferSize;
        this.thread = new Thread(this);
        
    }
    
    
    @Override
    public void run() {
        while(!stopListening)
        {
            try
            {
                Socket client = serverSocket.accept();
                System.out.println("Client connected");
                synchronized(this.clients)
                {
                    clients.add(client);
                }
                CallObserversConnection(client);
            }
            catch(Exception ex)
            {
                break;
            }
            
        }
        StopListening();
    }
    
    
   
    
    public void SendMessage(Socket client, Object obj) throws IOException
    {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        objectOutputStream.writeObject(obj);
        objectOutputStream.close();
        CallObserversMessageSent(client, obj);
    }
    
    public void StartListening()
    {
        stopListening = false;
        if(!thread.isAlive())
        {
            thread = new Thread(this);
            thread.start();
        }
        inputReader.Start();
        
    }
    
    public void StopListening()
    {
        stopListening = true;
        synchronized(this.clients)
        {
            for(Socket client : clients){
                try {
                    client.close();
                } catch (IOException ex) {
                    Logger.getLogger(TCPServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            
            clients.clear();
        }
        inputReader.Stop();
        
    }
    
    public void AddObserver(TCPServerObserver observer)
    {
        observers.add(observer);
    }
    public void RemoveObserver(TCPServerObserver observer)
    {
        observers.remove(observer);
    }
    
    private void CallObserversConnection(Socket socket)
    {
        for(TCPServerObserver observer : observers)
        {
           observer.OnConnection(socket);
        }
    }
    
    private void CallObserversMessageReceived(Socket socket, Object message)
    {
        for(TCPServerObserver observer : observers)
        {
           observer.OnMessageReceived(socket, message);
        }
    }
    
    private void CallObserversMessageSent(Socket socket, Object message)
    {
        for(TCPServerObserver observer : observers)
        {
           observer.OnMessageSent(socket, message);
        }
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public Set<Socket> getClients() {
        return clients;
    }

    public void setClients(Set<Socket> clients) {
        this.clients = clients;
    }

    public int getClientBufferSize() {
        return clientBufferSize;
    }

    public void setClientBufferSize(int clientBufferSize) {
        this.clientBufferSize = clientBufferSize;
    }
    
    
    
    
    
}
