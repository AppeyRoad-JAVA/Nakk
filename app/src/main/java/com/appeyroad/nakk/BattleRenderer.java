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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.util.Log;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BattleRenderer implements GLSurfaceView.Renderer {
    private Context context;
    private ArrayList<Model> models = new ArrayList<>();

    private float[] mMVPMatrix = new float[16];
    private float[] mPMatrix = new float[16];
    private float[] mVMatrix = new float[16];
    private float[] mMMatrix = new float[16];
    private float mAngle;

    public BattleRenderer(Context context){
        this.context = context;
    }
    public float getAngle() {
        return mAngle;
    }
    public void setAngle(float angle) {
        mAngle = angle;
    }
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        //GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        models.add(new RodModel(context));
        models.add(new WaterModel(context));
        models.add(new LandModel(context));
        Matrix.setIdentityM(mMMatrix, 0);
    }
    @Override
    public void onDrawFrame(GL10 unused) {
        float[] mVPMatrix = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        //Matrix.setLookAtM(mVMatrix, 0, -200, 190, 0, -190, 180, 0, -180, 190, 0); //옆에서 바라보는 시점
        Matrix.setLookAtM(mVMatrix, 0, 0, 190, 30, 0, 180, 10, 0, 200, 0);
        Matrix.multiplyMM(mVPMatrix, 0, mPMatrix, 0, mVMatrix, 0);
        Matrix.setRotateM(mMMatrix, 0, mAngle, 0, 1, 0);
        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, mMMatrix, 0);
        for(int i=0; i<models.size(); i++){
            models.get(i).draw(mMVPMatrix);
        }
    }
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width/height;
        Matrix.frustumM(mPMatrix, 0, -ratio, ratio, -1, 2, 1.5f, 3000);
    }

    public static int loadShader(int type, String shaderCode){
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
    public static int loadTexture(Context context, int resourceId){
        int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);
        if(textureHandle[0]!=0){
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled=false;
            final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId,
                    options);
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                    GLES20.GL_NEAREST);
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();
        }
        if(textureHandle[0]==0){
            throw new RuntimeException("Error loading texture.");
        }
        return textureHandle[0];
    }
    public static void checkGlError(String glOperation) {
        int error;
        if ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("RodRenderer", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }
}
