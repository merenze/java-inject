package com.merenze.dependencyinjection.stubs;

import java.util.Optional;

public class Gizmo {
    public Optional<Whatsit> whatsit;

    public Gizmo(Optional<Whatsit> whatsit) {
        this.whatsit = whatsit;
    }

    public Gizmo() {
        this.whatsit = Optional.empty();
    }
}