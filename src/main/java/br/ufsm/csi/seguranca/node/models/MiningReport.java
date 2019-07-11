/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.models;

/**
 *
 * @author politecnico
 */
public class MiningReport
{

    private boolean turnedOn;
    private int threads;
    private Long numeroMagico;
    private int scheduledPilaCoins;
    private int underValidation;

    public MiningReport(boolean turnedOn, int threads, Long numeroMagico, int scheduledPilaCoins, int underValidation)
    {
        this.turnedOn = turnedOn;
        this.threads = threads;
        this.numeroMagico = numeroMagico;
        this.scheduledPilaCoins = scheduledPilaCoins;
        this.underValidation = underValidation;
    }

    public MiningReport()
    {
    }

    
    
    
    public int getUnderValidation()
    {
        return underValidation;
    }

    public void setUnderValidation(int underValidation)
    {
        this.underValidation = underValidation;
    }
   

    public int getScheduledPilaCoins()
    {
        return scheduledPilaCoins;
    }

    public void setScheduledPilaCoins(int scheduledPilaCoins)
    {
        this.scheduledPilaCoins = scheduledPilaCoins;
    }
    
    

    public boolean isTurnedOn()
    {
        return turnedOn;
    }

    public void setTurnedOn(boolean turnedOn)
    {
        this.turnedOn = turnedOn;
    }

    public int getThreads()
    {
        return threads;
    }

    public void setThreads(int threads)
    {
        this.threads = threads;
    }

    public Long getNumeroMagico()
    {
        return numeroMagico;
    }

    public void setNumeroMagico(Long numeroMagico)
    {
        this.numeroMagico = numeroMagico;
    }

}
