package br.ufsm.csi.seguranca.pila.model;

import java.net.InetAddress;
import java.security.PublicKey;

public class User
{

    private String id;
    private InetAddress inetAddress;
    private PublicKey publicKey;

    public User()
    {

    }

    public User(String id, InetAddress inetAddress, PublicKey publicKey)
    {
        this.id = id;
        this.inetAddress = inetAddress;
        this.publicKey = publicKey;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public InetAddress getInetAddress()
    {
        return inetAddress;
    }

    public void setInetAddress(InetAddress inetAddress)
    {
        this.inetAddress = inetAddress;
    }

    public PublicKey getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey)
    {
        this.publicKey = publicKey;
    }
}
