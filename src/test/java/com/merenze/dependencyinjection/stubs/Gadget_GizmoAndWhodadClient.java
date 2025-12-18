package com.merenze.dependencyinjection.stubs;

public class Gadget_GizmoAndWhodadClient {
        public Gizmo gizmo;
        public Whodad_ThingamabobClient whodad;

        public Gadget_GizmoAndWhodadClient(Gizmo gizmo) {
            this(gizmo, null);
        }

        public Gadget_GizmoAndWhodadClient(Gizmo gizmo, Whodad_ThingamabobClient whodad) {
            this.gizmo = gizmo;
            this.whodad = whodad;
        }
    }