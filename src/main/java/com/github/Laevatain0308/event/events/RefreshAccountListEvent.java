package com.github.Laevatain0308.event.events;

import com.github.Laevatain0308.account.Account;

public record RefreshAccountListEvent(Account account , RefreshAccountListType type)
{
    public enum RefreshAccountListType
    {
        Add , Delete
    }
}
