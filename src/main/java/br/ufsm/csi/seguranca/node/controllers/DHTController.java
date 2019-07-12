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
import br.ufsm.csi.seguranca.node.models.SerializablePilaCoin;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.model.Usuario;
import br.ufsm.csi.seguranca.pila.network.PilaDHTClientManager;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
        if (PilaDHTClientManager.getInstance().getClient() != null)
        {
            PilaDHTClientManager.getInstance().SellPilaCoin(sellInfo.getPilaCoinId(), sellInfo.getTargetId());
        }
        else
        {
            throw new Exception("DHT Client is not ready.");
        }
    }

    @NodeJSControllerRoute(CommandPath = "dht/user-wallet", OperationType = OperationType.READ)
    public List<SerializablePilaCoin> GetWallet(String userId) throws Exception
    {
        if (PilaDHTClientManager.getInstance().getClient() != null)
        {
            Usuario usuario = PilaDHTClientManager.getInstance().getClient().getUsuario(userId);
            if (usuario != null)
            {
                List<SerializablePilaCoin> pilaCoins = new ArrayList<>();
                Set<PilaCoin> meusPilas = usuario.getMeusPilas();
                meusPilas.forEach((pilaCoin) ->
                {
                    pilaCoins.add(SerializablePilaCoin.FromPilaCoin(pilaCoin));
                });
                return pilaCoins;
            }
            else
            {
                throw new Exception("User not found!");
            }

        }
        else
        {
            throw new Exception("DHT Client is not ready.");
        }

    }
}
