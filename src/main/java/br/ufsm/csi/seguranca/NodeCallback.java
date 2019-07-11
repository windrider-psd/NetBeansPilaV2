/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author politecnico
 */
public class NodeCallback implements MqttCallback
{

    private static NodeCallback instance = null;
    private NodeCallback()
    {
    }
    
    public static synchronized NodeCallback getInstance()
    {
        if(instance == null)
        {
            instance = new NodeCallback();
        }
        return instance;
    }

    @Override
    public void connectionLost(Throwable thrwbl)
    {
        System.exit(0);
    }

    @Override
    public void messageArrived(String string, MqttMessage mm) throws Exception
    {
        ///throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken imdt)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
}
