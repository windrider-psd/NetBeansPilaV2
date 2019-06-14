package br.ufsm.csi.seguranca;

import java.net.InetAddress;

public interface MasterScoutObserver {
    void OnReady(InetAddress inetAddress, int port);
    void OnError();
}
