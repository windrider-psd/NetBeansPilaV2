package br.ufsm.csi.seguranca.pila.validation;

import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidator;

public interface PilaCoinValidatorObserver
{

    void OnPilaCoinValidatorReady(PilaCoinValidator pilaCoinValidator);
}
