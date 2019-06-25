package br.ufsm.csi.seguranca.pila.mining;

import br.ufsm.csi.seguranca.pila.mining.PilaCoinCreator;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;

public interface PilaCoinObserver {

    void OnCreatedPilaCoin(PilaCoinCreator pilaCoinCreator, PilaCoin pilaCoin);
}
