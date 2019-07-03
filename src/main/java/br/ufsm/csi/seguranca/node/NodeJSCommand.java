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
    private String id;
    private String commandPath;
    private OperationType operationType;
    private String arg;

    public NodeJSCommand()
    {
    }

    public NodeJSCommand(String id, String commandPath, OperationType operationType, String arg)
    {
        this.id = id;
        this.commandPath = commandPath;
        this.operationType = operationType;
        this.arg = arg;
    }

    
    
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
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
