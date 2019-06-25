/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

import java.net.Socket;

/**
 *
 * @author politecnico
 */
public interface TCPServerObserver
{

    void OnMessageReceived(Socket socket, Object obj);

    void OnMessageSent(Socket socket, Object obj);

    void OnConnection(Socket client);
}
