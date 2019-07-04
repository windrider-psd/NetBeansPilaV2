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
class NodeJSCommand
{
    private int commandId;
    private String commandPath;
    private OperationType operationType;
    private String arg;

    public NodeJSCommand()
    {
    }

    public NodeJSCommand(int commandId, String commandPath, OperationType operationType, String arg)
    {
        this.commandId = commandId;
        this.commandPath = commandPath;
        this.operationType = operationType;
        this.arg = arg;
    }

    
    public int getCommandId()
    {
        return commandId;
    }

    public void setCommandId(int commandId)
    {
        this.commandId = commandId;
    }

   

    public String getCommandPath()
    {
        return commandPath;
    }

    public void setCommandPath(String commandPath)
    {
        this.commandPath = commandPath;
    }

    public OperationType getOperationType()
    {
        return operationType;
    }

    public void setOperationType(OperationType operationType)
    {
        this.operationType = operationType;
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
