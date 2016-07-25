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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class RodModel {
    private final String vertexShaderCode =
            "attribute vec4 vPosition;"+
                "attribute vec4 vColor;"+
                "varying vec4 fColor;"+
                "uniform mat4 uMVPMatrix;"+
                "void main() {" +
                "   gl_Position =  uMVPMatrix * vPosition;" +
                "   fColor = vColor;"+
                "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec4 fColor;" +
                    "void main() {" +
                    "  gl_FragColor = fColor;" +
                    "}";
    private FloatBuffer vertexBuffer;
    private FloatBuffer colorBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private static final int COORDS_PER_VERTEX= 4;
    private static float coords[];
    private static float colors[];

    public RodModel(Context context) {
        InputStream inputStream = context.getResources().openRawResource(R.raw.rod);
        try {
            InputStreamReader inputstreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputstreamReader);
            String str = bufferedReader.readLine();
            int length = Integer.parseInt(str)*COORDS_PER_VERTEX;
            coords = new float[length];
            colors = new float[length];
            for(int i=0; i<length; i+=COORDS_PER_VERTEX){
                str = bufferedReader.readLine();
                String[] words = str.split(" ");
                for(int j=0; j<COORDS_PER_VERTEX; j++){
                    if(j==COORDS_PER_VERTEX-1) {
                        coords[i+j]=1.0f;
                        colors[i+j]=0.0f;
                    }
                    else {
                        coords[i + j] = Float.parseFloat(words[j]);
                        colors[i + j] = Float.parseFloat(words[j + 3]) / 256.0f;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(coords.length*4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(coords.length * 4);
        byteBuffer2.order(ByteOrder.nativeOrder());
        colorBuffer= byteBuffer.asFloatBuffer();
        colorBuffer.put(colors);
        colorBuffer.position(0);

        int vertexShader = RodRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = RodRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, COORDS_PER_VERTEX * 4, vertexBuffer);

        mColorHandle = GLES20.glGetAttribLocation(mProgram, "vColor");
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glVertexAttribPointer(mColorHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, COORDS_PER_VERTEX * 4, colorBuffer);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        RodRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        RodRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, coords.length/COORDS_PER_VERTEX);

        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }
}
