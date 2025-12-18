package com.merenze.dependencyinjection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.merenze.dependencyinjection.stubs.*;

import java.util.List;

class MultiRegistrationTests {

    private ServiceBuilder builder;

    @BeforeEach
    void setup() {
        builder = new ServiceBuilder();
    }

    @Test
    void testGetServicesReturnsEmptyIfNoneRegistered() {
        var provider = builder.build();
        List<Gadget_GizmoAndWhodadClient> gadgets = provider.getServices(Gadget_GizmoAndWhodadClient.class);
        Assertions.assertNotNull(gadgets);
        Assertions.assertTrue(gadgets.isEmpty(), "Expected empty list when no service is registered");
    }

    @Test
    void testGetServicesReturnsAllRegisteredInstancesInOrder() {
        var gadgets = List.of(new Gadget_GizmoAndWhodadClient(new Gizmo()), new Gadget_GizmoAndWhodadClient(new Gizmo()), new Gadget_GizmoAndWhodadClient(new Gizmo()));

        gadgets.forEach(gadget -> builder.addSingleton(Gadget_GizmoAndWhodadClient.class, gadget));

        var provider = builder.build();

        var result = provider.getServices(Gadget_GizmoAndWhodadClient.class);

        Assertions.assertEquals(gadgets.size(), result.size());

        for (var i = 0; i < gadgets.size(); i++) {
            Assertions.assertSame(gadgets.get(i), result.get(i));
        }

        Assertions.assertSame(gadgets.get(gadgets.size() - 1), provider.getRequiredService(Gadget_GizmoAndWhodadClient.class));
    }

    @Test
    void testGetServiceAndGetRequiredServiceReturnLastRegistered() {
        builder.addSingleton(Gadget_GizmoAndWhodadClient.class, new Gadget_GizmoAndWhodadClient(new Gizmo()))
                .addSingleton(Gadget_GizmoAndWhodadClient.class, new Gadget_GizmoAndWhodadClient(new Gizmo(), new Whodad_ThingamabobClient()));

        var provider = builder.build();

        Gadget_GizmoAndWhodadClient last = provider.getService(Gadget_GizmoAndWhodadClient.class);
        Assertions.assertNotNull(last);
        Assertions.assertNotNull(last.gizmo);
        Assertions.assertNotNull(last.whodad, "getService should return the last registered instance with whodad");

        Gadget_GizmoAndWhodadClient required = provider.getRequiredService(Gadget_GizmoAndWhodadClient.class);
        Assertions.assertNotNull(required);
        Assertions.assertNotNull(required.gizmo);
        Assertions.assertNotNull(required.whodad,
                "getRequiredService should return the last registered instance with whodad");
    }
}
