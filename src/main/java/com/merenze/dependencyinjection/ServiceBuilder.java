package com.merenze.dependencyinjection;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * A builder for constructing a {@link ServiceProvider} using a simple
 * registration-based dependency injection model.
 *
 * <p>
 * Service resolution is constructor-based when no instance or factory is
 * provided. The {@link ServiceProvider} is created from the registrations
 * configured on this builder.
 * </p>
 *
 * <p>
 * Instances of {@code ServiceBuilder} are mutable and not thread-safe.
 * </p>
 */
public class ServiceBuilder {

    private final Map<Class<?>, List<Function<ServiceProvider, ?>>> factories = new HashMap<>();

    /**
     * Registers a singleton service whose implementation type is the same as
     * its exposed service type.
     *
     * <p>
     * The service instance will be created lazily on first resolution unless
     * the provider is built eagerly.
     * </p>
     *
     * <p>
     * Constructor-based resolution is used: the constructor with the most
     * resolvable parameters is preferred. Only public constructors whose
     * parameters can be satisfied by the {@link ServiceProvider} will be used.
     * </p>
     *
     * @param type the service type and concrete implementation class
     * @param <T>  the service type
     * @return this builder instance, for fluent chaining
     */
    public <T> ServiceBuilder addSingleton(Class<T> type) {
        return addSingleton(type, type);
    }

    /**
     * Registers a singleton service with a distinct implementation type.
     *
     * <p>
     * The service will be exposed under {@code type}, but instantiated using
     * {@code implementationType}. The implementation type must be assignable to
     * the service type.
     * </p>
     *
     * <p>
     * Constructor-based resolution is used: the constructor with the fewest
     * resolvable parameters is preferred. Constructor parameters of type
     * {@link Optional} are treated as optional dependencies; all other
     * parameters must be resolvable or the constructor is skipped.
     * </p>
     *
     * @param type               the service type under which the instance will be
     *                           registered
     * @param implementationType the concrete class used to instantiate the service
     * @param <S>                the service type
     * @param <T>                the concrete implementation type
     * @return this builder instance, for fluent chaining
     */
    public <S, T extends S> ServiceBuilder addSingleton(Class<S> type, Class<T> implementationType) {
        return addSingletonFactory(type, provider -> resolveByConstructor(implementationType, provider));
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveByConstructor(Class<T> type, ServiceProvider provider) {
        var constructors = type.getConstructors();

        var orderedConstructors = Arrays.stream(constructors)
                .sorted(Comparator.comparing((Constructor<?> c) -> c.getParameterCount()).reversed())
                .toList();

        for (var constructor : orderedConstructors) {
            List<Object> arguments = new ArrayList<>();
            var resolved = true;

            for (var parameter : constructor.getParameters()) {
                var parameterType = parameter.getType();
                var typeToResolve = parameterType;
                var isOptional = parameterType == Optional.class;

                if (isOptional) {
                    typeToResolve = parameter.getParameterizedType() instanceof ParameterizedType parameterizedType
                            ? (Class<?>) parameterizedType.getActualTypeArguments()[0]
                            : Object.class;
                }

                var argument = provider.getService(typeToResolve);
                if (isOptional) {
                    argument = Optional.ofNullable(argument);
                } else if (argument == null) {
                    resolved = false;
                    break;
                }

                arguments.add(argument);
            }

            if (resolved) {
                try {
                    return (T) constructor.newInstance(arguments.toArray());
                } catch (Exception ex) {
                    throw new RuntimeException("Service provider unable to instantiate " + type.getName(), ex);
                }
            }
        }

        return null;
    }

    /**
     * Registers a singleton service backed by a pre-existing instance.
     *
     * <p>
     * The provided instance will always be returned for the given service
     * type. No constructor injection or lifecycle management is performed.
     * </p>
     *
     * @param type     the service type
     * @param instance the singleton instance to register
     * @param <T>      the service type
     * @return this builder instance, for fluent chaining
     */
    public <T> ServiceBuilder addSingleton(Class<T> type, T instance) {
        return addSingletonFactory(type, provider -> instance);
    }

    /**
     * Registers a singleton service using a custom factory function.
     *
     * <p>
     * The factory is invoked at most once per {@link ServiceProvider} instance,
     * and the resulting value is cached and reused for all subsequent resolutions
     * of the service.
     * </p>
     *
     * <p>
     * The provided {@link ServiceProvider} argument may be used to resolve
     * other services, allowing for manual or conditional dependency resolution.
     * </p>
     *
     * <p>
     * Multiple factories may be registered for the same service type. The
     * {@link ServiceProvider} built from this builder is responsible for deciding
     * how to handle multiple factories for the same type.
     * </p>
     *
     * @param type    the service type
     * @param factory a factory that produces the singleton instance
     * @param <T>     the service type
     * @return this builder instance, for fluent chaining
     */
    public <T> ServiceBuilder addSingletonFactory(Class<T> type, Function<ServiceProvider, T> factory) {
        factories.computeIfAbsent(type, k -> new ArrayList<>()).add(factory);
        return this;
    }

    /**
     * Builds a {@link ServiceProvider} with lazy service instantiation.
     *
     * <p>
     * No services are created during the build phase. Each service is
     * instantiated the first time it is requested.
     * </p>
     *
     * @return a new {@link ServiceProvider} backed by the current registrations
     */
    public ServiceProvider build() {
        return build(false);
    }

    /**
     * Builds a {@link ServiceProvider}, optionally instantiating all registered
     * services eagerly.
     *
     * <p>
     * If {@code eager} is {@code true}, the provider will immediately attempt
     * to resolve all registered service types. This can be useful for validating
     * configuration at startup or surfacing wiring errors early.
     * </p>
     *
     * <p>
     * If {@code eager} is {@code false}, services are instantiated lazily
     * on first resolution.
     * </p>
     *
     * @param eager whether to eagerly instantiate all registered services
     * @return a new {@link ServiceProvider} backed by the current registrations
     */
    public ServiceProvider build(boolean eager) {
        for (var entry : factories.entrySet()) {
            factories.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }

        var provider = new ServiceProvider(factories);

        if (eager) {
            for (var type : factories.keySet()) {
                provider.getService(type);
            }
        }

        return provider;
    }
}
