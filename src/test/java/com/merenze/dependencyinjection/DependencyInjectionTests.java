package com.merenze.dependencyinjection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.merenze.dependencyinjection.stubs.*;

import java.util.Optional;

class ServiceBuilderConstructorTests {

    private ServiceBuilder builder;

    @BeforeEach
    void setup() {
        builder = new ServiceBuilder();
    }

    static class Thingamabob {
    }


    @Test
    void testConstructorOrdering() {
        builder.addSingleton(Thingamabob.class)
                .addSingleton(Whodad.class)
                .addSingleton(Gizmo.class)
                .addSingleton(Gadget.class);

        var provider = builder.build();

        var gadget = provider.getService(Gadget.class);
        Assertions.assertNotNull(gadget);
        Assertions.assertNotNull(gadget.gizmo);
        Assertions.assertNotNull(gadget.whodad);

        Assertions.assertNotNull(gadget.gizmo.whatsit);
        Assertions.assertTrue(gadget.gizmo.whatsit.isEmpty());
    }

    @Test
    void testOptionalParameter() {
        builder.addSingleton(Gizmo.class);

        var provider = builder.build();
        var gizmo = provider.getService(Gizmo.class);
        Assertions.assertNotNull(gizmo);
        Assertions.assertTrue(gizmo.whatsit.isEmpty());
    }

    @Test
    void testCircularDependencyThrows() {
        builder.addSingleton(Gadget.class)
                .addSingleton(Whatsit.class);

        var provider = builder.build();

        RuntimeException ex = Assertions.assertThrows(RuntimeException.class, () -> provider.getService(Gadget.class));
        Assertions.assertTrue(ex.getMessage().contains("unable to instantiate"));
    }
}
