/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.controllers;

import br.ufsm.csi.seguranca.Main;
import br.ufsm.csi.seguranca.node.NodeJSController;
import br.ufsm.csi.seguranca.node.NodeJSControllerRoute;
import br.ufsm.csi.seguranca.node.OperationType;
import br.ufsm.csi.seguranca.node.models.SerializablePilaCoin;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.network.PilaDHTClientManager;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidator;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidatorManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 *
 * @author politecnico
 */
@NodeJSController
public class PilaCoinController
{

    @NodeJSControllerRoute(CommandPath = "pilacoin/storage", OperationType = OperationType.READ)
    public List<SerializablePilaCoin> getStorage() throws IOException, ClassNotFoundException
    {
        List<SerializablePilaCoin> serializablePilaCoins = new ArrayList<>();
        
       // List<PilaCoin> pilaCoins = Arrays.asList(Main.pilaCoinStorage.GetAll());
        Set<PilaCoin> pilaCoins = PilaDHTClientManager.getInstance().getClient().getUsuario(Main.id).getMeusPilas();
        
        pilaCoins.forEach(pilaCoin -> {
            serializablePilaCoins.add(SerializablePilaCoin.FromPilaCoin(pilaCoin));
        });
        return serializablePilaCoins;
       
    }
    
    @NodeJSControllerRoute(CommandPath = "pilacoin/schedule", OperationType = OperationType.READ)
    public List<SerializablePilaCoin> getScheduled()
    {
        List<SerializablePilaCoin> serializablePilaCoins = new ArrayList<>();
        Set<PilaCoin> scheduled = PilaCoinValidatorManager.getInstance().getScheduledPilaCoins();
        
        scheduled.forEach(pilaCoin -> {
            serializablePilaCoins.add(SerializablePilaCoin.FromPilaCoin(pilaCoin));
        });
        return serializablePilaCoins;
    }
    @NodeJSControllerRoute(CommandPath = "pilacoin/validation", OperationType = OperationType.READ)
    public List<SerializablePilaCoin> getUnderValidation()
    {
        List<SerializablePilaCoin> serializablePilaCoins = new ArrayList<>();
        
        Set<PilaCoinValidator> validators = PilaCoinValidatorManager.getInstance().getPilaCoinValidators();
        validators.forEach(validator -> {
            serializablePilaCoins.add(SerializablePilaCoin.FromPilaCoin(validator.getPilaCoin()));
        });
        
        return serializablePilaCoins;
    }

}
