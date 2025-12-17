package com.merenze.dependencyinjection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.merenze.dependencyinjection.stubs.*;

import java.util.List;
import java.util.Optional;

class MultiRegistrationTests {

    private ServiceBuilder builder;

    @BeforeEach
    void setup() {
        builder = new ServiceBuilder();
    }

    @Test
    void testGetServicesReturnsEmptyIfNoneRegistered() {
        var provider = builder.build();
        List<Gadget> gadgets = provider.getServices(Gadget.class);
        Assertions.assertNotNull(gadgets);
        Assertions.assertTrue(gadgets.isEmpty(), "Expected empty list when no service is registered");
    }

    @Test
    void testGetServicesReturnsAllRegisteredInstancesInOrder() {
        builder.addSingleton(Gadget.class, new Gadget(new Gizmo()))
                .addSingleton(Gadget.class, new Gadget(new Gizmo()))
                .addSingleton(Gadget.class, new Gadget(new Gizmo()));

        var provider = builder.build();

        List<Gadget> gadgets = provider.getServices(Gadget.class);
        Assertions.assertEquals(3, gadgets.size(), "Expected three registered services");
        Assertions.assertNotNull(gadgets.get(0).gizmo);
        Assertions.assertNotNull(gadgets.get(1).gizmo);
        Assertions.assertNotNull(gadgets.get(2).gizmo);
    }

    @Test
    void testGetServiceAndGetRequiredServiceReturnLastRegistered() {
        builder.addSingleton(Gadget.class, new Gadget(new Gizmo()))
                .addSingleton(Gadget.class, new Gadget(new Gizmo(), new Whodad()));

        var provider = builder.build();

        Gadget last = provider.getService(Gadget.class);
        Assertions.assertNotNull(last);
        Assertions.assertNotNull(last.gizmo);
        Assertions.assertNotNull(last.whodad, "getService should return the last registered instance with whodad");

        Gadget required = provider.getRequiredService(Gadget.class);
        Assertions.assertNotNull(required);
        Assertions.assertNotNull(required.gizmo);
        Assertions.assertNotNull(required.whodad,
                "getRequiredService should return the last registered instance with whodad");
    }
}
