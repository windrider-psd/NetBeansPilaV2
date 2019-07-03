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
    private String id;
    private ResponseStatus responseStatus;
    private Object arg;

    public NodeJSCommandResponse()
    {
    }

    public NodeJSCommandResponse(String id, ResponseStatus responseStatus, Object arg)
    {
        this.id = id;
        this.responseStatus = responseStatus;
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
