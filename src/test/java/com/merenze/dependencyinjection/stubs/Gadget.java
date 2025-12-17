package com.merenze.dependencyinjection.stubs;

public class Gadget {
        public Gizmo gizmo;
        public Whodad whodad;

        public Gadget(Gizmo gizmo) {
            this(gizmo, null);
        }

        public Gadget(Gizmo gizmo, Whodad whodad) {
            this.gizmo = gizmo;
            this.whodad = whodad;
        }
    }