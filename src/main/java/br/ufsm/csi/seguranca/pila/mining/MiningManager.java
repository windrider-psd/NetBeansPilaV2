/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.mining;

import br.ufsm.csi.seguranca.Main;
import br.ufsm.csi.seguranca.pila.model.PersonalCertificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author politecnico
 */
public class MiningManager
{
    private static MiningManager instance;
    private Set<PilaCoinCreator> pilaCoinCreators = new HashSet<>();
    private boolean mining = false;
    
    private MiningManager()
    {
    }
    
    public void SetUpCreators(int total)
    {
        synchronized(this)
        {
            if(total < pilaCoinCreators.size())
            {
                int i = 0;
                int totalEliminated = pilaCoinCreators.size() - total;
                ArrayList<PilaCoinCreator> eliminated = new ArrayList<>();
                eliminated.ensureCapacity(totalEliminated);
                for(PilaCoinCreator pcc : pilaCoinCreators)
                {
                    if(i <= totalEliminated)
                    {
                        eliminated.add(pcc);
                        pcc.Stop();
                        i++;
                    }
                    else
                    {
                        break;
                    }
                }
                
                this.pilaCoinCreators.removeAll(eliminated);
            }
            else if(total > pilaCoinCreators.size())
            {
                int totalAdded = total - pilaCoinCreators.size();
                for(int i = 0; i < totalAdded; i++)
                {
                    PilaCoinCreator pilaCoinCreator = new PilaCoinCreator(Main.id, PersonalCertificate.getInstance().getPublicKey());
                    
                    this.pilaCoinCreators.add(pilaCoinCreator);
                    if(mining)
                    {
                        pilaCoinCreator.Start();
                    }
                }
            }
        }
    }
    
    
    public synchronized static MiningManager getInstance()
    {
        if(instance == null)
        {
            instance = new MiningManager();
        }
        return instance;
    }
    
    public void StartMining()
    {
        synchronized(this)
        {
            this.pilaCoinCreators.forEach((pcc) ->
            {
                pcc.Start();
            });
            mining = true;
        }
    }
    
    public void StopMining()
    {
        synchronized(this)
        {
            this.pilaCoinCreators.forEach((pcc) ->
            {
                pcc.Stop();
            });
            mining = false;
        }
    }

    public boolean isMining()
    {
        return mining;
    }

    public Set<PilaCoinCreator> getPilaCoinCreators()
    {
        return pilaCoinCreators;
    }

   
    
    
}
