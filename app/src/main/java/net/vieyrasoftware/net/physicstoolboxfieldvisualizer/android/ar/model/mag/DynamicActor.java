package net.vieyrasoftware.net.physicstoolboxfieldvisualizer.android.ar.model.mag;

import java.util.List;

public interface DynamicActor extends Actor {
    boolean apply(List<? extends Field> f);
}
