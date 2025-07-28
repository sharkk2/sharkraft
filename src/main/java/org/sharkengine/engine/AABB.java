package org.sharkengine.engine;

public class AABB {
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;

    public AABB(float x, float y, float z, float width, float height, float depth) {
        this.minX = x;
        this.minY = y;
        this.minZ = z;
        this.maxX = x + width;
        this.maxY = y + height;
        this.maxZ = z + depth;
    }

    public boolean intersects(AABB other) {
        return this.maxX > other.minX && this.minX < other.maxX &&
                this.maxY > other.minY && this.minY < other.maxY &&
                this.maxZ > other.minZ && this.minZ < other.maxZ;
    }
}
