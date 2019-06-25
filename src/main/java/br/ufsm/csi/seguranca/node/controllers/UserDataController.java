/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.controllers;

import br.ufsm.csi.seguranca.node.NodeJSController;
import br.ufsm.csi.seguranca.node.NodeJSControllerRoute;
import br.ufsm.csi.seguranca.node.OperationType;
import br.ufsm.csi.seguranca.pila.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.Socket;

/**
 *
 * @author politecnico
 */
@NodeJSController
public class UserDataController
{

    User user = new User();

    @NodeJSControllerRoute(Command = "user", OperationType = OperationType.READ)
    public User ReadUser() throws JsonProcessingException
    {
        return user;
    }

    @NodeJSControllerRoute(Command = "user", OperationType = OperationType.WRITE)
    public void WriteUser(User user)
    {
        this.user = user;
    }

}
