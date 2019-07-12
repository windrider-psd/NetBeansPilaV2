/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufsm.csi.seguranca.pila.scouting;

import br.ufsm.csi.seguranca.pila.model.User;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Christian Lemos
 */
public class UserDatabase implements UserScoutObserver
{
    private Set<User> users = new HashSet<>();
    private static UserDatabase instance = null;
    private UserDatabase()
    {
    }
    
    
    
    public static synchronized UserDatabase getInstance()
    {
        if(instance == null)
        {
            instance = new UserDatabase();
        }
        return instance;
    }

    @Override
    public void OnUserFound(User user)
    {
        boolean repeated = false;
        for(User u : this.users)
        {
            if(u.getId().equals(user.getId()))
            {
                repeated = true;
                break;
            }
        }
        if(!repeated)
        {
            users.add(user);
        }
        
    }

    public Set<User> getUsers()
    {
        return users;
    }

    public void setUsers(Set<User> users)
    {
        this.users = users;
    }
    
    
}
