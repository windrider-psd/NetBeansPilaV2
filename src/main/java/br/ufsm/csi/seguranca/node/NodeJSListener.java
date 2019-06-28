/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node;

import br.ufsm.csi.seguranca.pila.network.TCPServer;
import br.ufsm.csi.seguranca.pila.network.TCPServerObserver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author politecnico
 */
public class NodeJSListener implements TCPServerObserver
{

    @Override
    public void OnMessageReceived(Socket socket, Object obj)
    {
        if (obj instanceof String)
        {
            try
            {
                ObjectMapper objectMapper = new ObjectMapper();
                NodeJSCommand nodeJSCommand = objectMapper.readValue((String) obj, NodeJSCommand.class);
                InvokeRoutes(nodeJSCommand.getCommandPath(), nodeJSCommand.getOperationType(), nodeJSCommand.getArg());
            }
            catch (IOException ex)
            {
                System.out.println("Invalid Command: " + ex.getMessage());
            }
        }
    }

    @Override
    public void OnMessageSent(Socket socket, Object obj)
    {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void OnConnection(Socket client)
    {
        this.socket = client;
    }

    private Map<String, Map<OperationType, Route>> routeMapMap = new HashMap<>();
    private static Map< String, Class> builtInMap = new HashMap< String, Class>();

    
    {
        builtInMap.put("int", Integer.TYPE);
        builtInMap.put("long", Long.TYPE);
        builtInMap.put("double", Double.TYPE);
        builtInMap.put("float", Float.TYPE);
        builtInMap.put("bool", Boolean.TYPE);
        builtInMap.put("char", Character.TYPE);
        builtInMap.put("byte", Byte.TYPE);
        builtInMap.put("void", Void.TYPE);
        builtInMap.put("short", Short.TYPE);
    }

    private Socket socket;
    private boolean caseSentitiveCommands;
    private TCPServer tcpServer;

    public NodeJSListener(String packageName)
    {

        try
        {
            SetUpRoutes(packageName);
            this.tcpServer = new TCPServer(new ServerSocket(42228), 5012, 1);
            this.tcpServer.AddObserver(this);
            this.tcpServer.StartListening();

        }
        catch (IOException ex)
        {
            Logger.getLogger(NodeJSListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void SetUpRoutes(String packageName)
    {
        try
        {
            Set<Class> classes = new HashSet<>(Arrays.asList(getClasses(packageName)));

            classes.stream().filter(c ->
            {
                Annotation[] annotations = c.getAnnotations();
                for (Annotation annotation : annotations)
                {
                    if (annotation.annotationType().equals(NodeJSController.class))
                    {
                        return true;
                    }
                }
                return false;
            }).collect(Collectors.toList());

            for (Class c : classes)
            {
                Object instance = c.newInstance();
                Set<Method> methods = new HashSet<>(Arrays.asList(c.getMethods()));

                methods = methods.stream().filter(method ->
                {
                    Annotation annotation = method.getAnnotation(NodeJSControllerRoute.class);
                    Parameter[] parameters = method.getParameters();

                    boolean valid = annotation != null
                            && (parameters.length == 0
                            || (parameters.length == 1 && !parameters[0].getType().isArray()));

                    return valid;

                }).collect(Collectors.toSet());

                for (Method method : methods)
                {

                    NodeJSControllerRoute nodeJSControllerRoute = method.getAnnotation(NodeJSControllerRoute.class);

                    String command = nodeJSControllerRoute.CommandPath();
                    if (!caseSentitiveCommands)
                    {
                        command = command.toLowerCase();
                    }
                    if (!routeMapMap.containsKey(command))
                    {
                        Map< OperationType, Route> routeMap = new HashMap<>();
                        for (OperationType op : OperationType.values())
                        {
                            routeMap.put(op, null);
                        }

                        routeMapMap.put(command, routeMap);
                    }

                    Map<OperationType, Route> routeMap = routeMapMap.get(command);
                    routeMap.put(nodeJSControllerRoute.OperationType(), new Route(method, instance));
                }

            }

        }
        catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | SecurityException ex)
        {
            ex.printStackTrace();
        }
    }

    public void InvokeRoutes(String command, OperationType operationType, String arg)
    {

        Object respArg;
        ResponseStatus responseStatus;
        if (!caseSentitiveCommands)
        {
            command = command.toLowerCase();
        }

        if (routeMapMap.containsKey(command))
        {
            Route route = routeMapMap.get(command).get(operationType);
            if (route == null)
            {
                responseStatus = ResponseStatus.INVALID;
                respArg = new NodeJSCommandError("NRT", "No route for " + command);
            }
            else
            {
                try
                {
                    if (route.getMethod().getParameterCount() == 0)
                    {
                        respArg = route.InvokeRoute(arg);
                        responseStatus = ResponseStatus.OK;
                    }
                    else if (isValidJSON(arg))
                    {
                        Parameter parameter = route.getMethod().getParameters()[0];
                        ObjectMapper objectMapper = new ObjectMapper();
                        try
                        {
                            Object obj;
                            obj = objectMapper.readValue(arg, parameter.getType());
                            
                            respArg = route.InvokeRoute(obj);
                            responseStatus = ResponseStatus.OK;
                            System.out.println(respArg);
                        }
                        catch (IOException | IllegalAccessException | IllegalArgumentException ex)
                        {
                            respArg = NodeJSCommandError.FromException(ex);
                            responseStatus = ResponseStatus.ERROR;
                        }
                        catch (InvocationTargetException ex)
                        {
                            responseStatus = ResponseStatus.ERROR;
                            respArg = NodeJSCommandError.FromException((Exception) ex.getTargetException());
                        }
                    }
                    else
                    {
                        Object returnedValue = null;
                        Parameter parameter = route.getMethod().getParameters()[0];
                        try
                        {
                            boolean valid = false;
                            if (parameter.getType().isPrimitive())
                            {
                                if (parameter.getType().equals(boolean.class))
                                {
                                    boolean value = Boolean.parseBoolean(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(byte.class))
                                {
                                    byte value = Byte.parseByte(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(short.class))
                                {
                                    short value = Short.parseShort(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(int.class))
                                {
                                    int value = Integer.parseInt(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(long.class))
                                {
                                    long value = Long.parseLong(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(float.class))
                                {
                                    float value = Float.parseFloat(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(double.class))
                                {
                                    double value = Double.parseDouble(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                            }
                            else if (IsFromPrimitive(parameter.getType()))
                            {
                                if (parameter.getType().equals(Boolean.class))
                                {
                                    Boolean value = Boolean.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(Byte.class))
                                {
                                    Byte value = Byte.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(Short.class))
                                {
                                    Short value = Short.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(Integer.class))
                                {
                                    Integer value = Integer.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(Long.class))
                                {
                                    Long value = Long.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(Float.class))
                                {
                                    Float value = Float.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                                else if (parameter.getType().equals(Double.class))
                                {
                                    Double value = Double.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                    valid = true;
                                }
                            }
                            else if (parameter.getType().equals(String.class))
                            {
                                returnedValue = route.InvokeRoute(arg);
                                valid = true;
                            }
                            /*else
                            {
                                responseStatus = ResponseStatus.INVALID;
                                respArg = new NodeJSCommandError("NRT", "No route for " + command);
                            }*/
                            
                            if(valid)
                            {
                                respArg = route.InvokeRoute(returnedValue);
                                responseStatus = ResponseStatus.OK;
                            }
                            else
                            {
                                respArg = route.InvokeRoute(new NodeJSCommandError("IARG", "Invalid arg"));
                                responseStatus = ResponseStatus.INVALID;
                            }
                        }
                        catch (Exception ex)
                        {
                            responseStatus = ResponseStatus.ERROR;
                            respArg = NodeJSCommandError.FromException(ex);
                        }
                    }
                    //
                }
                catch (IllegalAccessException | IllegalArgumentException ex)
                {
                    responseStatus = ResponseStatus.ERROR;
                    respArg = NodeJSCommandError.FromException(ex);
                }
                catch (InvocationTargetException ex)
                {
                    responseStatus = ResponseStatus.ERROR;
                    respArg = NodeJSCommandError.FromException((Exception) ex.getTargetException());
                }
            }
        }
        else
        {
            responseStatus = ResponseStatus.INVALID;
            respArg = new NodeJSCommandError("CMP", "No command path for " + command);
        }

        if (socket != null)
        {
            NodeJSCommandResponse nodeJSCommandResponse = new NodeJSCommandResponse();
            nodeJSCommandResponse.setArg(respArg);
            nodeJSCommandResponse.setResponseStatus(responseStatus);
            try
            {
                byte[] bytes;
                ObjectMapper mapper = new ObjectMapper();
                String json = mapper.writeValueAsString(nodeJSCommandResponse);
                bytes = json.getBytes();
                socket.getOutputStream().write(bytes);
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

    }
    

    private static boolean IsFromPrimitive(Class classz)
    {
        return builtInMap.containsValue(classz);
    }

    public boolean isValidJSON(final String json)
    {
        try
        {
            String trim = json.trim();
            if (trim.isEmpty())
            {
                return false;
            }
            final ObjectMapper mapper = new ObjectMapper();
            JsonNode jnode = mapper.readTree(json);
            return jnode.fields().hasNext();
        }
        catch (IOException e)
        {
            return false;
        }
    }

    static Class[] getClasses(String packageName) throws ClassNotFoundException, IOException
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration resources = classLoader.getResources(path);
        List dirs = new ArrayList();
        while (resources.hasMoreElements())
        {
            URL resource = (URL) resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        ArrayList classes = new ArrayList();
        for (Object directory : dirs)
        {
            classes.addAll(findClasses((File) directory, packageName));
        }
        return (Class[]) classes.toArray(new Class[classes.size()]);
    }

    static List findClasses(File directory, String packageName) throws ClassNotFoundException
    {
        List classes = new ArrayList();
        if (!directory.exists())
        {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files)
        {
            if (file.isDirectory())
            {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            }
            else if (file.getName().endsWith(".class"))
            {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}