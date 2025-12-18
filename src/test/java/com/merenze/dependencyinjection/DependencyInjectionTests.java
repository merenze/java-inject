package com.merenze.dependencyinjection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.merenze.dependencyinjection.stubs.*;

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
                .addSingleton(Whodad_ThingamabobClient.class)
                .addSingleton(Gizmo.class)
                .addSingleton(Gadget_GizmoAndWhodadClient.class);

        var provider = builder.build();

        var gadget = provider.getService(Gadget_GizmoAndWhodadClient.class);
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
        builder.addSingleton(CircularDependency.class);
        var provider = builder.build();

        Assertions.assertThrows(RuntimeException.class, () -> provider.getService(CircularDependency.class));
    }
}
