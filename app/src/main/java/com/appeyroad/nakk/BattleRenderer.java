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
    public ArrayList<Model> models = new ArrayList<>();
    public ArrayList<Integer> rodIndex = new ArrayList<>();
    public ArrayList<Integer> waterIndex = new ArrayList<>();
    public ArrayList<Integer> landIndex = new ArrayList<>();
    public ArrayList<Integer> lineIndex = new ArrayList<>();

    private float width;
    private float height;
    private float zNear = 10;
    private float zFar = 3000;
    private float fovY = 60.0f;
    private float[] eyePos = {0, 0, 190, 1};
    public float[] mPMatrix = new float[16];
    public float[] mVMatrix = new float[16];
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
        GLES20.glClearColor(1, 1, 1, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        models.add(new RodModel(context));
        rodIndex.add(models.size()-1);
        models.add(new WaterModel(context));
        waterIndex.add(models.size()-1);
        models.add(new LandModel(context));
        landIndex.add(models.size()-1);
        models.add(new LineModel(context));
        lineIndex.add(models.size()-1);
    }
    @Override
    public void onDrawFrame(GL10 unused) {
        float[] mVPMatrix = new float[16];
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        eyePos[0]=-100*(float)Math.sin(mAngle * Math.PI / 180.0f);
        eyePos[1]=100*(1-(float)Math.cos(mAngle * Math.PI / 180.0f));

        Matrix.setLookAtM(mVMatrix, 0, eyePos[0], eyePos[1], eyePos[2], 0, 50, 170, eyePos[0], eyePos[1], 200);
        Model.setEyePos(eyePos);
        //Matrix.rotateM(mVMatrix, 0, mAngle, 0, 0, -1);
        Matrix.multiplyMM(mVPMatrix, 0, mPMatrix, 0, mVMatrix, 0);
        for(int i=0; i<models.size(); i++){
            models.get(i).draw(mVPMatrix);
        }
    }
    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height) {
        this.width = (float)width;
        this.height = (float)height;
        GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width/height;
        //Matrix.frustumM(mPMatrix, 0, -ratio, ratio, -1, 2, 1.5f, 3000);
        Matrix.perspectiveM(mPMatrix, 0, fovY, ratio, zNear, zFar);
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
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e("BattleRenderer", glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    public float[] convertPos(float[] pos, float level){ //화면에 입력된 2D좌표를 z = level 평면상의 3D좌표로 바꿈
        float[] result = new float[4];
        float[] inverseV = new float[16];

        float scaledHeight = zNear * (float)Math.tan((fovY/2) * (Math.PI/180));
        float[] scaled = {(2*pos[0] - width) * (scaledHeight/height) , (2*pos[1] - height) * (scaledHeight/height), -zNear, 1};

        Matrix.invertM(inverseV, 0, mVMatrix, 0);

        float[] temp = new float[4];
        Matrix.multiplyMV(temp, 0, inverseV, 0, scaled, 0);
        float k = (eyePos[2] - level) / (eyePos[2] - temp[2]);
        for(int i=0; i<3; i++){
            result[i] = (1-k)*eyePos[i] + k*temp[i];
        }
        return result;
    }
}
