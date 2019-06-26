/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.models;

import br.ufsm.csi.seguranca.pila.model.PilaCoin;
import br.ufsm.csi.seguranca.pila.model.Transacao;
import java.security.PublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.List;

/**
 *
 * @author politecnico
 */
public class SerializablePilaCoin
{
    private long serialVersionUID;

    private String idCriador;
    private Date dataCriacao;
    private String chaveCriador;
    private Long numeroMagico;
    private byte[] assinaturaMaster;
    private Long id;
    private List<Transacao> transacoes;
    
    public static SerializablePilaCoin FromPilaCoin(PilaCoin pilaCoin)
    {
        SerializablePilaCoin serializablePilaCoin = new SerializablePilaCoin();
        serializablePilaCoin.setAssinaturaMaster(pilaCoin.getAssinaturaMaster());
        serializablePilaCoin.setChaveCriador(Base64.getEncoder().encodeToString(pilaCoin.getChaveCriador().getEncoded()));
        serializablePilaCoin.setDataCriacao(pilaCoin.getDataCriacao());
        serializablePilaCoin.setId(pilaCoin.getId());
        serializablePilaCoin.setIdCriador(pilaCoin.getIdCriador());
        serializablePilaCoin.setNumeroMagico(pilaCoin.getNumeroMagico());
        serializablePilaCoin.setTransacoes(pilaCoin.getTransacoes());
        return serializablePilaCoin;
    }
    
    
    public List<Transacao> getTransacoes() {
        return transacoes;
    }

    public void setTransacoes(List<Transacao> transacoes) {
        this.transacoes = transacoes;
    }

    public String getIdCriador() {
        return idCriador;
    }

    public void setIdCriador(String idCriador) {
        this.idCriador = idCriador;
    }

    public Date getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(Date dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getChaveCriador() {
        return chaveCriador;
    }

    public void setChaveCriador(String chaveCriador) {
        this.chaveCriador = chaveCriador;
    }

    public Long getNumeroMagico() {
        return numeroMagico;
    }

    public void setNumeroMagico(Long numeroMagico) {
        this.numeroMagico = numeroMagico;
    }

    public byte[] getAssinaturaMaster() {
        return assinaturaMaster;
    }

    public void setAssinaturaMaster(byte[] assinaturaMaster) {
        this.assinaturaMaster = assinaturaMaster;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Object clone() {
        Object clone = null;

        try {
            clone = super.clone();

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return clone;
    }
}
