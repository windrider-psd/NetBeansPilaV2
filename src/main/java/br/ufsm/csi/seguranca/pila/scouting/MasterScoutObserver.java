package br.ufsm.csi.seguranca.pila.scouting;

import java.net.InetAddress;

public interface MasterScoutObserver {
    void OnMasterFound(InetAddress inetAddress, int port);
    void OnMasterError();
}
