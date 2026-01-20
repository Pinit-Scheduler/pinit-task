package me.gg.pinit.pinittask.domain.dependency.model;

import java.util.ArrayList;
import java.util.List;

public class GraphUtils {
    public static List<Dependency> getDependenciesSample() {
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.add(new Dependency(1L, 3L));
        dependencies.add(new Dependency(2L, 3L));
        dependencies.add(new Dependency(3L, 4L));
        dependencies.add(new Dependency(3L, 5L));
        return dependencies;
    }
}
