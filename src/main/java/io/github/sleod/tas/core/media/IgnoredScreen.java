package io.github.sleod.tas.core.media;


import java.awt.*;

public class IgnoredScreen {
    private final Point location;
    private final Dimension size;

    public IgnoredScreen(Point location, Dimension size) {
        this.location = location;
        this.size = size;
    }
    public Point getLocation() {
        return location;
    }
    public Dimension getSize() {
        return size;
    }

}

