package org.sharkengine;

import java.awt.*;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.sharkengine.engine.*;
import org.sharkengine.world.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;
import static org.lwjgl.system.MemoryUtil.*;


public class Game {
    public long window;

    private FontRenderer fontRenderer;
    private Player player;
    private final Renderer render = new Renderer();
    private final World world = World.getInstance();
    private final BlockRegistry blockRegistry = BlockRegistry.getInstance();

    private int chunkDisplayList;

    // ### SETTINGS ###
    public final boolean FLYING = false;
    public final boolean flat_world = true;
    public static boolean wireFrame = false;
    private static boolean viewUi = true;
    public static final int width = 800;
    public static final int height = 600;
    private float cameraX = 5, cameraY = 20, cameraZ = 5;
    private float cameraYaw = 0, cameraPitch = -60;
    private final String fontPath = "assets/font.ttf";
    public float FOV = 80f;

    public void init() {
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

        this.window = glfwCreateWindow(width, height, "Sharkraft | Initializing", NULL, NULL);
        if (this.window == NULL) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        Utils utils = Utils.getInstance();
        player = Player.getInstance(window);
        glfwMakeContextCurrent(this.window);
        glfwShowWindow(this.window);
        glfwSetInputMode(this.window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        utils.setWindow(this.window);
        utils.setDim(this.width, this.height);
        utils.setFov(this.FOV);

        GL.createCapabilities();
        String version = GL11.glGetString(GL11.GL_VERSION);
        String renderer = GL11.glGetString(GL11.GL_RENDERER);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        System.out.println("OpenGL Version: " + version + "\n" + renderer + "\nResolution: " + width + "x" + height + " (" + screenSize.width + "x" + screenSize.height + ")\n----------------------------------------");
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        fontRenderer = new FontRenderer(fontPath);

        glfwWindowHint(GLFW_SAMPLES, 4);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(FOV, 800.0f / 600.0f, 0.1f, 100);
        glMatrixMode(GL_MODELVIEW);

        glfwSetFramebufferSizeCallback(window, (w, width, height) -> {
            glViewport(0, 0, width, height);
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            gluPerspective(utils.getFov(), (float) width / (float) height, 0.1f, 100);
            glMatrixMode(GL_MODELVIEW);
            utils.setDim(width, height);
        });

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);
        glEnable(GL_COLOR_MATERIAL);

        float[] lightPosition = {0, 10, 0, 1};
        float[] lightColor = {1, 1, 1, 1};

        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
        glLightfv(GL_LIGHT0, GL_DIFFUSE, lightColor);
        glLightfv(GL_LIGHT0, GL_SPECULAR, lightColor);

        chunkDisplayList = glGenLists(1);
        glNewList(chunkDisplayList, GL_COMPILE);
        glEndList();

        blockRegistry.registerBlock("voxel", "assets/voxel.png", 1, 1, 1, false, false);
        blockRegistry.registerBlock("checkpoint", "assets/checkpoint.png", 1, 1, 1, true, false);
        blockRegistry.registerBlock("death", "assets/death.png", 1, 1, 1, true, false);
        blockRegistry.registerBlock("vslab", "assets/motion.png", 1, 1, 0.3f, true, false);
        blockRegistry.registerBlock("slab", "assets/motion.png", 1, 0.5f, 1, true, true);


        System.out.println("Generating world...");
        world.generateWorld(flat_world);
        System.out.println("Loading player...");
        player.setCameraPos(cameraX, cameraY, cameraZ);
        player.setFlying(FLYING);
        player.setResetAsFlat(flat_world);
        player.setCameraAngle(cameraYaw, cameraPitch);
        System.out.println("Loaded :D");
    }

    private void gluPerspective(float fov, float aspect, float zNear, float zFar) {
        float ymax = zNear * (float) Math.tan(Math.toRadians(fov / 2));
        float xmax = ymax * aspect;
        glFrustum(-xmax, xmax, -ymax, ymax, zNear, zFar);
    }

    private void processPlayer() {
        player.process();
        cameraX = player.getCameraX();
        cameraY = player.getCameraY();
        cameraZ = player.getCameraZ();
        cameraYaw = player.getCameraYaw();
        cameraPitch = player.getCameraPitch();
    }


    public void loop() {
        while (!glfwWindowShouldClose(this.window)) {
            render.render(fontRenderer, chunkDisplayList, window, cameraX, cameraY, cameraZ, cameraPitch, cameraYaw);
            processPlayer();
            glfwSwapBuffers(this.window);
            glfwPollEvents();
        }
    }

    public void cleanup() {
        glfwDestroyWindow(this.window);
        glfwTerminate();
    }

    public static void setWireFrame(boolean state) {wireFrame = state;}
    public static boolean getWireFrame() {return wireFrame;}
    public static void toggleUI() {viewUi = !viewUi;}
    public static boolean isUIEnabled() {return viewUi;}

    public static void main(String[] args) {
        System.out.println("Initializing...");
        Game game = new Game();
        game.init();
        game.loop();
        game.cleanup();
    }
}