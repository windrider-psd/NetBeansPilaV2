package br.ufsm.csi.seguranca.pila.Serialization;

import java.io.*;

public class SerializationUtils {

    public static void SerializeObject(Object object, OutputStream outputStream) throws IOException
    {
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
        objectOutputStream.writeObject(object);
        objectOutputStream.close();
    }

    public static Object DeserializeObject(InputStream inputStream) throws  IOException, ClassNotFoundException
    {
        ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
        Object object = objectInputStream.readObject();
        objectInputStream.close();
        return  object;
    }
}
