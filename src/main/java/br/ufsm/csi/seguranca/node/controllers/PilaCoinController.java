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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

/**
 *
 * @author politecnico
 */
@NodeJSController
public class PilaCoinController
{

    @NodeJSControllerRoute(CommandPath = "pilacoin", OperationType = OperationType.READ)
    public List<SerializablePilaCoin> getPilaCoins()
    {
        List<PilaCoin> pilaCoins = Arrays.asList(Main.pilaCoinStorage.GetAll());

        List<SerializablePilaCoin> serializablePilaCoins = new ArrayList<>();

        pilaCoins.stream().map((pilaCoin) ->
        {
            SerializablePilaCoin serializablePilaCoin = new SerializablePilaCoin();
            serializablePilaCoin.setAssinaturaMaster(pilaCoin.getAssinaturaMaster());
            serializablePilaCoin.setChaveCriador(Base64.getEncoder().encodeToString(pilaCoin.getChaveCriador().getEncoded()));
            serializablePilaCoin.setDataCriacao(pilaCoin.getDataCriacao());
            serializablePilaCoin.setId(pilaCoin.getId());
            serializablePilaCoin.setIdCriador(pilaCoin.getIdCriador());
            serializablePilaCoin.setNumeroMagico(pilaCoin.getNumeroMagico());
            serializablePilaCoin.setTransacoes(pilaCoin.getTransacoes());
            return serializablePilaCoin;
        }).forEachOrdered((serializablePilaCoin) ->
        {
            serializablePilaCoins.add(serializablePilaCoin);
        });

        return serializablePilaCoins;
    }

}
