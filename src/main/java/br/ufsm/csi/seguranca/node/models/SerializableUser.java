/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node.models;

import br.ufsm.csi.seguranca.pila.model.User;
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
public class SerializableUser
{

    private String id;
    private String inetAddress;
    private String publicKey;

    public SerializableUser()
    {
        this.id = "null";
        this.inetAddress = "192.168.32.186";
        this.publicKey = "null";
    }

    
    
    /*public SerializableUser(User user)
    {
        this.id = user.getId();
        this.inetAddress = user.getInetAddress().toString();
        this.publicKey = Base64.getEncoder().encodeToString(user.getPublicKey().getEncoded());
    }*/
    /*public User getUser() throws UnknownHostException, InvalidKeySpecException
    {
        try
        {
            User user = new User();
            user.setId(id);
            user.setInetAddress(InetAddress.getByName(inetAddress.substring(1)));
            byte[] byteKey = Base64.getDecoder().decode(publicKey);
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(byteKey);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            
            user.setPublicKey(kf.generatePublic(X509publicKey));
            return user;
        }
        catch (NoSuchAlgorithmException ex)
        {
            Logger.getLogger(SerializableUser.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }*/
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getInetAddress()
    {
        return inetAddress;
    }

    public void setInetAddress(String inetAddress)
    {
        this.inetAddress = inetAddress;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

}
