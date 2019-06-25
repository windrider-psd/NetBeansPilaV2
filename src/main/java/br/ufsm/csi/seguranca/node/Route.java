/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.node;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 *
 * @author politecnico
 */
class Route
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

    /*public Object InvokeRoute(boolean arg) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException
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
    }*/

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
