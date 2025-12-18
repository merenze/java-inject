package com.merenze.dependencyinjection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.Function;

/**
 * A simple constructor-based dependency injection container that supports
 * singleton services and factory-based registrations.
 *
 * <p>
 * Services are registered via a {@link ServiceBuilder}, and this provider
 * resolves them lazily on first request. Multiple instances of the same type
 * may be registered, in which case {@link #getServices(Class)} returns all
 * of them in registration order, and {@link #getService(Class)} returns the
 * last-registered instance.
 * </p>
 *
 * <p>
 * Circular dependencies are detected during resolution, and attempting to
 * resolve a required service that has not been registered will throw a
 * runtime exception.
 * </p>
 *
 * <p>
 * Thread safety: The provider is safe for concurrent reads. Circular dependency
 * tracking uses a {@link ThreadLocal} stack to avoid false positives across
 * threads.
 * </p>
 */
public class ServiceProvider {

    /** Map of service type to a list of factories producing singleton instances. */
    private final Map<Class<?>, List<Function<ServiceProvider, ?>>> factories;

    /** Cache of singleton instances per service type, in registration order. */
    private final Map<Class<?>, List<Object>> singletons = new HashMap<>();

    /**
     * Tracks currently resolving types to detect circular dependencies per thread.
     */
    private final ThreadLocal<Stack<Class<?>>> resolving = ThreadLocal.withInitial(Stack::new);

    /**
     * Constructs a new ServiceProvider with the given factory registrations.
     *
     * <p>
     * The provider automatically registers itself as a singleton under
     * {@link ServiceProvider.class}.
     * </p>
     *
     * @param factories a map from service types to lists of factory functions.
     *                  Each factory function receives this provider and produces
     *                  a singleton instance. If a type has multiple factories,
     *                  all will be invoked in registration order.
     */
    protected ServiceProvider(Map<Class<?>, List<Function<ServiceProvider, ?>>> factories) {
        this.factories = factories;
        // register the provider itself as a singleton
        this.factories.put(ServiceProvider.class, List.of(p -> p));
    }

    /**
     * Returns the last-registered singleton instance for the given service type.
     *
     * <p>
     * If no instances are registered for the type, returns {@code null}.
     * </p>
     *
     * @param <T>  the service type
     * @param type the class object representing the service type to resolve
     * @return the last-registered instance of {@code type}, or {@code null} if none
     *         is registered
     */
    public <T> T getService(Class<T> type) {
        List<T> services = getServices(type);
        return services.isEmpty() ? null : services.get(services.size() - 1);
    }

    /**
     * Returns all singleton instances registered for the given service type.
     *
     * <p>
     * Instances are returned in registration order. If multiple factories were
     * registered for a type, each will be invoked once and cached. Subsequent
     * calls return the cached instances.
     * </p>
     *
     * <p>
     * Circular dependencies are detected and will throw a {@link RuntimeException}.
     * </p>
     *
     * @param <T>  the service type
     * @param type the class object representing the service type to resolve
     * @return a list of singleton instances registered for {@code type}, in
     *         registration order. Returns an empty list if no instances are
     *         registered.
     * @throws RuntimeException if a circular dependency is detected
     */
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

    /**
     * Returns the last-registered singleton instance for the given service type,
     * throwing an exception if no instance is registered.
     *
     * <p>
     * This is useful for mandatory dependencies that must exist.
     * </p>
     *
     * @param <T>  the service type
     * @param type the class object representing the service type to resolve
     * @return the last-registered instance of {@code type}
     * @throws RuntimeException if no instance is registered for {@code type}
     */
    public <T> T getRequiredService(Class<T> type) {
        T service = getService(type);
        if (service == null) {
            throw new RuntimeException("Unable to resolve required service " + type.getName());
        }
        return service;
    }
}
