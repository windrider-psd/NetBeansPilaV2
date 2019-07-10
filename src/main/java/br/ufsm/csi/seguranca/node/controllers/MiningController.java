/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.controllers;

import br.ufsm.csi.seguranca.node.NodeJSController;
import br.ufsm.csi.seguranca.node.NodeJSControllerRoute;
import br.ufsm.csi.seguranca.node.OperationType;
import br.ufsm.csi.seguranca.pila.mining.MiningManager;

/**
 *
 * @author politecnico
 */
@NodeJSController
public class MiningController
{

    @NodeJSControllerRoute(CommandPath = "mining/control", OperationType = OperationType.WRITE)
    public void ControlMining(Boolean value)
    {
        if (value)
        {
            MiningManager.getInstance().StartMining();
        }
        else
        {
            MiningManager.getInstance().StopMining();
        }

    }

    @NodeJSControllerRoute(CommandPath = "mining/control", OperationType = OperationType.READ)
    public boolean GetMiningState()
    {
        return MiningManager.getInstance().isMining();
    }
}
