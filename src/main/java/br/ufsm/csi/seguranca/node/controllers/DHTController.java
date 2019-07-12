/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.controllers;

import br.ufsm.csi.seguranca.node.NodeJSController;
import br.ufsm.csi.seguranca.node.NodeJSControllerRoute;
import br.ufsm.csi.seguranca.node.OperationType;
import br.ufsm.csi.seguranca.node.models.SellInfo;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.network.PilaDHTClientManager;
import br.ufsm.csi.seguranca.pilacoin.PilaDHTClient;
import java.util.List;

/**
 *
 * @author Christian Lemos
 */
@NodeJSController
public class DHTController
{
    
    @NodeJSControllerRoute(CommandPath = "dht/sell", OperationType = OperationType.WRITE)
    public void SellPilaCoin(SellInfo sellInfo) throws Exception
    {
        if(PilaDHTClientManager.getInstance().getClient() != null)
        {
            PilaDHTClientManager.getInstance().SellPilaCoin(sellInfo.getPilaCoinId(), sellInfo.getTargetId());
        }
        else
        {
            throw new Exception("DHT Client is not ready.");
        }
    }
}
