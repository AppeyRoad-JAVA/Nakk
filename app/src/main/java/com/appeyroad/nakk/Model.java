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

public class Model {
    private final String vertexShaderCode =
            "attribute vec4 aPosition;" +
                    "attribute vec2 aTexCoord;"+
                    "attribute vec3 aNorCoord;"+

                    "uniform mat4 uMVPMatrix;"+

                    "varying vec2 vTexCoord;"+
                    "varying vec3 vNorCoord;"+
                    "varying vec4 vPosition;"+
                    "void main() {" +
                    "   gl_Position =  uMVPMatrix * aPosition;"+
                    "   vTexCoord = aTexCoord;"+
                    "   vNorCoord = aNorCoord;"+
                    "   vPosition = aPosition;"+
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uDiff;"+
                    "uniform sampler2D uSpec;"+
                    "uniform vec3 uLight;"+
                    "uniform vec4 uEyePos;"+

                    "varying vec2 vTexCoord;"+
                    "varying vec3 vNorCoord;"+
                    "varying vec4 vPosition;"+
                    "void main() {" +
                    "   vec3 n_uLight = normalize(uLight);"+
                    "   vec3 n_vNorCoord = normalize(vNorCoord);"+
                    "   vec3 n_viewer = normalize(vec3(uEyePos-vPosition));"+
                    "   vec3 n_halfway = normalize(n_uLight + n_viewer);"+

                    "   vec4 diffColor = max(dot(n_uLight, n_vNorCoord), 0) * texture2D(uDiff, vTexCoord);"+
                    "   vec4 specColor = pow(max(dot(n_halfway, n_vNorCoord), 0), 20) * texture2D(uSpec, vTexCoord);"+
                    "   gl_FragColor = diffColor+specColor;"+
                    "}";
    private Context context;

    private FloatBuffer vertexBuffer;
    private FloatBuffer uvBuffer;
    private FloatBuffer norCoordsBuffer;

    private int mPositionHandle;
    private int mUvHandle;
    private int mNorCoordsHandle;
    private int mDiffMapHandle;
    private int mDiffMapDataHandle;
    private int mSpecMapHandle;
    private int mSpecMapDataHandle;
    private int mLightHandle;
    private int mEyePosHandle;
    private int mMVPMatrixHandle;
    private final int mProgram;


    private static final int COORDS_PER_VERTEX = 4;
    private static final int TEXCOORDS_PER_VERTEX = 2;
    private static final int NORCOORDS_PER_VERTEX = 3;
    private float coords[];
    private float uv[];
    private float norCoords[];
    private float light[] = {1, 1, 1};  //햇빛이므로 모든 점으로 일정한 방향으로 일정한 세기로 내리쬔다고 가정
    public static float eyePos[] = new float[4];

    public Model(Context context, int vertResourceId, int diffMapResourceId, int specMapResourceId) {
        this.context=context;
        InputStream inputStream = context.getResources().openRawResource(vertResourceId);
        try {
            InputStreamReader inputstreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputstreamReader);
            String str = bufferedReader.readLine();
            int nVertices = Integer.parseInt(str);
            coords = new float[nVertices * COORDS_PER_VERTEX];
            uv = new float[nVertices * TEXCOORDS_PER_VERTEX];
            norCoords = new float[nVertices * NORCOORDS_PER_VERTEX];
            for (int i = 0; i < nVertices; i++) {
                str = bufferedReader.readLine();
                String[] words = str.split(" ");
                for (int j = 0; j < COORDS_PER_VERTEX; j++) {
                    if (j == COORDS_PER_VERTEX - 1) {
                        coords[i * COORDS_PER_VERTEX + j] = 1.0f;
                    } else {
                        coords[i * COORDS_PER_VERTEX + j] = Float.parseFloat(words[j]);
                    }
                }
                if(words.length >= COORDS_PER_VERTEX + TEXCOORDS_PER_VERTEX) {
                    for (int j = 0; j < TEXCOORDS_PER_VERTEX; j++) {
                       uv[i * TEXCOORDS_PER_VERTEX + j] =
                                Float.parseFloat(words[j + COORDS_PER_VERTEX - 1]);
                    }
                }
                if(words.length >= COORDS_PER_VERTEX + TEXCOORDS_PER_VERTEX + NORCOORDS_PER_VERTEX) {
                    for (int j = 0; j < NORCOORDS_PER_VERTEX; j++) {
                        norCoords[i * NORCOORDS_PER_VERTEX + j] =
                                Float.parseFloat(words[j + COORDS_PER_VERTEX + TEXCOORDS_PER_VERTEX]);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(coords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(uv.length * 4);
        byteBuffer2.order(ByteOrder.nativeOrder());
        uvBuffer = byteBuffer2.asFloatBuffer();
        uvBuffer.put(uv);
        uvBuffer.position(0);

        ByteBuffer byteBuffer3 = ByteBuffer.allocateDirect(norCoords.length * 4);
        byteBuffer3.order(ByteOrder.nativeOrder());
        norCoordsBuffer = byteBuffer3.asFloatBuffer();
        norCoordsBuffer.put(norCoords);
        norCoordsBuffer.position(0);

        mDiffMapDataHandle = BattleRenderer.loadTexture(context, diffMapResourceId);
        mSpecMapDataHandle = BattleRenderer.loadTexture(context, specMapResourceId);

        int vertexShader = BattleRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = BattleRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram);

        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "aPosition");
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, COORDS_PER_VERTEX * 4, vertexBuffer);

        mDiffMapHandle = GLES20.glGetUniformLocation(mProgram, "uDiff");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mDiffMapDataHandle);
        GLES20.glUniform1i(mDiffMapHandle, 0);

        mSpecMapHandle = GLES20.glGetUniformLocation(mProgram, "uSpec");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSpecMapDataHandle);
        GLES20.glUniform1i(mSpecMapHandle, 1);

        mUvHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mUvHandle);
        GLES20.glVertexAttribPointer(mUvHandle, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, TEXCOORDS_PER_VERTEX*4, uvBuffer);

        mNorCoordsHandle = GLES20.glGetAttribLocation(mProgram, "aNorCoord");
        GLES20.glEnableVertexAttribArray(mNorCoordsHandle);
        GLES20.glVertexAttribPointer(mNorCoordsHandle, NORCOORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, NORCOORDS_PER_VERTEX*4, norCoordsBuffer);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        BattleRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        BattleRenderer.checkGlError("glUniformMatrix4fv");

        mLightHandle = GLES20.glGetUniformLocation(mProgram, "uLight");
        GLES20.glUniform3fv(mLightHandle, 1, light, 0);
        mEyePosHandle = GLES20.glGetUniformLocation(mProgram, "uEyePos");
        GLES20.glUniform4fv(mEyePosHandle, 1, eyePos, 0);


        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, coords.length / COORDS_PER_VERTEX);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mUvHandle);
        GLES20.glDisableVertexAttribArray(mNorCoordsHandle);
    }
}

class RodModel extends Model{
    public RodModel(Context context){
        super(context, R.raw.rod2, R.drawable.rod2_diff, R.drawable.rod2_specular);
    }
}
class WaterModel extends Model{
    public WaterModel(Context context){
        super(context, R.raw.water, R.drawable.water_deep, R.drawable.black);
    }
}
class LandModel extends Model{
    public LandModel(Context context){
        super(context, R.raw.land, R.drawable.land, R.drawable.black);
    }
}
