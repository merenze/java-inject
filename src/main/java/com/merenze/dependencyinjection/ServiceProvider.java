package com.merenze.dependencyinjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

public class ServiceProvider {
    private final Map<Class<?>, List<Function<ServiceProvider, ?>>> factories;

    private final Map<Class<?>, List<Object>> singletons = new HashMap<>();

    private final ThreadLocal<Stack<Class<?>>> resolving = ThreadLocal.withInitial(Stack::new);

    public ServiceProvider(Map<Class<?>, List<Function<ServiceProvider, ?>>> factories) {
        this.factories = factories;
        // register the provider itself as a singleton
        this.factories.put(ServiceProvider.class, List.of(p -> p));
    }

    /** Returns the last-registered instance of the given type, or null if the type is not registered */
    public <T> T getService(Class<T> type) {
        List<T> services = getServices(type);
        return services.isEmpty() ? null : services.get(services.size() - 1);
    }

    /** Returns all instances registered for the given type, in registration order */
    @SuppressWarnings("unchecked")
    public <T> List<T> getServices(Class<T> type) {
        if (resolving.get().contains(type)) {
            throw new RuntimeException(
                    "The service provider detected a circular dependency for " + type.getName());
        }

        if (!singletons.containsKey(type)) {
            List<Function<ServiceProvider, ?>> typeFactories = factories.get(type);
            if (typeFactories == null || typeFactories.isEmpty()) {
                return List.of(); // no factories registered
            }

            resolving.get().push(type);
            List<Object> instances = new ArrayList<>(typeFactories.size());
            try {
                for (Function<ServiceProvider, ?> factory : typeFactories) {
                    instances.add(factory.apply(this));
                }
            } finally {
                resolving.get().pop();
            }
            singletons.put(type, instances);
        }

        return (List<T>) singletons.get(type);
    }

    /** Returns last-registered instance of the given type, or throws if none is registered */
    public <T> T getRequiredService(Class<T> type) {
        T service = getService(type);
        if (service == null) {
            throw new RuntimeException("Unable to resolve required service " + type.getName());
        }
        return service;
    }
}
