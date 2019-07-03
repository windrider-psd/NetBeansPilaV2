/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

/**
 *
 * @author politecnico
 */
public interface TCPClientObserver
{
    void OnMessage(TCPClient client, Object message);
    void OnClosed(TCPClient client);
}
