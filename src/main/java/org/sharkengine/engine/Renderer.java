package org.sharkengine.engine;

import org.sharkengine.Game;
import org.sharkengine.Player;
import org.sharkengine.engine.classes.RGBA;
import org.sharkengine.world.entities.Block;
import org.sharkengine.world.World;
import org.sharkengine.world.entities.Crosshair;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;


public class Renderer {
    private final Utils utils = Utils.getInstance();
    private final long lastTime = System.nanoTime();
    private final World world = World.getInstance();
    private final Frustum frustum = new Frustum();
    private final Crosshair crosshair = new Crosshair();
    private Player player;
    private long lastFPSUpdate = System.nanoTime();
    private int fps = 0;
    private int displayedFPS = 0;
    private int[] lasthit;
    private int renderedBlocks = 0;

    private void updateWindowTitle(long window, float cameraX, float cameraY, float cameraZ, float cameraPitch, float cameraYaw) {
        String title = "Sharkraft engine | X: " + cameraX + " Y: " + cameraY + " Z: " + cameraZ + " | p" + Math.round(cameraPitch) + ",y" + Math.round(cameraYaw) + " | Rendered cubes: " + renderedBlocks;
        glfwSetWindowTitle(window, title);
        renderedBlocks = 0;
    }

    private String getFPS() {
        long currentTime = System.nanoTime();
        fps++;

        if ((currentTime - lastFPSUpdate) >= 1_000_000_000L) { // 1 second
            displayedFPS = fps;
            fps = 0;
            lastFPSUpdate = currentTime;
        }
        return String.valueOf(displayedFPS);
    }

    public void renderWorld(long window) {
        player = Player.getInstance(window);
        // culling updaters!!!!

         frustum.updateFrustum(
                utils.getFov(),
                (float) utils.getWidth() / utils.getHeight(),
                0.1f, 70.0f,
                player.getCameraX(),
                player.getCameraY(),
                player.getCameraZ(),
                player.getCameraPitch(),
                player.getCameraYaw()
        );

        for (Block block : world.blocks) {
            boolean inFrustum = frustum.BlockInFrustum(block.x, block.y, block.z, Math.max(block.width, Math.max(block.height, block.depth)) / 2);
            if (inFrustum) {
                if (utils.isSurrounded(block.x, block.y, block.z)) { continue; }
                if (utils.inShadowCache(block)) {
                    if (!block.lit) {
                        block.shadow(true);
                    }
                } else {
                    block.shadow(false);
                }
                block.render();
                renderedBlocks++;
            }
            if (block.isBouncing) {
                block.bounce(1, 2);
            }
        }


    }


    public void render(FontRenderer fontRenderer, int chunkDisplayList, long window, float cameraX, float cameraY, float cameraZ, float cameraPitch, float cameraYaw) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        player = Player.getInstance(window);
        // rendering
        if (Game.getWireFrame()) {
            glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
        }

        glLoadIdentity();
        glRotatef(-cameraPitch, 1, 0, 0);
        glRotatef(-cameraYaw, 0, 1, 0);
        glTranslatef(-cameraX, -cameraY, -cameraZ);
        renderWorld(window);
        crosshair.render(Game.isUIEnabled());
        RGBA skyColor = new RGBA(0, 0, 0.5f, 1);
        glClearColor(skyColor.red, skyColor.green, skyColor.blue, skyColor.alpha);
        glCallList(chunkDisplayList);
        updateWindowTitle(window, cameraX, cameraY, cameraZ, cameraPitch, cameraYaw);
        String direction = player.getPlayerDirection();

        // UI
        if (Game.isUIEnabled()) {
            fontRenderer.addText("SharkrAft! 1.4b", 10, 24, 24, new RGBA(211, 10, 255, 1));
            fontRenderer.addText("FPS: " + getFPS(), 10, 47, 24, new RGBA(255, 255, 255, 1));
            fontRenderer.addText("X: " + Math.round(cameraX) + ", Y: " + Math.round(cameraY) + ", Z: " + Math.round(cameraZ), 10, 70, 24, new RGBA(255, 255, 255, 1));
            fontRenderer.addText("Selected block: " + player.getCurrentBlockID(), 10, 585, 24, new RGBA(255, 255, 255, 1));

            StringBuilder bString = new StringBuilder();
            for (Block block : player.collidedblocks) {
                bString.append(block.id);
                bString.append(" ");
            }
            fontRenderer.addText("a shArkk2 project - facing: " + direction + " ( colls: " + bString + ")", 10, 597, 10, new RGBA(188, 188, 188, 1));
        }

        // raycasting check
        int[] hit =  Raycast.castRay(cameraX, cameraY, cameraZ, cameraYaw, cameraPitch);
        if (hit != null) {
            if (lasthit != null && hit != lasthit) {
                Block lblock = world.getBlock(lasthit[0], lasthit[1], lasthit[2]);
                if (lblock != null) {
                    lblock.highlight(false);
                    lblock.setHitFace(null);
                }
            }
            Block block = world.getBlock(hit[0], hit[1], hit[2]);
            if (block != null) {
                block.highlight(true);
                lasthit = hit;
                if (hit[3] != 69) {
                    block.setHitFace(hit[3]);
                }
                player.setLookedBlock(block);

            }
        } else {
            if (lasthit != null) {
                Block lblock = world.getBlock(lasthit[0], lasthit[1], lasthit[2]);
                if (lblock != null) {
                    lblock.highlight(false);
                    lblock.setHitFace(null);
                    player.setLookedBlock(null);

                }
            }
        }
    }
}