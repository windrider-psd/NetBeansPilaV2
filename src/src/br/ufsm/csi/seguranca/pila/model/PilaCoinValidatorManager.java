package br.ufsm.csi.seguranca.pila.model;

import br.ufsm.csi.seguranca.MasterScoutObserver;
import br.ufsm.csi.seguranca.PilaCoinCreator;
import br.ufsm.csi.seguranca.PilaCoinObserver;

import java.net.InetAddress;
import java.util.*;

public class PilaCoinValidatorManager implements PilaCoinObserver, PilaCoinValidatorObserver {

    private Set<PilaCoinValidatorManagerObserver> observers = new HashSet<>();





    private static PilaCoinValidatorManager instance;
    private PilaCoinValidatorManager(){

    }

    public void StartValidation()
    {
        PilaCoinCreator.AddCreationObserver(this);
    }

    public  void StopValidation()
    {
        PilaCoinCreator.RemoveCreationObserver(this);
    }

    public static synchronized PilaCoinValidatorManager getInstance()
    {
        if(instance == null)
        {
            instance = new PilaCoinValidatorManager();
        }
        return instance;
    }

    public void AddObserver(PilaCoinValidatorManagerObserver observer)
    {
        this.observers.add(observer);
        PilaCoinValidator.AddObserver(this);
    }

    public void RemoveObserver(PilaCoinValidatorManagerObserver observer)
    {
        this.observers.remove(observer);
        PilaCoinValidator.RemoveObserver(this);
    }

    private void CallManagerObservers(PilaCoin pilaCoin)
    {
        for(PilaCoinValidatorManagerObserver pilaCoinValidatorManagerObserver : this.observers)
        {
            pilaCoinValidatorManagerObserver.OnFinishedValidation(pilaCoin);
        }
    }

    private Set<PilaCoinValidator> pilaCoinValidators = new HashSet<>();
    static boolean create = true;
    @Override
    public void OnCreatedPilaCoin(PilaCoinCreator pilaCoinCreator, PilaCoin pilaCoin) {

        if(!create)
        {
           // return;
        }
        PilaCoinValidator pilaCoinValidator = new PilaCoinValidator();
        pilaCoinValidator.setPilaCoin(pilaCoin);
        Thread thread = new Thread(pilaCoinValidator);


        thread.start();
        create = false;
        //System.out.println("Found: " + pilaCoin.getNumeroMagico());
    }

    @Override
    public void OnPilaCoinValidatorReady(PilaCoinValidator pilaCoinValidator) {
        System.out.println("Valid: " + pilaCoinValidator.getPilaCoin().getNumeroMagico());
        CallManagerObservers(pilaCoinValidator.getPilaCoin());
    }


}
