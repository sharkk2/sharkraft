package org.sharkengine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.sharkengine.engine.AABB;
import org.sharkengine.engine.Utils;
import org.sharkengine.world.BlockRegistry;
import org.sharkengine.world.World;
import org.sharkengine.world.entities.Block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player {
    private static Player instance;

    private float cameraX, cameraY, cameraZ;
    private float cameraYaw, cameraPitch;

    private Utils utils = Utils.getInstance();
    private World world = World.getInstance();

    private float velocityX, velocityY, velocityZ;
    private boolean FLYING;
    private static final float ACCELERATION = 0.05f;
    private static final float FRICTION = 0.85f;
    private static final float MAX_SPEED = 0.1f;
    private static final float MOUSE_SENSITIVITY = 0.1f;
    private static final float PLAYER_HEIGHT = 1.75f;

    private Block lookedCube;

    private boolean resetAsFlat;
    private boolean[] keys = new boolean[GLFW.GLFW_KEY_LAST];
    public  List<Block> collidedblocks = new ArrayList<>();

    private double lastMouseX, lastMouseY;

    private boolean firstMouse = true;
    private boolean leftMousePressed = false;
    private boolean rightMousePressed = false;

    private boolean clickBreak = false;
    private boolean modeSwitchBreak = false;
    private boolean flightBreak = false;

    private int currentBlock = 1;
    private final Map<Integer, String> blocks = new HashMap<>() {{
        put(1, "voxel");
        put(2, "checkpoint");
        put(3, "death");
        put(4, "vslab");
        put(5, "slab");
        put(6, "nigga");
    }};

    private Player(long window) {
        GLFW.glfwSetCursorPosCallback(window, new GLFWCursorPosCallback() {
            @Override
            public void invoke(long window, double xpos, double ypos) {
                if (firstMouse) {
                    lastMouseX = xpos;
                    lastMouseY = ypos;
                    firstMouse = false;
                }

                float xOffset = (float) (xpos - lastMouseX);
                float yOffset = (float) (lastMouseY - ypos);

                lastMouseX = xpos;
                lastMouseY = ypos;

                xOffset *= MOUSE_SENSITIVITY;
                yOffset *= MOUSE_SENSITIVITY;

                cameraYaw -= xOffset;
                cameraPitch += yOffset;
                cameraPitch = Math.max(-89.0f, Math.min(89.0f, cameraPitch));
            }
        });

        GLFW.glfwSetKeyCallback(window, (windowHandle, key, scancode, action, mods) -> {
            if (key >= 0 && key < keys.length) {
                if (action == GLFW.GLFW_PRESS) {
                    keys[key] = true;
                } else if (action == GLFW.GLFW_RELEASE) {
                    if (key == GLFW.GLFW_KEY_E || key == GLFW.GLFW_KEY_F1) {
                        modeSwitchBreak = false;
                    } else if (key == GLFW.GLFW_KEY_F) {
                        flightBreak = false;
                    }
                    keys[key] = false;

                }
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, (windowHandle, button, action, mods) -> {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                if (action == GLFW.GLFW_PRESS) {
                    if (!leftMousePressed) {
                        leftMousePressed = true;
                    }
                } else if (action == GLFW.GLFW_RELEASE) {
                    leftMousePressed = false;
                    clickBreak = false;
                }
            }

            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                if (action == GLFW.GLFW_PRESS) {
                    if (!rightMousePressed) {
                        rightMousePressed = true;

                    }
                } else if (action == GLFW.GLFW_RELEASE) {
                    rightMousePressed = false;
                    clickBreak = false;
                }
            }
        });


    }

    public static Player getInstance(long window) {
        if (instance == null) {
            instance = new Player(window);
        }
        return instance;
    }

    public void process() {
        boolean isOnGround = utils.isBlock(Math.round(cameraX), Math.round(cameraY - 1), Math.round(cameraZ), true) ||
                utils.isBlock(Math.round(cameraX), Math.round(cameraY - PLAYER_HEIGHT), Math.round(cameraZ), true);


        cameraYaw = (cameraYaw + 360) % 360;
        float radYaw = (float) Math.toRadians(cameraYaw);
        float radPitch = (float) Math.toRadians(cameraPitch);
        float forwardX = (float) (Math.cos(radPitch) * Math.sin(radYaw));
        float forwardZ = (float) (Math.cos(radPitch) * Math.cos(radYaw));
        float rightX = (float) Math.cos(radYaw);
        float rightZ = (float) -Math.sin(radYaw);
        float moveX = 0, moveZ = 0;

        if (keys[GLFW.GLFW_KEY_W]) { moveX -= forwardX; moveZ -= forwardZ; }
        if (keys[GLFW.GLFW_KEY_S]) { moveX += forwardX; moveZ += forwardZ; }
        if (keys[GLFW.GLFW_KEY_A]) { moveX -= rightX; moveZ -= rightZ; }
        if (keys[GLFW.GLFW_KEY_D]) { moveX += rightX; moveZ += rightZ; }
        if (leftMousePressed) {
            if (!clickBreak) {
                if (lookedCube != null) {
                    lookedCube.destroy();
                    utils.clearShadowCache();
                }
                clickBreak = true;
            }

        }

        if (keys[GLFW.GLFW_KEY_1]) { currentBlock = 1; }
        if (keys[GLFW.GLFW_KEY_2]) { currentBlock = 2; }
        if (keys[GLFW.GLFW_KEY_3]) { currentBlock = 3; }
        if (keys[GLFW.GLFW_KEY_4]) { currentBlock = 4; }
        if (keys[GLFW.GLFW_KEY_5]) { currentBlock = 5; }
        if (keys[GLFW.GLFW_KEY_6]) { currentBlock = 6; }
        if (keys[GLFW.GLFW_KEY_7]) { currentBlock = 7; }

        if (rightMousePressed) {
            if (!clickBreak) {
                placeBlock();
            }
            clickBreak = true;
        }

        if (keys[GLFW.GLFW_KEY_F1]) {
            if (!modeSwitchBreak) {
                Game.toggleUI();
            }
            modeSwitchBreak = true;
        }

        if (keys[GLFW.GLFW_KEY_R]) {
            world.regenerate(resetAsFlat);
            resetCamera();
        }

        if (keys[GLFW.GLFW_KEY_E]) {
            if (!modeSwitchBreak) {
                Game.setWireFrame(!Game.getWireFrame());
            }
            modeSwitchBreak = true;
        }

        if (keys[GLFW.GLFW_KEY_F]) {
            if (!flightBreak) {
                setFlying(!getFlying());
            }
            flightBreak = true;
        }

        if (cameraY < -25) { resetCamera(); }

        if (FLYING) {
            if (keys[GLFW.GLFW_KEY_SPACE]) { cameraY += 0.23f; }
            if (keys[GLFW.GLFW_KEY_LEFT_CONTROL]) { cameraY -= 0.5f; }
        } else if (isOnGround && keys[GLFW.GLFW_KEY_SPACE]) {
            velocityY = 0.53f;
        }

        float length = (float) Math.sqrt(moveX * moveX + moveZ * moveZ);
        if (length > 0) {
            moveX /= length;
            moveZ /= length;
        }

        velocityX = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, (velocityX + moveX * ACCELERATION) * FRICTION));
        velocityZ = Math.max(-MAX_SPEED, Math.min(MAX_SPEED, (velocityZ + moveZ * ACCELERATION) * FRICTION));

        if (!FLYING) {
            if (!isOnGround) {
                velocityY = Math.max(velocityY - 0.1f, -MAX_SPEED);
            } else if (velocityY < 0) {
                velocityY = 0;
            }
        }

        if (!collides(cameraX + velocityX, cameraY, cameraZ)) {
            cameraX += velocityX;
        } else {
            velocityX = 0;
        }

        if (!collides(cameraX, cameraY + velocityY, cameraZ)) {
            cameraY += velocityY;
        } else {
            velocityY = 0;
        }

        if (!collides(cameraX, cameraY, cameraZ + velocityZ)) {
            cameraZ += velocityZ;
        } else {
            velocityZ = 0;
        }

    }

    private void placeBlock() {
        if (lookedCube != null) {
            Integer face = lookedCube.rayHitFace;
            int[][] offsets = {
                    {-1,  0,  0},
                    { 1,  0,  0},
                    { 0, -1,  0},
                    { 0,  1,  0},
                    { 0,  0, -1},
                    { 0,  0,  1}
            };

            if (face != null) {
                if (face < 0 || face >= offsets.length) {
                    System.out.println("Unknown face hit: " + face);
                    return;
                }

                String blockid = blocks.getOrDefault(currentBlock, "voxel");

                int[] offset = offsets[face];
                Block block = BlockRegistry.getInstance().makeBlock(blockid, lookedCube.x + offset[0], lookedCube.y + offset[1], lookedCube.z + offset[2]);
                if (block.x == Math.round(cameraX) && block.z == Math.round(cameraZ)) {
                    if (block.y == Math.round(cameraY) || block.y == Math.round(cameraY) - 1) {
                        return;
                    }
                }
                if (keys[GLFW.GLFW_KEY_B]) {
                    System.out.println("Bouncing block added");
                    block.bounce(0.01f, 0.02f);
                }
                if (!block.isDirectionFixed) {
                    if (getPlayerDirection().equals("WEST") || getPlayerDirection().equals("EAST")) {
                        float depth = block.depth;
                        float width = block.width;
                        block.setWidth(depth);
                        block.setDepth(width);
                    }
                }
                world.blocks.add(block);
                utils.clearShadowCache();
            }
        }
    }

    private void resetCamera() {
        cameraX = 5;
        cameraY = 20;
        cameraZ = 5;
        velocityX = velocityY = velocityZ = 0;
    }

    private boolean collides(float x, float y, float z) { // fuck ts shit
        AABB playerBox = new AABB(x, y - 1, z, 0.8f, PLAYER_HEIGHT, 0.8f);
        List<Block> nCollided = new ArrayList<>();
        boolean didCollide = false;
        int minX = (int)Math.floor(playerBox.minX) - 1;
        int maxX = (int)Math.floor(playerBox.maxX) + 1;
        int minY = (int)Math.floor(playerBox.minY) - 1;
        int maxY = (int)Math.floor(playerBox.maxY) + 1;
        int minZ = (int)Math.floor(playerBox.minZ) - 1;
        int maxZ = (int)Math.floor(playerBox.maxZ) + 1;
        for (int bx = minX; bx <= maxX; bx++) {
            for (int by = minY; by <= maxY; by++) {
                for (int bz = minZ; bz <= maxZ; bz++) {
                    if (utils.isBlock(bx, by, bz, true)) {
                        Block block = world.getBlock(bx, by, bz);
                        if (block == null) continue;

                        AABB blockBox = new AABB(
                                block.x + block.getOffsetX(),
                                block.y + block.getOffsetY(),
                                block.z + block.getOffsetZ(),
                                block.width,
                                block.height,
                                block.depth
                        );

                        if (playerBox.intersects(blockBox)) {
                            nCollided.add(block);
                            didCollide = true;
                        }
                    }
                }
            }
        }
        collidedblocks.clear();
        collidedblocks.addAll(nCollided);
        return didCollide;
    }

    public void setCameraPos(float x, float y, float z) { cameraX = x; cameraY = y; cameraZ = z; }
    public void setCameraAngle(float yaw, float pitch) { cameraYaw = yaw; cameraPitch = pitch; }
    public void setFlying(boolean flying) { FLYING = flying; }
    public boolean getFlying() { return FLYING; }
    public void setResetAsFlat(boolean isFlat) { resetAsFlat = isFlat; }
    public void setLookedBlock(Block block) { lookedCube = block; }
    public float getCameraX() { return cameraX; }
    public float getCameraY() { return cameraY; }
    public float getCameraZ() { return cameraZ; }
    public float getCameraYaw() { return cameraYaw; }
    public float getCameraPitch() { return cameraPitch; }
    public String getCurrentBlockID() {return blocks.getOrDefault(currentBlock, "voxel");} // i need to rewrite every single line of code in this game
    public String getPlayerDirection() {
        if (cameraYaw <= 180) {
            if (cameraYaw >= 90) {
                if (cameraYaw >= 135) {
                    return "NORTH";
                } else {
                    return "EAST";
                }
            }
        }
        if (cameraYaw >= 180) {
            if (cameraYaw <= 270) {
                if (cameraYaw > 225) {
                    return "WEST";
                } else {
                    return "NORTH";
                }
            }
        }
        if (cameraYaw >= 270) {
            if (cameraYaw <= 360) {
               if (cameraYaw > 315) {
                   return "SOUTH";
               } else {
                   return "WEST";
               }
            }
        }
        if (cameraYaw <= 90) {
            if (cameraYaw > 45) {
                return "EAST";
            } else {
                return "SOUTH";
            }
        }
        return "UNKNOWN";
    }
}