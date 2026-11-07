package com.improvedoverheads;

import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.Player;

enum OverheadsVisibility
{
    ALL("Always"),
    CURRENT_TARGET("Current target"),
    IN_COMBAT("In combat"),
    HIDDEN("Hidden");

    private final String label;

    OverheadsVisibility(String label)
    {
        this.label = label;
    }

    boolean includes(Client client, Actor actor)
    {
        Player local = client.getLocalPlayer();
        switch (this)
        {
            case HIDDEN:
                return false;
            case CURRENT_TARGET:
                return local != null && local.getInteracting() == actor;
            case IN_COMBAT:
                return actor.getHealthRatio() >= 0 && actor.getHealthScale() > 0;
            default:
                return true;
        }
    }

    @Override
    public String toString()
    {
        return label;
    }
}
