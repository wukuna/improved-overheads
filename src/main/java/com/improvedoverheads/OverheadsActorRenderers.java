package com.improvedoverheads;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.runelite.api.Actor;

final class OverheadsActorRenderers<T>
{
    private final T self;
    private final OverheadsProfiles profiles;
    private final Function<OverheadsConfig, T> factory;
    private final Map<OverheadsProfiles.Scope, T> renderers = new EnumMap<>(OverheadsProfiles.Scope.class);

    OverheadsActorRenderers(T self, OverheadsProfiles profiles, Function<OverheadsConfig, T> factory)
    {
        this.self = self;
        this.profiles = profiles;
        this.factory = factory;
    }

    T forActor(Actor actor)
    {
        if (actor == null || profiles.scope(actor) == OverheadsProfiles.Scope.SELF) return self;
        return renderers.computeIfAbsent(profiles.scope(actor), scope -> factory.apply(profiles.config(scope)));
    }

    void forEach(Consumer<T> consumer) { renderers.values().forEach(consumer); }
}
