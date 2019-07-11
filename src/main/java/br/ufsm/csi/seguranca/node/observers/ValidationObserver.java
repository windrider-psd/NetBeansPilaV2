/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.observers;

import br.ufsm.csi.seguranca.node.NodeJSListener;
import br.ufsm.csi.seguranca.node.OperationType;
import br.ufsm.csi.seguranca.node.models.SerializablePilaCoin;
import br.ufsm.csi.seguranca.pila.mining.PilaCoinCreator;
import br.ufsm.csi.seguranca.pila.mining.PilaCoinObserver;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidatorManagerObserver;

/**
 *
 * @author politecnico
 */
public class ValidationObserver implements PilaCoinValidatorManagerObserver
{

    private NodeJSListener nodeJSListener;

    public ValidationObserver(NodeJSListener nodeJSListener)
    {
        this.nodeJSListener = nodeJSListener;
    }

    
    
    @Override
    public void OnFinishedValidation(PilaCoin pilaCoin)
    {
        SerializablePilaCoin serializablePilaCoin = SerializablePilaCoin.FromPilaCoin(pilaCoin);
        this.nodeJSListener.WriteCommand("pilacoin/finished-validation", OperationType.WRITE, serializablePilaCoin);
    }

}
