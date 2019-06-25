package br.ufsm.csi.seguranca.pila.Serialization;

import br.ufsm.csi.seguranca.pila.Serialization.PilaCoinStorage;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidatorManager;
import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidatorManagerObserver;

public class PilaCoinStorageSaver implements PilaCoinValidatorManagerObserver
{

    private PilaCoinStorage pilaCoinStorage;

    public PilaCoinStorageSaver(PilaCoinStorage pilaCoinStorage)
    {
        this.pilaCoinStorage = pilaCoinStorage;

        PilaCoinValidatorManager.getInstance().AddObserver(this);
    }

    @Override
    public void OnFinishedValidation(PilaCoin pilaCoin)
    {
        this.pilaCoinStorage.Add(pilaCoin);
    }

}
