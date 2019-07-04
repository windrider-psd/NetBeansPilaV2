/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node;

/**
 *
 * @author politecnico
 */
public class NodeJSMessage
{
    private MessageType messageType;
    private String arg;

    public NodeJSMessage()
    {
    }

    public NodeJSMessage(MessageType messageType, String arg)
    {
        this.messageType = messageType;
        this.arg = arg;
    }

    public MessageType getMessageType()
    {
        return messageType;
    }

    public void setMessageType(MessageType messageType)
    {
        this.messageType = messageType;
    }

    public String getArg()
    {
        return arg;
    }

    public void setArg(String arg)
    {
        this.arg = arg;
    }

    
    
   
    
    
    
}
