package com.merenze.dependencyinjection.stubs;

public class Whatsit_GadgetClient {
    public Gadget_GizmoAndWhodadClient gadget;

    public Whatsit_GadgetClient(Gadget_GizmoAndWhodadClient gadget) {
        this.gadget = gadget;
    }

    public Whatsit_GadgetClient() {
    }
}
