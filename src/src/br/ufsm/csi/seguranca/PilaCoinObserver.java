package br.ufsm.csi.seguranca;

import br.ufsm.csi.seguranca.pila.model.PilaCoin;

public interface PilaCoinObserver {

    void OnCreatedPilaCoin(PilaCoinCreator pilaCoinCreator, PilaCoin pilaCoin);
}
