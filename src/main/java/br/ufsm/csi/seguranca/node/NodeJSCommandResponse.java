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
public class NodeJSCommandResponse
{
    private int commandId;
    private ResponseStatus responseStatus;
    private Object arg;

    public NodeJSCommandResponse()
    {
    }

    public NodeJSCommandResponse(int commandId, ResponseStatus responseStatus, Object arg)
    {
        this.commandId = commandId;
        this.responseStatus = responseStatus;
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

    public ResponseStatus getResponseStatus()
    {
        return responseStatus;
    }

    public void setResponseStatus(ResponseStatus responseStatus)
    {
        this.responseStatus = responseStatus;
    }

    public Object getArg()
    {
        return arg;
    }

    public void setArg(Object arg)
    {
        this.arg = arg;
    }

    
    
    public static NodeJSCommandResponse FromException(Exception ex, ResponseStatus responseStatus)
    {
        NodeJSCommandResponse nodeJSCommandResponse = new NodeJSCommandResponse();
        nodeJSCommandResponse.setResponseStatus(responseStatus);
        nodeJSCommandResponse.setArg(new NodeJSCommandError(ex.getClass().getSimpleName(), ex.getMessage()));
        return nodeJSCommandResponse;
    }
    public static Object ArgFromException(Exception ex)
    {
        return new NodeJSCommandError(ex.getClass().getSimpleName(), ex.getMessage());
    }
            
    
    
}
