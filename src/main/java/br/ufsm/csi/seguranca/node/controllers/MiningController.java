/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.controllers;

import br.ufsm.csi.seguranca.node.NodeJSController;
import br.ufsm.csi.seguranca.node.NodeJSControllerRoute;
import br.ufsm.csi.seguranca.node.OperationType;
import br.ufsm.csi.seguranca.node.models.MiningReport;
import br.ufsm.csi.seguranca.pila.mining.MiningManager;
import br.ufsm.csi.seguranca.pila.mining.PilaCoinCreator;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidatorManager;

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
        try
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
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

    }

    @NodeJSControllerRoute(CommandPath = "mining/control", OperationType = OperationType.READ)
    public MiningReport GetMiningState()
    {
        MiningReport miningReport = new MiningReport();
        miningReport.setNumeroMagico(PilaCoinCreator.magicalNumber);
        miningReport.setThreads(MiningManager.getInstance().getPilaCoinCreators().size());
        miningReport.setTurnedOn(MiningManager.getInstance().isMining());
        miningReport.setScheduledPilaCoins(PilaCoinValidatorManager.getInstance().getScheduledPilaCoins().size());
        miningReport.setUnderValidation(PilaCoinValidatorManager.getInstance().getPilaCoinValidators().size());
        return miningReport;
    }
}
