package com.merenze.dependencyinjection;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

public class ServiceProvider {
    private final Map<Class<?>, Function<ServiceProvider, ?>> factories;
    private final Map<Class<?>, Object> singletons = new HashMap<>();
    private final ThreadLocal<Stack<Class<?>>> resolving = ThreadLocal.withInitial(Stack::new);

    public ServiceProvider(Map<Class<?>, Function<ServiceProvider, ?>> factories) {
        this.factories = factories;
        factories.put(ServiceProvider.class, provider -> provider);
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> type) {
        if (resolving.get().contains(type)) {
            throw new RuntimeException(
                    "The service provider detected a circular dependency detected for " + type.getName());
        }

        if (!singletons.containsKey(type)) {
            if (!factories.containsKey(type)) {
                return null;
            }

            resolving.get().push(type);

            try {
                var factory = factories.get(type);
                var instance = factory.apply(this);
                singletons.put(type, instance);
            } finally {
                resolving.get().pop();
            }
        }

        return (T) singletons.get(type);
    }

    public <T> T getRequiredService(Class<T> type) {
        var service = getService(type);
        if (service == null) {
            throw new RuntimeException("Unable to resolve required service " + type.getName());
        }
        return service;
    }
}