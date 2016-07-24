/*
 * Copyright (c) 2016  AppeyRoad-JAVA team
 *
 * This file is part of Nakk.
 *
 * Nakk is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Nakk is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Nakk.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.appeyroad.nakk;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

class FishModel {
    private final String vertexShaderSource =
            "uniform mat4 mMatrix;" +
            "uniform mat4 vMatrix;" +
            "uniform mat4 pMatrix;" +
            "attribute vec4 vPosition;" +
            "attribute vec4 vColor;" +
            "varying vec4 fColor;" +
            "void main() {" +
            "    gl_Position = pMatrix * vMatrix * mMatrix * vPosition;" +
            "    fColor = vColor;" +
            "}";
    private final String fragmentShaderSource =
            "precision mediump float;" +
            "varying vec4 fColor;" +
            "void main() {" +
            "    gl_FragColor = fColor;" +
            "}";
    private final int program;
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;

    private static float coords[];
    private static float colors[];

    private int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    private void checkGlError(String glOperation) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("FishRenderer", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    FishModel(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.fishingrod);
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String string = bufferedReader.readLine();
            int length = Integer.parseInt(string)*4;
            coords = new float[length];
            colors = new float[length];
            int i = 0;
            while ((string = bufferedReader.readLine()) != null) {
                String[] words = string.split(" ");
                coords[i] = Float.parseFloat(words[0]);
                coords[i+1] = Float.parseFloat(words[1]);
                coords[i+2] = Float.parseFloat(words[2]);
                coords[i+3] = 1.0f;
                colors[i] = Float.parseFloat(words[3])/256.0f;
                colors[i+1] = Float.parseFloat(words[4])/256.0f;
                colors[i+2] = Float.parseFloat(words[5])/256.0f;
                colors[i+3] = 1.0f;
                i += 4;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(coords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(coords.length * 4);
        byteBuffer2.order(ByteOrder.nativeOrder());
        colorBuffer= byteBuffer2.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
    }

    void draw(float[] mMatrix, float[] vMatrix, float[] pMatrix) {
        GLES20.glUseProgram(program);

        int v_pos = GLES20.glGetAttribLocation(program, "vPosition");
        GLES20.glEnableVertexAttribArray(v_pos);
        GLES20.glVertexAttribPointer(v_pos, 4,GLES20.GL_FLOAT, false, 16, vertexBuffer);

        int v_col = GLES20.glGetAttribLocation(program, "vColor");
        GLES20.glEnableVertexAttribArray(v_col);
        GLES20.glVertexAttribPointer(v_col, 4,GLES20.GL_FLOAT, false, 16, colorBuffer);

        int m_mat = GLES20.glGetUniformLocation(program, "mMatrix");
        checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(m_mat, 1, false, mMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        int v_mat = GLES20.glGetUniformLocation(program, "vMatrix");
        checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(v_mat, 1, false, vMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        int p_mat = GLES20.glGetUniformLocation(program, "pMatrix");
        checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(p_mat, 1, false, pMatrix, 0);
        checkGlError("glUniformMatrix4fv");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, coords.length / 4);

        GLES20.glDisableVertexAttribArray(v_pos);
    }
}
