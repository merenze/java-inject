# Java Inject

A simple DI container for Java, based heavily on [Microsoft's DI patterns](https://learn.microsoft.com/en-us/dotnet/core/extensions/dependency-injection).

**Features**
- **Reflection-based service resolution** for services whose constructor parameters are registered in the DI container.
- **Multiple registrations** for a single service type, with the option to get the last-registered service or all services registered by that type.
- **Required and optional resolution** to either return null or throw an exception when the service cannot be resolved.
- **Circular dependency detection** to prevent infinite recursion.
- **Eager and lazy loading** to control when singletons are instantiated.

## Getting started

### Adding dependencies

Add as a dependency to your POM:

```xml
<dependency>
    <groupId>com.merenze.dependencyinjection</groupId>
    <artifactId>javainject</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Full example

Consider an `Animal` interface with implementations `Dog` and `Cat`:

```java
interface Animal {
    void makeNoise();
}

class Dog implements Animal {
    @Override
    public void makeNoise() {
        System.out.println("Woof!");
    }
}

class Cat implements Animal {
    @Override
    public void makeNoise() {
        System.out.println("Meow!");
    }
}
```

### Basic usage

You can register a concrete implementation by itself, or as an implementation of a supertype:

```java
ServiceBuilder builder = new ServiceBuilder();

builder.addSingleton(Dog.class);
builder.addSingleton(Animal.class, Cat.class);

ServiceProvider provider = builder.build();

// Resolve and use services
Dog dog = provider.getRequiredService(Dog.class);
Animal animal = provider.getRequiredService(Animal.class);

cat.makeNoise(); // prints "Meow!"
dog.makeNoise(); // prints "Woof!"
```

### Multiple implementations

You can register multiple implementations of the same type and resolve them as a `List`, in registration order:

```java
ServiceBuilder builder = new ServiceBuilder();

builder.addSingleton(Animal.class, Dog.class);
builder.addSingleton(Animal.class, Cat.class);

ServiceProvider provider = builder.build();

List<Animal> animals = provider.getServices(Animal.class);

// Prints:
// "Woof!"
// "Meow!"
for (Animal animal : animals)
{
    animal.makeNoise();
}


// The last-registered implementation always wins for getService or getRequiredService:
provider.getRequiredService(Animal.class).makeNoise(); // Prints: "Meow!"
```
