package br.ufsm.csi.seguranca.pila.scouting;

import br.ufsm.csi.seguranca.pila.model.User;

public interface UserScoutObserver
{

    void OnUserFound(User user);
}
