package br.ufsm.csi.seguranca.pila.validation;

import br.ufsm.csi.seguranca.pila.model.PilaCoin;

public interface PilaCoinValidatorManagerObserver
{

    void OnFinishedValidation(PilaCoin pilaCoin);
}
