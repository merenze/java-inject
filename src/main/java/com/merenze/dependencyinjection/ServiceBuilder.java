package com.merenze.dependencyinjection;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ServiceBuilder {
    private final Map<Class<?>, List<Function<ServiceProvider, ?>>> factories = new HashMap<>();

    public <T> ServiceBuilder addSingleton(Class<T> type) {
        return addSingleton(type, type);
    }

    public <S, T extends S> ServiceBuilder addSingleton(Class<S> type, Class<T> implementationType) {
        return addSingletonFactory(type, provider -> resolveByConstructor(implementationType, provider));
    }

    @SuppressWarnings("unchecked")
    private <T> T resolveByConstructor(Class<T> type, ServiceProvider provider) {
        var constructors = type.getConstructors();

        var orderedConstructors = Arrays.stream(constructors)
                .sorted((c1, c2) -> Integer.compare(c1.getParameterCount(), c2.getParameterCount()))
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

    public <T> ServiceBuilder addSingleton(Class<T> type, T instance) {
        return addSingletonFactory(type, provider -> instance);
    }

    public <T> ServiceBuilder addSingletonFactory(Class<T> type, Function<ServiceProvider, T> factory) {
        factories.computeIfAbsent(type, k -> new ArrayList<>()).add(factory);
        return this;
    }

    public ServiceProvider build() {
        return build(false);
    }

    public ServiceProvider build(boolean eager) {
        Map<Class<?>, List<Function<ServiceProvider, ?>>> factories = new HashMap<>();
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
