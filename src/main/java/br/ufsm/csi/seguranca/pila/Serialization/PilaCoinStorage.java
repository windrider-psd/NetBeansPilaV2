package br.ufsm.csi.seguranca.pila.Serialization;

import br.ufsm.csi.seguranca.pila.model.PilaCoin;

public interface PilaCoinStorage {
    boolean Add(PilaCoin pilaCoin);
    boolean Remove(PilaCoin pilaCoin);
    boolean Update(PilaCoin pilaCoin);
    int Remove(PilaCoin[] pilaCoins);
    PilaCoin Get(Long id);
    boolean Contains(PilaCoin pilaCoin);
    PilaCoin[] Get(Long[] ids);
    PilaCoin[] GetAll();
    int Clear();
    boolean Save();
}
