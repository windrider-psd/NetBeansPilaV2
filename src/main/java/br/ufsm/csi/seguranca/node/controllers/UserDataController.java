/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.controllers;

import br.ufsm.csi.seguranca.Main;
import br.ufsm.csi.seguranca.node.NodeJSController;
import br.ufsm.csi.seguranca.node.NodeJSControllerRoute;
import br.ufsm.csi.seguranca.node.OperationType;
import br.ufsm.csi.seguranca.node.models.SerializableUser;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@NodeJSController
public class UserDataController {
    
      
    @NodeJSControllerRoute(CommandPath = "user", OperationType = OperationType.READ)
    public SerializableUser ReadUser()
    {
        return SerializableUser.FromUser(Main.thisUser);
    }
    @NodeJSControllerRoute(CommandPath = "user", OperationType = OperationType.WRITE)
    public SerializableUser WriteUser(SerializableUser serializableUser) throws UnknownHostException, NoSuchAlgorithmException, InvalidKeySpecException
    {
        Main.thisUser = serializableUser.ToUser();
        return SerializableUser.FromUser(Main.thisUser);
    }
    
}
