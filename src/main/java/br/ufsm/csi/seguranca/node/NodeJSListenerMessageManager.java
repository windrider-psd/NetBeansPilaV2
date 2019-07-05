/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node;

import br.ufsm.csi.seguranca.pila.network.TCPClient;
import br.ufsm.csi.seguranca.pila.network.TCPClientObserver;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author politecnico
 */
public class NodeJSListenerMessageManager implements TCPClientObserver
{
    
    private class MessageChunk {
    
        private String type;
        private String id;
        private String length;
        private String chunk;
       
        public MessageChunk(String type, String id)
        {
            this.type = type;
            this.id = id;
        }

        public String getType()
        {
            return type;
        }

        public void setType(String type)
        {
            this.type = type;
        }

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public MessageChunk(String chunk)
        {
            this.chunk = chunk;
        }

        public void setChunk(String chunk)
        {
            this.chunk = chunk;
        }
        
    }
    
    private List<NodeJSMessage> nodeJSMessages = new ArrayList<>();
    private TCPClient tCPClient;
    private MessageChunk messageChunk;
    public void WriteMessage()
    {
        
    }

    @Override
    public void OnMessage(TCPClient client, Object message)
    {
        String data = (String) message;
        
        if(data.contains("\\r\\nBEGINHEADERS\\r\\n"))
        {
            String[] afterBegin = data.split("\\r\\nBEGINHEADERS\\r\\n");
            
            String[] afterEnd = afterBegin[0].split("\\r\\nENDHEADERS\\r\\n");
            
            data = afterEnd[1];
            String headers = afterEnd[0];
            
            String[] allHeaders = headers.split("\n");
            Map<String, String> headersMap = new HashMap<>();
            for(String header : allHeaders)
            {
                String[] split = header.split(":");
                String key = split[0].trim().toLowerCase();
                String value = split[1].trim().toLowerCase();
                
                headersMap.put(key, value);
            }
            
            messageChunk = new MessageChunk(headersMap.get("type"), headersMap.get("id"));
        }
        
        if(data.contains("\\r\\nEND\\r\\n"))
        {
            
        }
    }

    @Override
    public void OnClosed(TCPClient client)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
