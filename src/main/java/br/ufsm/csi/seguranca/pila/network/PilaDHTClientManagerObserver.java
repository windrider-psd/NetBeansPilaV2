/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.network;

import br.ufsm.csi.seguranca.pila.model.PilaCoin;

/**
 *
 * @author politecnico
 */
public interface PilaDHTClientManagerObserver
{
    void OnSoldPilaCoin(PilaCoin pilaCoin);
}
