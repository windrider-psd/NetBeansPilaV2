/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author politecnico
 */
public class UDPBroadcaster implements Runnable
{

    private DatagramSocket datagramSocket;
    private Object defaultBroadcastMessage;
    private InetAddress broadcastAddress;
    private int broadcastPort;
    private int interval;

    private byte[] serializedBroadcastMessage = new byte[1];
    private Thread thread;
    private boolean stop = true;

    private Set<UDPBroadcasterObserver> observers = new HashSet<>();

    public UDPBroadcaster(DatagramSocket datagramSocket, Object broadcastMessage, InetAddress broadcastAddress, int broadcastPort, int interval) throws SocketException
    {
        this.defaultBroadcastMessage = broadcastMessage;
        this.broadcastAddress = broadcastAddress;
        this.broadcastPort = broadcastPort;
        this.interval = interval;

        this.datagramSocket = datagramSocket;
        this.datagramSocket.setBroadcast(true);
        setSerializedBroadcastMessage();
        thread = new Thread(this);
    }

    public synchronized void Start()
    {
        if (!thread.isAlive())
        {
            thread.start();
        }

        stop = false;
    }

    public synchronized void Stop()
    {
        stop = true;
    }

    @Override
    public void run()
    {
        while (!stop)
        {
            try
            {
                synchronized (this.serializedBroadcastMessage)
                {
                    BroadcastSingle(serializedBroadcastMessage, true);
                }

                Thread.sleep(interval);
            }
            catch (InterruptedException ex)
            {
                ex.printStackTrace();
            }
            catch (Exception ex)
            {
                ex.printStackTrace();
                break;
            }
        }
    }

    public void BroadcastSingle(byte[] buffer, boolean callObservers) throws IOException
    {
        DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, broadcastAddress, broadcastPort);
        datagramSocket.send(datagramPacket);
        CallObservers(datagramPacket);
    }

    public void BroadcastSingle(Object object, boolean callObservers) throws IOException
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        SerializationUtils.SerializeObject(object, byteArrayOutputStream);
        byte[] buffer = byteArrayOutputStream.toByteArray();
        byteArrayOutputStream.close();
        BroadcastSingle(buffer, callObservers);
    }

    public int getInterval()
    {
        return interval;
    }

    public void setInterval(int interval)
    {
        this.interval = interval;
    }

    public DatagramSocket getDatagramSocket()
    {
        return datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket)
    {
        this.datagramSocket = datagramSocket;
    }

    private void setSerializedBroadcastMessage()
    {
        synchronized (this.serializedBroadcastMessage)
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try
            {
                SerializationUtils.SerializeObject(defaultBroadcastMessage, byteArrayOutputStream);
                serializedBroadcastMessage = byteArrayOutputStream.toByteArray();
                byteArrayOutputStream.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }

    public Object getDefaultBroadcastMessage()
    {
        return defaultBroadcastMessage;
    }

    public void setDefaultBroadcastMessage(Object defaultBroadcastMessage)
    {
        this.defaultBroadcastMessage = defaultBroadcastMessage;
    }

    public InetAddress getBroadcastAddress()
    {
        return broadcastAddress;
    }

    public void setBroadcastAddress(InetAddress broadcastAddress)
    {
        this.broadcastAddress = broadcastAddress;
    }

    public int getBroadcastPort()
    {
        return broadcastPort;
    }

    public void setBroadcastPort(int broadcastPort)
    {
        this.broadcastPort = broadcastPort;
    }

    public void AddObserver(UDPBroadcasterObserver observer)
    {
        observers.add(observer);
    }

    public void RemoveObserver(UDPBroadcasterObserver observer)
    {
        observers.remove(observer);
    }

    private void CallObservers(DatagramPacket datagramPacket)
    {
        this.observers.forEach((observer) ->
        {
            try
            {
                observer.OnMessageSent(datagramPacket);
            }
            catch (Exception ex)
            {

            }
        });
    }

}
