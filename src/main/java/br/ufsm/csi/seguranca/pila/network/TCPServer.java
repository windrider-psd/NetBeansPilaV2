/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.lang3.ArrayUtils;

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
                            if(!client.isConnected() || client.isClosed())
                            {
                                System.out.println("disconnected");
                                closedSockets.add(client);
                                continue;
                            }
                            
                            if(client.getInputStream().available() != 0)
                            {
                                /*InputStreamReader inputStreamReader = new InputStreamReader(client.getInputStream());
                                char[] target = new char[255];
                                StringBuilder stringBuilder = new StringBuilder();
                                
                                while(client.getInputStream().available() != 0)
                                {
                                    inputStreamReader.read(target);
                                    String line = new String(target);
                                    stringBuilder.append(line);
                                }
                                System.out.println(stringBuilder.toString());
                                
                                CallObserversMessageReceived(client, stringBuilder.toString());*/
                                byte[] buff = new byte[clientBufferSize];
                                client.getInputStream().read(buff);
                                
                                Object object;
                                try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buff)) {
                                    object = SerializationUtils.DeserializeObject(byteArrayInputStream);
                                }
                                catch(StreamCorruptedException ex)
                                {
                                    object = new String(buff);
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
    private int maximumClients;
    private final Set<TCPServerObserver> observers = new HashSet<>();
    private final InputReader inputReader = new InputReader();
    private int clientBufferSize;
    
    public TCPServer(ServerSocket serverSocket, int clientBufferSize, int maximumClients) 
    {
        this.serverSocket = serverSocket;
        this.clientBufferSize = clientBufferSize;
        this.thread = new Thread(this);
        this.maximumClients = maximumClients;
        
    }
    
    
    @Override
    public void run() {
        while(!stopListening)
        {
            try
            {
                Socket client = serverSocket.accept();
                if(clients.size() >= this.maximumClients)
                {
                    client.close();
                }
                else
                {
                    System.out.println("Client connected");
                    synchronized(this.clients)
                    {
                        clients.add(client);
                    }
                    CallObserversConnection(client);
                }
                
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
