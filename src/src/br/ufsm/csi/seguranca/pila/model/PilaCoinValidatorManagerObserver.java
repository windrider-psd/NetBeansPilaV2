package br.ufsm.csi.seguranca.pila.model;

public interface PilaCoinValidatorManagerObserver {
    void OnFinishedValidation(PilaCoin pilaCoin);
}
