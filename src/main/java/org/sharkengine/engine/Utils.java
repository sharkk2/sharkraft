package org.sharkengine.engine;

import org.lwjgl.system.MemoryUtil;
import org.sharkengine.world.entities.Block;
import org.sharkengine.world.World;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.glFrustum;

public class Utils {
    private static final Utils INSTANCE = new Utils();
    private static final World world = World.getInstance();
    private final Map<String, Boolean> shadowCache = new HashMap<>();
    private long window;
    private int width;
    private int height;
    private float FOV;

    private Utils() {}

    public static Utils getInstance() {
        return INSTANCE;
    }


    public void setFov(float fov) {FOV = fov;}
    public float getFov() {return FOV;}
    public void setWindow(long win) {window = win;}
    public long getWindow() {return window;}
    public void setDim(int w, int h) {width = w; height = h;}
    public int getWidth() {return width;}
    public int getHeight() {return height;}

    public boolean isBlock(float x, float y, float z, boolean fixedDimensions) {
        for (Block block : world.blocks) {
            if (block.x == x && block.y == y && block.z == z) {
                if (block.depth != 1 || block.height != 1 || block.width != 1) {
                    if (fixedDimensions) { return true; } else { return false; } // its flipped but kinda works idk how
                }
                return true;
            }
        }
        return false;
    }

    public Block fetchBlock(float x, float y, float z) {
        for (Block block : world.blocks) {
            if (block.x == x && block.y == y && block.z == z) {
                return block;
            }
        }
        return null;
    }

    public boolean isSurrounded(float x, float y, float z) {
        if (
                isBlock(x+1, y, z, false) &&
                        isBlock(x, y+1, z, false) &&
                        isBlock(x, y, z+1, false) &&
                        isBlock(x-1, y, z, false) &&
                        isBlock(x, y-1, z, false) &&
                        isBlock(x, y, z-1, false)
        ) {
            return true;
        }
        return false;
    }

    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) {
        try {
            ByteBuffer buffer;
            java.nio.file.Path path = Paths.get(resource);

            if (Files.isReadable(path)) {
                try (FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
                    buffer = MemoryUtil.memAlloc((int) fc.size() + 1);
                    while (fc.read(buffer) != -1);
                }
            } else {
                try (InputStream source = Utils.class.getClassLoader().getResourceAsStream(resource)) {
                    if (source == null) {
                        throw new IOException("Resource not found: " + resource);
                    }
                    byte[] bytes = source.readAllBytes();
                    buffer = MemoryUtil.memAlloc(bytes.length + 1);
                    buffer.put(bytes);
                }
            }
            buffer.flip();
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException("Failed to load resource: " + resource, e);
        }
    }

    public boolean shouldRenderFace(Block currentBlock, float opX, float opY, float opZ) {
        Block oppBlock = fetchBlock(opX, opY, opZ);
        if (oppBlock == null) return true;

        if (Math.abs(currentBlock.x - oppBlock.x) == 1 && currentBlock.y == oppBlock.y && currentBlock.z == oppBlock.z) {
            if (currentBlock.height == oppBlock.height && currentBlock.depth == oppBlock.depth) {
                if (currentBlock.width == 1 && oppBlock.width == 1) return false;
                if (currentBlock.width == oppBlock.width && oppBlock.isFullSized()) return false;
            }
        }

        if (Math.abs(currentBlock.y - oppBlock.y) == 1 && currentBlock.x == oppBlock.x && currentBlock.z == oppBlock.z) {
            if (currentBlock.width == oppBlock.width && currentBlock.depth == oppBlock.depth) {
                if (currentBlock.height == 1 && oppBlock.height == 1) return false;
                if (currentBlock.height == oppBlock.height && oppBlock.isFullSized()) return false;
            }
        }

        if (Math.abs(currentBlock.z - oppBlock.z) == 1 && currentBlock.x == oppBlock.x && currentBlock.y == oppBlock.y) {
            if (currentBlock.width == oppBlock.width && currentBlock.height == oppBlock.height) {
                if (currentBlock.depth == 1 && oppBlock.depth == 1) return false;
                if (currentBlock.depth == oppBlock.depth && oppBlock.isFullSized()) return false;
            }
        }
        return true;
    }



    public boolean inShadowCache(Block block) {
        String key = block.x + "," + block.y + "," + block.z;
        if (shadowCache.containsKey(key)) return shadowCache.get(key);

        if (isBlock(block.x, block.y + 1, block.z, false)) {
            return false;
        }

        for (int i = (int)block.y + 1; i < block.y + 16; i++) {
            if (isBlock(block.x, i, block.z, true)) {
                shadowCache.put(key, true);
                return true;
            }
        }

        shadowCache.put(key, false);
        return false;
    }

    public void clearShadowCache() {shadowCache.clear();}
}
