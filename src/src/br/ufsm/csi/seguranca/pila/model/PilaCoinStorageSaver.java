package br.ufsm.csi.seguranca.pila.model;

public class PilaCoinStorageSaver implements PilaCoinValidatorManagerObserver {


    private PilaCoinStorage pilaCoinStorage;

    public PilaCoinStorageSaver(PilaCoinStorage pilaCoinStorage) {
        this.pilaCoinStorage = pilaCoinStorage;

        PilaCoinValidatorManager.getInstance().AddObserver(this);
    }

    @Override
    public void OnFinishedValidation(PilaCoin pilaCoin) {
        this.pilaCoinStorage.Add(pilaCoin);
    }


}
