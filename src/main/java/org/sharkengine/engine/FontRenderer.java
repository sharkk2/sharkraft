package org.sharkengine.engine;

import org.lwjgl.stb.*;
import org.lwjgl.system.MemoryStack;
import org.sharkengine.Game;
import org.sharkengine.engine.classes.RGBA;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryUtil.*;

public class FontRenderer {
    private int textureId = -1;
    private final STBTTFontinfo fontInfo;
    private final ByteBuffer fontBuffer;
    private STBTTBakedChar.Buffer charData;
    private int bitmapW = 256, bitmapH = 256;
    private float lastSize = -1;
    private RGBA lastColor = null;

    public FontRenderer(String fontPath) {
        fontInfo = STBTTFontinfo.create();
        fontBuffer = Utils.ioResourceToByteBuffer(fontPath, 1024 * 1024);

        if (!stbtt_InitFont(fontInfo, fontBuffer)) {
            throw new RuntimeException("Failed to initialize font, does that font even exist?");
        }
    }

    private void generateTexture(float size, RGBA rgba) {
        if (textureId != -1) glDeleteTextures(textureId);
        bitmapW = (int)(size * 16);
        bitmapH = (int)(size * 16);

        ByteBuffer bitmap = memAlloc(bitmapW * bitmapH);
        charData = STBTTBakedChar.malloc(96);
        int result = stbtt_BakeFontBitmap(fontBuffer, size, bitmap, bitmapW, bitmapH, 32, charData);
        if (result <= 0) throw new RuntimeException("Failed to bake font");
        ByteBuffer rgbaBuffer = memAlloc(bitmapW * bitmapH * 4);
        for (int i = 0; i < bitmapW * bitmapH; i++) {
            byte alpha = bitmap.get(i);
            rgbaBuffer.put(rgba.red);
            rgbaBuffer.put(rgba.green);
            rgbaBuffer.put(rgba.blue);
            rgbaBuffer.put(alpha);
        }
        rgbaBuffer.flip();
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, bitmapW, bitmapH, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgbaBuffer);

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

        memFree(bitmap);
        memFree(rgbaBuffer);
        lastSize = size;
        lastColor = rgba;
    }

    public void addText(String text, float x, float y, float size, RGBA rgba) {
        int windowWidth = Utils.getInstance().getWidth();
        int windowHeight = Utils.getInstance().getHeight();

        float baseh = Game.height;
        float scaleFactor = windowHeight / baseh;
        float scaledSize = size * scaleFactor;

        if (lastSize != scaledSize || lastColor == null || !lastColor.equals(rgba)) {
            generateTexture(scaledSize, rgba);
        }

        float scaledX = x * scaleFactor;
        float scaledY = y * scaleFactor;

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureId);

        glColor4f(1, 1, 1, 1);

        glPushMatrix();
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, windowWidth, windowHeight, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer xBuffer = stack.mallocFloat(1);
            FloatBuffer yBuffer = stack.mallocFloat(1);
            xBuffer.put(0, scaledX);
            yBuffer.put(0, scaledY);

            for (char c : text.toCharArray()) {
                if (c < 32 || c >= 128) continue;

                STBTTAlignedQuad quad = STBTTAlignedQuad.malloc(stack);
                stbtt_GetBakedQuad(charData, bitmapW, bitmapH, c - 32, xBuffer, yBuffer, quad, true);
                renderCharQuad(quad);
            }
        }

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glPopMatrix();
    }


    private void renderCharQuad(STBTTAlignedQuad quad) {
        glBegin(GL_QUADS);
        glTexCoord2f(quad.s0(), quad.t0()); glVertex2f(quad.x0(), quad.y0());
        glTexCoord2f(quad.s1(), quad.t0()); glVertex2f(quad.x1(), quad.y0());
        glTexCoord2f(quad.s1(), quad.t1()); glVertex2f(quad.x1(), quad.y1());
        glTexCoord2f(quad.s0(), quad.t1()); glVertex2f(quad.x0(), quad.y1());
        glEnd();
    }
}
