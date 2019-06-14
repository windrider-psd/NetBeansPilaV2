package br.ufsm.csi.seguranca.pila.model;

public interface PilaCoinStorage {
    boolean Add(PilaCoin pilaCoin);
    boolean Remove(PilaCoin pilaCoin);
    boolean Update(PilaCoin pilaCoin);
    int Remove(PilaCoin[] pilaCoins);
    PilaCoin Get(Long id);
    boolean IsAtDatabase(PilaCoin pilaCoin);
    PilaCoin[] Get(Long[] ids);
    PilaCoin[] GetAll();
    int Clear();
    boolean Save();
}
