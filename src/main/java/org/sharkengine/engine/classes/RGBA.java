package org.sharkengine.engine.classes;

public class RGBA {
    public byte red;
    public byte green;
    public byte blue;
    public byte alpha;

    public RGBA(float red, float green, float blue, float alpha) {
        this.red = (byte)red;
        this.green = (byte)green;
        this.blue = (byte)blue;
        this.alpha = (byte)alpha;
    }
}
