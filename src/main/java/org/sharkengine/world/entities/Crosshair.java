package org.sharkengine.world.entities;

import org.sharkengine.engine.Utils;

import static org.lwjgl.opengl.GL11.*;

public class Crosshair {
    private final Utils utils = Utils.getInstance();
    public Crosshair() {}

    public void render(boolean cross) {
        glDisable(GL_DEPTH_TEST);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        float width = utils.getWidth();
        float height = utils.getHeight();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glColor3f(1, 1, 1);
        if (cross) {
            glLineWidth(2);
            glBegin(GL_LINES);
            glVertex2f(width / 2 - 10, height / 2); // TODO: fix and find out why the fuck is it not symmetrical in fullscreen
            glVertex2f(width / 2 + 10, height / 2);
            glVertex2f(width / 2, height / 2 - 9);
            glVertex2f(width / 2, height / 2 + 10);
        } else {
            glPointSize(5);
            glBegin(GL_POINTS);
            glVertex2f(width / 2, height / 2);
        }
        glEnd();
        glPopMatrix();
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);

        glEnable(GL_DEPTH_TEST);
    }
}
