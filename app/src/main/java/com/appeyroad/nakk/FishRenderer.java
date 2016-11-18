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
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class FishRenderer implements GLSurfaceView.Renderer{

    FishRenderer(Context context) {
        this.context = context;
    }

    private Context context;
    private FishModel fishModel;

    float[] getmMatrix() {
        return mMatrix;
    }

    void setmMatrix(float[] mMatrix) {
        this.mMatrix = mMatrix;
    }

    private float[] mMatrix = new float[16];
    private float[] vMatrix = new float[16];
    private float[] pMatrix = new float[16];

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        fishModel = new FishModel(context);
        Matrix.setIdentityM(mMatrix, 0);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);
        float ratio = i / i1;
        Matrix.perspectiveM(pMatrix, 0, 45.0f, ratio, 1f, 100f);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        Matrix.setLookAtM(vMatrix, 0, 0, 0, -30, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        fishModel.draw(mMatrix, vMatrix, pMatrix);
    }
}
