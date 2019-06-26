package br.ufsm.csi.seguranca.pila.validation;

import br.ufsm.csi.seguranca.pila.validation.PilaCoinValidator;
import br.ufsm.csi.seguranca.pila.scouting.MasterScoutObserver;
import br.ufsm.csi.seguranca.pila.mining.PilaCoinCreator;
import br.ufsm.csi.seguranca.pila.mining.PilaCoinObserver;
import br.ufsm.csi.seguranca.pila.model.PilaCoin;

import java.net.InetAddress;
import java.util.*;

public class PilaCoinValidatorManager implements PilaCoinObserver, PilaCoinValidatorObserver, MasterScoutObserver {

    private Set<PilaCoinValidatorManagerObserver> observers = new HashSet<>();
    private static PilaCoinValidatorManager instance;
    
    private Set<PilaCoinValidator> pilaCoinValidators = new HashSet<>();
    private Set<PilaCoin> scheduledPilaCoins = new HashSet<>();
    private boolean ready = false;
    
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

    
    @Override
    public void OnCreatedPilaCoin(PilaCoinCreator pilaCoinCreator, PilaCoin pilaCoin) {

        
        if(this.ready)
        {
            ValidatePilaCoin(pilaCoin);    
        }
        else
        {
            scheduledPilaCoins.add(pilaCoin);
        }
        
    }

    @Override
    public void OnPilaCoinValidatorReady(PilaCoinValidator pilaCoinValidator) {
        System.out.println("Valid: " + pilaCoinValidator.getPilaCoin().getNumeroMagico());
        CallManagerObservers(pilaCoinValidator.getPilaCoin());
    }

    @Override
    public void OnMasterFound(InetAddress inetAddress, int port) {
       setReady(true);
    }

    @Override
    public void OnMasterError() {
        setReady(false);
    }
    
    private void setReady(boolean value)
    {
        this.ready = value;
        
        if(value == true)
        {
            CallScheduledPilaCoins();
        }
    }
    
    private void CallScheduledPilaCoins()
    {
        for(PilaCoin p : this.scheduledPilaCoins)
        {
            ValidatePilaCoin(p);
        }
    }
    
    private void ValidatePilaCoin(PilaCoin pilaCoin)
    {
        this.scheduledPilaCoins.remove(pilaCoin);
        
        PilaCoinValidator pilaCoinValidator = new PilaCoinValidator();
        pilaCoinValidator.setPilaCoin(pilaCoin);
        Thread thread = new Thread(pilaCoinValidator);
        thread.start();
    }

    public Set<PilaCoinValidator> getPilaCoinValidators()
    {
        return pilaCoinValidators;
    }

    public void setPilaCoinValidators(Set<PilaCoinValidator> pilaCoinValidators)
    {
        this.pilaCoinValidators = pilaCoinValidators;
    }

    public Set<PilaCoin> getScheduledPilaCoins()
    {
        return scheduledPilaCoins;
    }

    public void setScheduledPilaCoins(Set<PilaCoin> scheduledPilaCoins)
    {
        this.scheduledPilaCoins = scheduledPilaCoins;
    }
    
    

}
