/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.observers;

import br.ufsm.csi.seguranca.node.NodeJSListener;
import br.ufsm.csi.seguranca.node.OperationType;
import br.ufsm.csi.seguranca.node.models.SerializablePilaCoin;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.network.PilaDHTClientManagerObserver;

/**
 *
 * @author Christian Lemos
 */
public class DHTObserver implements PilaDHTClientManagerObserver
{
    private final NodeJSListener nodeJSListener;

    public DHTObserver(NodeJSListener nodeJSListener)
    {
        this.nodeJSListener = nodeJSListener;
    }
    
    
    @Override
    public void OnSoldPilaCoin(PilaCoin pilaCoin)
    {
        SerializablePilaCoin serializablePilaCoin = SerializablePilaCoin.FromPilaCoin(pilaCoin);
        this.nodeJSListener.WriteCommand("pilacoin/sold", OperationType.WRITE, serializablePilaCoin);
    }
    
}
