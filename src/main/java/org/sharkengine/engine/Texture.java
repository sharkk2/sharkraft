package org.sharkengine.engine;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class Texture {

    public int loadTexture(String texPath) {
        int textureID = 0;

        ByteBuffer imageBuffer;
        try {
            imageBuffer = Utils.ioResourceToByteBuffer(texPath, 8 * 1024);
        } catch (Exception e) {
            System.err.println("Failed to load texture file! does that texture even exist?" + texPath);
            return -1;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer image = STBImage.stbi_load_from_memory(imageBuffer, width, height, channels, 4);
            if (image == null) {
                System.err.println("STBImage failed to decode image: " + texPath);
                return -1;
            }

            textureID = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, textureID);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(0), height.get(0),
                    0, GL_RGBA, GL_UNSIGNED_BYTE, image);

            glGenerateMipmap(GL_TEXTURE_2D);

            STBImage.stbi_image_free(image);
        }

        return textureID;
    }
}
