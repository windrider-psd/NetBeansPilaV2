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
public class NodeJSCommandError
{
    private String code;
    private String message;

    public NodeJSCommandError(String code, String message)
    {
        this.code = code;
        this.message = message;
    }
    
    public static NodeJSCommandError FromException(Exception ex)
    {
        return new NodeJSCommandError(ex.getClass().getSimpleName(), ex.getMessage());
    }
    
    public static NodeJSCommandError FromException(Throwable ex)
    {
        return new NodeJSCommandError(ex.getClass().getSimpleName(), ex.getMessage());
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
    
    
}
