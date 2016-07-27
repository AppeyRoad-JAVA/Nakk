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
import android.view.MotionEvent;

public class BattleView extends GLSurfaceView{
    public final BattleRenderer rodRenderer;
    float mPreviousX;
    float mPreviousY;
    public BattleView(Context context){
        super(context);
        setEGLContextClientVersion(2);
        rodRenderer = new BattleRenderer(context);
        setRenderer(rodRenderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
    @Override
    public boolean onTouchEvent(MotionEvent e){
        float x = e.getX();
        float y = e.getY();
        if(e.getAction()==MotionEvent.ACTION_MOVE) {
            float dx = x - mPreviousX;
            float dy = y - mPreviousY;
            rodRenderer.setAngle(rodRenderer.getAngle() + dx);
            requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}
