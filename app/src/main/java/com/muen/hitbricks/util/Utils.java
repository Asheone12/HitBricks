package com.muen.hitbricks.util;

import android.opengl.GLES20;

import java.nio.ByteBuffer;

/**
 * A handful of utility functions.
 */
public class Utils {

    /**
     * Creates a texture from raw data.
     *
     * @param data   Image data.
     * @param width  Texture width, in pixels (not bytes).
     * @param height Texture height, in pixels.
     * @param format Image data format (use constant appropriate for glTexImage2D(), e.g. GL_RGBA).
     * @return Handle to texture.
     */
    public static int createImageTexture(ByteBuffer data, int width, int height, int format) {
        int[] textureHandles = new int[1];
        int textureHandle;

        GLES20.glGenTextures(1, textureHandles, 0);
        textureHandle = textureHandles[0];
        Utils.checkGlError("glGenTextures");

        // Bind the texture handle to the 2D texture target.
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle);

        // Configure min/mag filtering, i.e. what scaling method do we use if what we're rendering
        // is smaller or larger than the source image.
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        Utils.checkGlError("loadImageTexture");

        // Load the data from the buffer into the texture handle.
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, /*level*/ 0, format,
                width, height, /*border*/ 0, format, GLES20.GL_UNSIGNED_BYTE, data);
        Utils.checkGlError("loadImageTexture");

        return textureHandle;
    }

    /**
     * Loads a shader from a string and compiles it.
     *
     * @param type       GL shader type, e.g. GLES20.GL_VERTEX_SHADER.
     * @param shaderCode Shader source code.
     * @return Handle to shader.
     */
    public static int loadShader(int type, String shaderCode) {
        int shaderHandle = GLES20.glCreateShader(type);

        GLES20.glShaderSource(shaderHandle, shaderCode);
        GLES20.glCompileShader(shaderHandle);

        // Check for failure.
        int[] compileStatus = new int[1];
        GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
        if (compileStatus[0] != GLES20.GL_TRUE) {
            // Extract the detailed failure message.
            String msg = GLES20.glGetShaderInfoLog(shaderHandle);
            GLES20.glDeleteProgram(shaderHandle);
            throw new RuntimeException("glCompileShader failed");
        }

        return shaderHandle;
    }

    /**
     * Creates a program, given source code for vertex and fragment shaders.
     *
     * @param vertexShaderCode   Source code for vertex shader.
     * @param fragmentShaderCode Source code for fragment shader.
     * @return Handle to program.
     */
    public static int createProgram(String vertexShaderCode, String fragmentShaderCode) {
        // Load the shaders.
        int vertexShader = Utils.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = Utils.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        // Build the program.
        int programHandle = GLES20.glCreateProgram();
        GLES20.glAttachShader(programHandle, vertexShader);
        GLES20.glAttachShader(programHandle, fragmentShader);
        GLES20.glLinkProgram(programHandle);

        // Check for failure.
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            // Extract the detailed failure message.
            String msg = GLES20.glGetProgramInfoLog(programHandle);
            GLES20.glDeleteProgram(programHandle);
            throw new RuntimeException("glLinkProgram failed");
        }

        return programHandle;
    }

    /**
     * Utility method for checking for OpenGL errors.  Use like this:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     * <p>
     * If an error was detected, this will throw an exception.
     *
     * @param msg string to display in the error message (usually the name of the last
     *            GL operation)
     */
    public static void checkGlError(String msg) {
        int error, lastError = GLES20.GL_NO_ERROR;

        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            lastError = error;
        }
        if (lastError != GLES20.GL_NO_ERROR) {
            throw new RuntimeException(msg + ": glError " + lastError);
        }
    }
}
