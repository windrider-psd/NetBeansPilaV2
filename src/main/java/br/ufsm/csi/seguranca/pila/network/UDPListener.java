/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author politecnico
 */
public class UDPListener implements Runnable{
    private DatagramSocket datagramSocket;
    private int bufferSize;
    
    private Thread thread;
    private boolean stop = false;
    
    private Set<UDPListenerObserver> observers = new HashSet<>();
    
    public UDPListener(DatagramSocket datagramSocket, int bufferSize) {
        this.datagramSocket = datagramSocket;
        this.bufferSize = bufferSize;
        this.thread = new Thread(this);
    }

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void setDatagramSocket(DatagramSocket datagramSocket) {
        this.datagramSocket = datagramSocket;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }
    
   
    
    
    public void AddObserver(UDPListenerObserver observer){
        this.observers.add(observer);
    }
    
    
    public void RemoveObserver(UDPListenerObserver observer){
        this.observers.remove(observer);
    }
    
    private void CallObservers(DatagramPacket datagramPacket)
    {
        this.observers.forEach((observer) -> {
            try{
                observer.OnPacket(datagramPacket);
            }
            catch(Exception ex)
            {
                
            }
        });
    }
    
    public void Start()
    {
        synchronized(this)
        {
            
            if(!this.thread.isAlive())
            {
                thread = new Thread(this);
                thread.start();
            }
            stop = false;
            
        }
        
    }
    public void Stop(){
        synchronized(this)
        {
            stop = true;
        }
        
    }

    @Override
    public void run() {
        while(!stop)
        {
            try
            {
                byte[] buffer = new byte[bufferSize];
            
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                datagramSocket.receive(incomingPacket);            
                
                CallObservers(incomingPacket);
            }
            catch(Exception ex)
            {
                this.Stop();
            }
        }
    }
}
