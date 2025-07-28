package org.sharkengine.world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.sharkengine.engine.PerlinNoise;
import org.sharkengine.engine.Renderer;
import org.sharkengine.engine.Utils;
import org.sharkengine.world.entities.Block;

public class World {
    private static World instance;
    private final Random random = new Random();
    public final List<Block> blocks = new ArrayList<>();
    private Utils utils = Utils.getInstance();
    private PerlinNoise noise;


    public final int chunkSize = 8;
    public int chunksWidth = 2;
    public int chunksDepth = 2;
    private int baseHeight = 6;
    private int boxHeight = 5;

    public static World getInstance() {
        if (instance == null) {
            instance = new World();
        }
        return instance;
    }

    public Block getBlock(float x, float y, float z) {
        for (Block block : blocks) {
            if (block.x == x && block.y == y && block.z == z) {
                return block;
            }
        }
        return null;
    }

    public void generateWorld(boolean flat) {
        int width = chunksWidth * chunkSize;
        int depth = chunksDepth * chunkSize;
        long seed = random.nextLong();
        random.setSeed(seed);
        blocks.clear();
        utils.clearShadowCache();

        if (!flat) {
            noise = new PerlinNoise(seed);
            for (int chunkX = 0; chunkX < chunksWidth; chunkX++) {
                for (int chunkZ = 0; chunkZ < chunksDepth; chunkZ++) {
                    for (int x = 0; x < chunkSize; x++) {
                        for (int z = 0; z < chunkSize; z++) {
                            int worldX = chunkX * chunkSize + x;
                            int worldZ = chunkZ * chunkSize + z;

                            double heightVariation = flat ? 0 : noise.noise(worldX * 0.08, worldZ * 0.08) * 8;
                            int surfaceHeight = baseHeight + (int) heightVariation;

                            for (int y = baseHeight - boxHeight; y <= surfaceHeight; y++) {
                                Block block;
                                if (y == surfaceHeight) {
                                    block = BlockRegistry.getInstance().makeBlock("voxel", worldX, y, worldZ);
                                } else if (y >= surfaceHeight - 3) {
                                    block = BlockRegistry.getInstance().makeBlock("voxel", worldX, y, worldZ);
                                } else {
                                    block = BlockRegistry.getInstance().makeBlock("death", worldX, y, worldZ);
                                }

                                blocks.add(block);
                            }
                        }
                    }
                }
            }

            System.out.println("Generated " + blocks.size() + " Blocks with seed: " + seed);
        } else {
            width = 12;
            depth = 8;

            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    int worldX = x;
                    int worldZ = z;

                    for (int y = 0; y < 5; y++) {
                        Block vox = BlockRegistry.getInstance().makeBlock("voxel", worldX, y, worldZ);
                        blocks.add(vox);
                    }
                }
            }

            System.out.println("Generated " + blocks.size() + " Blocks");
        }
    }

    public void setChunkSize(int chunksWidth, int chunksDepth) {
        this.chunksWidth = chunksWidth;
        this.chunksDepth = chunksDepth;
    }

    public void regenerate(boolean flat) {
        generateWorld(flat);
    }
    public int getWorldWidth() {
        return chunksWidth * chunkSize;
    }
    public int getWorldDepth() {
        return chunksDepth * chunkSize;
    }
    public int getWorldHeight() {
        return boxHeight + baseHeight;
    }


}