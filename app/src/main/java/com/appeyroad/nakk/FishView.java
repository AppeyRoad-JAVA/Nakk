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
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;

class FishView extends GLSurfaceView{

    private final FishRenderer fishRenderer;
    public FishView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        fishRenderer = new FishRenderer(context);
        setRenderer(fishRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    private float prevX;
    private float prevY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        if (event.getPointerCount() == 1) {
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                float dx = (x - prevX) / getWidth() * 180.0f;
                float dy = (y - prevY) / getHeight() * 180.0f;

                float m[] = fishRenderer.getmMatrix();
                Matrix.rotateM(m, 0, dx, 1.0f, 0.0f, 0.0f);
                Matrix.rotateM(m, 0, dy, 0.0f, 1.0f, 0.0f);
                fishRenderer.setmMatrix(m);
            }
        }
        prevX = x;
        prevY = y;
        requestRender();
        return true;
    }
}
