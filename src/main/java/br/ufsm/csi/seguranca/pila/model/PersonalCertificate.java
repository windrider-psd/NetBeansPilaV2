package br.ufsm.csi.seguranca.pila.model;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;

public class PersonalCertificate implements Serializable {
    private PublicKey publicKey;
    private PrivateKey privateKey;

    private PersonalCertificate(PublicKey publicKey, PrivateKey privateKey) {
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
    static PersonalCertificate instance;
    public static PersonalCertificate getInstance()
    {
        if(instance == null)
        {
            instance = new PersonalCertificate(null, null);
        }
        return instance;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}
