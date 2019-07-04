/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author politecnico
 */
public class TCPClient implements Runnable
{
    private Socket socket;
    private boolean stopListening = true;
    private Thread thread;
    
    private final Set<TCPClientObserver> observers = new HashSet<>();
    
    
    private int clientBufferSize;
    
    public TCPClient(Socket socket, int clientBufferSize) 
    {
        this.clientBufferSize = clientBufferSize;
        this.thread = new Thread(this);
        this.socket = socket;
    }
    
    
    @Override
    public void run() {
        while(!stopListening)
        {
            try
            {
                boolean close = false;                
                synchronized(this.socket)
                {
                    try
                    {
                         this.socket.getOutputStream().write("ping".getBytes());
                    }
                    catch(Exception ex)
                    {
                       close = true;
                    }
                          
                    if(!close && this.socket.getInputStream().available() != 0)
                    {
                        byte[] buff = new byte[clientBufferSize];
                        this.socket.getInputStream().read(buff);

                        Object object;
                        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(buff)) {
                            object = SerializationUtils.DeserializeObject(byteArrayInputStream);
                        }
                        catch(StreamCorruptedException  | ClassNotFoundException ex )
                        {
                            object = new String(buff);
                        }
      
                        CallOnMessage(object);
                    }
                }
            
                if(close)
                {
                    StopListening();
                }
                else
                {
                    Thread.sleep(500);
                }
                
            }
            catch (InterruptedException | IOException ex)
            {
                Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    
   
    public void StartListening()
    {
        stopListening = false;
        if(!thread.isAlive())
        {
            thread = new Thread(this);
            thread.start();
        }
        
    }
    
    public void StopListening()
    {
        synchronized(this.socket)
        {
            try
            {
                this.socket.close();
            }
            catch (IOException ex)
            {
                Logger.getLogger(TCPClient.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        stopListening = true;
        CallOnClosed(this);
    }
    
    public void AddObserver(TCPClientObserver tCPClientObserver)
    {
        this.observers.add(tCPClientObserver);
    }
    
    public void RemoveObserver(TCPClientObserver tCPClientObserver)
    {
        this.observers.remove(tCPClientObserver);
    }
    
    private void CallOnMessage(Object message)
    {
        observers.forEach((o) ->
        {
            o.OnMessage(this, message);
        });
    }
    private void CallOnClosed(Object message)
    {
        observers.forEach((o) ->
        {
            o.OnClosed(this);
        });
    }

    public Socket getSocket()
    {
        return socket;
    }

    public void setSocket(Socket socket)
    {
        this.socket = socket;
    }

    public boolean isStopListening()
    {
        return stopListening;
    }

    public void setStopListening(boolean stopListening)
    {
        this.stopListening = stopListening;
    }

    public Thread getThread()
    {
        return thread;
    }

    public void setThread(Thread thread)
    {
        this.thread = thread;
    }

    public int getClientBufferSize()
    {
        return clientBufferSize;
    }

    public void setClientBufferSize(int clientBufferSize)
    {
        this.clientBufferSize = clientBufferSize;
    }
    
    
    
}
