package org.sharkengine.world.entities;

import org.joml.Vector3f;
import org.sharkengine.engine.*;
import org.sharkengine.world.World;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class Block {
    public float x, y, z;
    public float width, height, depth;
    public String texturePath;
    public int textureID;
    public boolean lit;
    public String id;
    public boolean highlighted = false;
    private Utils utils = Utils.getInstance();
    private World world = World.getInstance();
    public Integer rayHitFace;
    public boolean isBouncing = false;
    private float bounceOffset;
    private float bounceSpeed;
    public boolean isRotating = false;
    private float rotationSpeed;
    private float rotationY = 0;
    private boolean shadowed;
    public boolean isDirectionFixed;

    public Block(float x, float y, float z, float width, float height, float depth, String texturePath, String id, boolean lit, boolean isDirectionFixed) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.width = width;
        this.height = height;
        this.depth = depth;
        this.texturePath = texturePath;
        this.id = id;
        this.lit = lit;
        this.isDirectionFixed = isDirectionFixed;

        Texture texture = new Texture();
        this.textureID = texture.loadTexture(texturePath);

    }

    public void destroy() {
        world.blocks.remove(this);
    }

    public void bounce(float offset, float speed) {
        this.bounceOffset = offset;
        this.bounceSpeed = speed;
        this.isBouncing = true;

        float time = (float) (System.nanoTime() / 1_000_000_000.0);
        this.y += Math.sin(time * speed/10) * offset/10; // TODO: FIX BOUNCING
    }


    public void highlight(boolean highlight) {
        highlighted = highlight;
    }
    public void shadow(boolean shadow) { shadowed = shadow; }

    public void setHitFace(Integer face) { rayHitFace = face; }
    public void setX(float nx) { x = nx; }
    public void setY(float ny) { y = ny; }
    public void setZ(float nz) { z = nz; }
    public void setWidth(float width) {this.width = width;}
    public void setHeight(float height) {this.height = height;}
    public void setDepth(float depth) {this.depth = depth;}
    public float getOffsetX() {return (1 - width) / 2;}
    public float getOffsetY() {return (1 - height) / 2;}
    public float getOffsetZ() {return (1 - depth) / 2;}


    public void render() {
        if (textureID == -1) {
            return;
        }

        glEnable(GL_MULTISAMPLE);

        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);

        glEnable(GL_LIGHTING);
        glEnable(GL_LIGHT0);

        float[] lightPosition = {0, 50, 0, 1};


        glLightfv(GL_LIGHT0, GL_POSITION, lightPosition);
        glLightfv(GL_LIGHT0, GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1});
        glLightfv(GL_LIGHT0, GL_DIFFUSE, new float[]{1, 1, 1, 1});
        glLightfv(GL_LIGHT0, GL_SPECULAR, new float[]{1, 1, 1, 1});

        float[] materialAmbient = {0.1f, 0.1f, 0.1f, 1};
        float[] materialDiffuse = {0.8f, 0.8f, 0.8f, 1};
        float[] materialSpecular = {1, 1, 1, 1};
        float shininess = 32;

        glMaterialfv(GL_FRONT, GL_AMBIENT, materialAmbient);
        glMaterialfv(GL_FRONT, GL_DIFFUSE, materialDiffuse);
        glMaterialfv(GL_FRONT, GL_SPECULAR, materialSpecular);
        glMaterialf(GL_FRONT, GL_SHININESS, shininess);

        if (highlighted) {
            glColor3f(10.5f, 10.5f, 10.5f);
        } else {
            if (lit) {
                glColor3f(10.5f, 10.5f, 10.5f);
            } else {
                glColor3f(0.85f, 0.85f, 0.85f);
            }
        }


        glBegin(GL_QUADS);

        // front
        if (utils.shouldRenderFace(this, x, y, z + 1)) {
            glNormal3f(0, 0, 1);
            glTexCoord2f(1, 1);
            glVertex3f(x - width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(0, 1);
            glVertex3f(x + width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(0, 0);
            glVertex3f(x + width / 2, y + height / 2, z + depth / 2);
            glTexCoord2f(1, 0);
            glVertex3f(x - width / 2, y + height / 2, z + depth / 2);
        }
        // back
        if (utils.shouldRenderFace(this, x, y, z - 1)) {
            glNormal3f(0, 0, -1);
            glTexCoord2f(0, 1);
            glVertex3f(x - width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(0, 0);
            glVertex3f(x - width / 2, y + height / 2, z - depth / 2);
            glTexCoord2f(1, 0);
            glVertex3f(x + width / 2, y + height / 2, z - depth / 2);
            glTexCoord2f(1, 1);
            glVertex3f(x + width / 2, y - height / 2, z - depth / 2);
        }

        // left
        if (utils.shouldRenderFace(this, x - 1, y, z)) {
            glNormal3f(-1, 0, 0);
            glTexCoord2f(0, 1);
            glVertex3f(x - width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(1, 1);
            glVertex3f(x - width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(1, 0);
            glVertex3f(x - width / 2, y + height / 2, z + depth / 2);
            glTexCoord2f(0, 0);
            glVertex3f(x - width / 2, y + height / 2, z - depth / 2);
        }

        //right
        if (utils.shouldRenderFace(this, x + 1, y, z)) {
            glNormal3f(1, 0, 0);
            glTexCoord2f(0, 1);
            glVertex3f(x + width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(1, 1);
            glVertex3f(x + width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(1, 0);
            glVertex3f(x + width / 2, y + height / 2, z + depth / 2);
            glTexCoord2f(0, 0);
            glVertex3f(x + width / 2, y + height / 2, z - depth / 2);
        }

        //top
        if (utils.shouldRenderFace(this, x, y + 1, z)) {
            if (shadowed) {
                glColor3f(0.4f, 0.4f, 0.4f);
            } else {
                glColor3f(0.85f, 0.85f, 0.85f);
            }

            glNormal3f(0, 1, 0);
            glTexCoord2f(0, 0);
            glVertex3f(x - width / 2, y + height / 2, z - depth / 2);
            glTexCoord2f(1, 0);
            glVertex3f(x + width / 2, y + height / 2, z - depth / 2);
            glTexCoord2f(1, 1);
            glVertex3f(x + width / 2, y + height / 2, z + depth / 2);
            glTexCoord2f(0, 1);
            glVertex3f(x - width / 2, y + height / 2, z + depth / 2);

            glColor3f(highlighted ? 10.5f : 1, highlighted ? 10.5f : 1, highlighted ? 10.5f : 1);
        }
        // Bottom face
        if (utils.shouldRenderFace(this, x, y - 1, z)) {
            glNormal3f(0, -1, 0);
            glTexCoord2f(0, 0);
            glVertex3f(x - width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(1, 0);
            glVertex3f(x + width / 2, y - height / 2, z - depth / 2);
            glTexCoord2f(1, 1);
            glVertex3f(x + width / 2, y - height / 2, z + depth / 2);
            glTexCoord2f(0, 1);
            glVertex3f(x - width / 2, y - height / 2, z + depth / 2);
        }
        glEnd();
        glPopMatrix();

        glDisable(GL_LIGHTING);
        glDisable(GL_TEXTURE_2D);

        glDisable(GL_MULTISAMPLE);

    }

    public boolean isFullSized() {
        return width == 1 && height == 1 && depth == 1;
    }
}
