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
            "attribute vec4 vPosition;" +
                    "attribute vec2 aTexCoord;" +
                    "varying vec2 vTexCoord;" +
                    "uniform mat4 uMVPMatrix;" +
                    "void main() {" +
                    "   gl_Position =  uMVPMatrix * vPosition;" +
                    "   vTexCoord=aTexCoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform sampler2D uTexture;" +
                    "varying vec2 vTexCoord;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture, vTexCoord);" +
                    "}";
    private Context context;

    private FloatBuffer vertexBuffer;
    private FloatBuffer texCoordsBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mMVPMatrixHandle;
    private int mTextureHandle;
    private int mTexCoordsHandle;
    private int mTextureDataHandle;


    private static final int COORDS_PER_VERTEX = 4;
    private static final int TEXCOORDS_PER_VERTEX = 2;
    private float coords[];
    private float texCoords[];

    public Model(Context context, int vertResourceId, int texResourceId) {
        this.context=context;
        InputStream inputStream = context.getResources().openRawResource(vertResourceId);
        try {
            InputStreamReader inputstreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputstreamReader);
            String str = bufferedReader.readLine();
            int nVertices = Integer.parseInt(str);
            coords = new float[nVertices * COORDS_PER_VERTEX];
            texCoords = new float[nVertices * TEXCOORDS_PER_VERTEX];
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
                for(int j=0; j<TEXCOORDS_PER_VERTEX; j++){
                    texCoords[i*TEXCOORDS_PER_VERTEX + j] =
                            Float.parseFloat(words[j+COORDS_PER_VERTEX-1]);
                }
                //texCoords[i*TEXCOORDS_PER_VERTEX] = Float.parseFloat(words[COORDS_PER_VERTEX-1]);
                //texCoords[i*TEXCOORDS_PER_VERTEX+1] =1.0f- Float.parseFloat(words[COORDS_PER_VERTEX]);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(coords.length * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        vertexBuffer = byteBuffer.asFloatBuffer();
        vertexBuffer.put(coords);
        vertexBuffer.position(0);

        ByteBuffer byteBuffer2 = ByteBuffer.allocateDirect(texCoords.length * 4);
        byteBuffer2.order(ByteOrder.nativeOrder());
        texCoordsBuffer = byteBuffer2.asFloatBuffer();
        texCoordsBuffer.put(texCoords);
        texCoordsBuffer.position(0);

        mTextureDataHandle = BattleRenderer.loadTexture(context, texResourceId);
        int vertexShader = BattleRenderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = BattleRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
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

        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "uTexture");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
        GLES20.glUniform1i(mTextureHandle, 0);

        mTexCoordsHandle = GLES20.glGetAttribLocation(mProgram, "aTexCoord");
        GLES20.glEnableVertexAttribArray(mTexCoordsHandle);
        GLES20.glVertexAttribPointer(mTexCoordsHandle, TEXCOORDS_PER_VERTEX, GLES20.GL_FLOAT,
                false, TEXCOORDS_PER_VERTEX*4, texCoordsBuffer);

        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        BattleRenderer.checkGlError("glGetUniformLocation");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        BattleRenderer.checkGlError("glUniformMatrix4fv");

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, coords.length / COORDS_PER_VERTEX);
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordsHandle);
    }
}

class RodModel extends Model{
    public RodModel(Context context){
        super(context, R.raw.rod2, R.drawable.rod2);
    }
}
class WaterModel extends Model{
    public WaterModel(Context context){
        super(context, R.raw.water, R.drawable.water_deep);
    }
}
class LandModel extends Model{
    public LandModel(Context context){
        super(context, R.raw.land, R.drawable.land);
    }
}
