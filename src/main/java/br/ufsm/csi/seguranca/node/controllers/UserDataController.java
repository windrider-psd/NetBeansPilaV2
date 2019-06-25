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
import br.ufsm.csi.seguranca.pila.model.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author politecnico
 */

@NodeJSController
public class UserDataController {
    
      
    @NodeJSControllerRoute(CommandPath = "user", OperationType = OperationType.READ)
    public SerializableUser ReadUser() throws JsonProcessingException
    {
        System.out.println("Reading");
        SerializableUser serializableUser = new SerializableUser();
        serializableUser.setId(Main.thisUser.getId());
        serializableUser.setInetAddress(Main.thisUser.getInetAddress().toString());
        serializableUser.setPublicKey(Base64.getEncoder().encodeToString(Main.thisUser.getPublicKey().getEncoded()));
        return serializableUser;
       //return new SerializableUser(Main.thisUser);
    }
    @NodeJSControllerRoute(CommandPath = "user", OperationType = OperationType.WRITE)
    public void WriteUser(SerializableUser serializableUser)
    {
        System.out.println("Writing");
        try
        {
            User user = new User();
            user.setId(serializableUser.getId());
            user.setInetAddress(InetAddress.getByName(serializableUser.getInetAddress().substring(1)));
            byte[] byteKey = Base64.getDecoder().decode(serializableUser.getPublicKey());
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            
            user.setPublicKey(kf.generatePublic(X509publicKey));
            Main.thisUser = user;
            
            System.out.println(Main.thisUser.getId());
        }
        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(SerializableUser.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (InvalidKeySpecException ex)
        {
            Logger.getLogger(UserDataController.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (UnknownHostException ex)
        {
            Logger.getLogger(UserDataController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
