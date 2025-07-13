package com.github.Laevatain0308.account;

import com.github.Laevatain0308.event.EventBus;
import com.github.Laevatain0308.event.events.RefreshMainSceneAccountEvent;

import java.util.ArrayList;
import java.util.List;

public class AccountManager
{
    public final static AccountManager instance = new AccountManager();

    private final List<Account> users;
    private Account currentUser;


    public AccountManager()
    {
        users = new ArrayList<>();

        //users.add(new Account("Laevatain" , LoadType.Offline));
        //users.add(new Account("123" , LoadType.Offline));
        //users.add(new Account("456" , LoadType.Offline));
        //users.add(new Account("789" , LoadType.Offline));
        //
        //currentUser = users.get(0);
    }


    public List<Account> getUsers() { return users; }

    public void addUser(Account user)
    {
        if (!users.contains(user))
        {
            users.add(user);

            if (users.size() == 1)
            {
                currentUser = users.get(0);
                EventBus.getInstance().publish(new RefreshMainSceneAccountEvent(currentUser));
            }
        }

    }
    public void removeUser(Account user)
    {
        if (!users.remove(user))
            return;

        if (user == currentUser)
        {
            if (users.isEmpty())
                currentUser = null;
            else
                currentUser = users.get(0);
        }

        EventBus.getInstance().publish(new RefreshMainSceneAccountEvent(currentUser));
    }


    public Account getCurrentUser() { return currentUser; }
    public void setCurrentUser(Account currentUser)
    {
        if (users.contains(currentUser))
        {
            this.currentUser = currentUser;
            EventBus.getInstance().publish(new RefreshMainSceneAccountEvent(currentUser));
        }
    }
    public boolean isCurrentUser(Account user) { return users.contains(user) && currentUser == user; }
}