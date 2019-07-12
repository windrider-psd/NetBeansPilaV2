/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.models;

/**
 *
 * @author Christian Lemos
 */
public class SellInfo
{
    private Long pilaCoinId;
    private String targetId;

    public SellInfo()
    {
    }

    
    
    public SellInfo(Long pilaCoinId, String targetId)
    {
        this.pilaCoinId = pilaCoinId;
        this.targetId = targetId;
    }

    public Long getPilaCoinId()
    {
        return pilaCoinId;
    }

    public void setPilaCoinId(Long pilaCoinId)
    {
        this.pilaCoinId = pilaCoinId;
    }

    public String getTargetId()
    {
        return targetId;
    }

    public void setTargetId(String targetId)
    {
        this.targetId = targetId;
    }
    
    }
