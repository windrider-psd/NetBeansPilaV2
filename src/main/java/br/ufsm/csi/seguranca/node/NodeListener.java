/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node;

import br.ufsm.csi.seguranca.pila.Serialization.SerializationUtils;
import br.ufsm.csi.seguranca.pila.network.TCPServer;
import br.ufsm.csi.seguranca.pila.network.TCPServerObserver;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayOutputStream;
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
public class NodeListener implements TCPServerObserver
{

    @Override
    public void OnMessageReceived(Socket socket, Object obj)
    {
        InvokeRoutes("user", OperationType.READ, "{\"id\":\"afaf\",\"inetAddress\":null,\"publicKey\":null}");
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

    private class Route
    {

        private Method method;
        private Object instance;

        public Route(Method method, Object instance)
        {
            this.method = method;
            this.instance = instance;
            this.method.setAccessible(true);
        }

        public Object InvokeRoute(Object arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            int count = method.getParameterCount();
            if (count == 0)
            {
                return this.method.invoke(instance);
            } else
            {
                return this.method.invoke(instance, arg);
            }
        }

        public Object InvokeRoute(boolean arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            int count = method.getParameterCount();
            if (count == 0)
            {
                return this.method.invoke(instance);
            } else
            {
                return this.method.invoke(instance, arg);
            }
        }

        public Object InvokeRoute(byte arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            int count = method.getParameterCount();
            if (count == 0)
            {
                return this.method.invoke(instance);
            } else
            {
                return this.method.invoke(instance, arg);
            }
        }

        public Object InvokeRoute(short arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            int count = method.getParameterCount();
            if (count == 0)
            {
                return this.method.invoke(instance);
            } else
            {
                return this.method.invoke(instance, arg);
            }
        }

        public Object InvokeRoute(int arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            int count = method.getParameterCount();
            if (count == 0)
            {
                return this.method.invoke(instance);
            } else
            {
                return this.method.invoke(instance, arg);
            }
        }

        public Object InvokeRoute(float arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            int count = method.getParameterCount();
            if (count == 0)
            {
                return this.method.invoke(instance);
            } else
            {
                return this.method.invoke(instance, arg);
            }
        }

        public Object InvokeRoute(long arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            int count = method.getParameterCount();
            if (count == 0)
            {
                return this.method.invoke(instance);
            } else
            {
                return this.method.invoke(instance, arg);
            }
        }

        public Object InvokeRoute(double arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
        {
            int count = method.getParameterCount();
            if (count == 0)
            {
                return this.method.invoke(instance);
            } else
            {
                return this.method.invoke(instance, arg);
            }
        }

        public Method getMethod()
        {
            return method;
        }

        public void setMethod(Method method)
        {
            this.method = method;
        }

        public Object getInstance()
        {
            return instance;
        }

        public void setInstance(Object instance)
        {
            this.instance = instance;
        }

    }

    private Map< String, Map< OperationType, Set< Route>>> routeMapMap = new HashMap<>();
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

    public NodeListener()
    {

        try
        {
            SetUpRoutes("br.ufsm.csi.seguranca.node.controllers");
            this.tcpServer = new TCPServer(new ServerSocket(42228), 5012, 1);
            this.tcpServer.AddObserver(this);
            this.tcpServer.StartListening();
            
            
        } catch (IOException ex)
        {
            Logger.getLogger(NodeListener.class.getName()).log(Level.SEVERE, null, ex);
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

                    String command = nodeJSControllerRoute.Command();
                    if (!caseSentitiveCommands)
                    {
                        command = command.toLowerCase();
                    }
                    if (!routeMapMap.containsKey(command))
                    {
                        Map< OperationType, Set< Route>> routeMap = new HashMap<>();
                        for (OperationType op : OperationType.values())
                        {
                            routeMap.put(op, new HashSet<>());
                        }

                        routeMapMap.put(command, routeMap);
                    }

                    Map< OperationType, Set< Route>> routeMap = routeMapMap.get(command);

                    routeMap.get(nodeJSControllerRoute.OperationType()).add(new Route(method, instance));
                }

            }

        } catch (IOException | ClassNotFoundException | IllegalAccessException | InstantiationException | SecurityException ex)
        {
            ex.printStackTrace();
        }
    }

    public void InvokeRoutes(String command, OperationType operationType, String arg)
    {
        if (!caseSentitiveCommands)
        {
            command = command.toLowerCase();
        }

        if (routeMapMap.containsKey(command))
        {
            Set< Route> routes = routeMapMap.get(command).get(operationType);
            Object returnedValue = null;

            for (Route route : routes)
            {
                try
                {

                    if (route.getMethod().getParameterCount() == 0)
                    {
                        returnedValue = route.InvokeRoute(arg);
                    } else if (isValidJSON(arg))
                    {
                        Parameter parameter = route.getMethod().getParameters()[0];
                        if (!parameter.getType().isPrimitive())
                        {
                            ObjectMapper objectMapper = new ObjectMapper();
                            Object obj;
                            try
                            {
                                obj = objectMapper.readValue(arg, parameter.getType());
                            }
                            catch(Exception ex)
                            {
                                continue;
                            }
                            returnedValue = route.InvokeRoute(obj);

                        }
                    } else
                    {
                        Parameter parameter = route.getMethod().getParameters()[0];

                        try
                        {
                            if (parameter.getType().isPrimitive())
                            {
                                if (parameter.getType().equals(boolean.class))
                                {
                                    boolean value = Boolean.parseBoolean(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(byte.class))
                                {
                                    byte value = Byte.parseByte(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(short.class))
                                {
                                    short value = Short.parseShort(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(int.class))
                                {
                                    int value = Integer.parseInt(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(long.class))
                                {
                                    long value = Long.parseLong(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(float.class))
                                {
                                    float value = Float.parseFloat(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(double.class))
                                {
                                    double value = Double.parseDouble(arg);
                                    returnedValue = route.InvokeRoute(value);
                                }
                            } else if (IsFromPrimitive(parameter.getType()))
                            {
                                if (parameter.getType().equals(Boolean.class))
                                {
                                    Boolean value = Boolean.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(Byte.class))
                                {
                                    Byte value = Byte.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(Short.class))
                                {
                                    Short value = Short.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(Integer.class))
                                {
                                    Integer value = Integer.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(Long.class))
                                {
                                    Long value = Long.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(Float.class))
                                {
                                    Float value = Float.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                } else if (parameter.getType().equals(Double.class))
                                {
                                    Double value = Double.valueOf(arg);
                                    returnedValue = route.InvokeRoute(value);
                                }
                            } else if (parameter.getType().equals(String.class))
                            {
                                returnedValue = route.InvokeRoute(arg);
                            } else
                            {
                                continue;
                            }
                        } catch (Exception ex)
                        {
                            continue;
                        }
                    }
                    
                    if (returnedValue != null && socket != null && operationType == OperationType.READ)
                    {
                        byte[] bytes;
                        if (returnedValue.getClass().isPrimitive() || IsFromPrimitive(returnedValue.getClass()))
                        {
                            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                            SerializationUtils.SerializeObject(returnedValue.toString(), byteArrayOutputStream);
                            bytes = byteArrayOutputStream.toByteArray();

                        } else
                        {
                            ObjectMapper mapper = new ObjectMapper();
                            String json = mapper.writeValueAsString(returnedValue);
                           
                            bytes = json.getBytes();
                        }
                        socket.getOutputStream().write(bytes);
                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
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
        } catch (IOException e)
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
            } else if (file.getName().endsWith(".class"))
            {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}
