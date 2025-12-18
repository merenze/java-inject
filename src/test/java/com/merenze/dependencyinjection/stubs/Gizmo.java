package com.merenze.dependencyinjection.stubs;

import java.util.Optional;

public class Gizmo {
    public Optional<Whatsit_GadgetClient> whatsit;

    public Gizmo(Optional<Whatsit_GadgetClient> whatsit) {
        this.whatsit = whatsit;
    }

    public Gizmo() {
        this.whatsit = Optional.empty();
    }
}