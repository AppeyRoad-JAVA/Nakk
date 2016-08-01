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
import android.util.AttributeSet;

public class BattleView extends GLSurfaceView{
    public final BattleRenderer renderer;
    private Context context;
    public BattleView(Context context, AttributeSet attrs){
        super(context, attrs);
        this.context=context;
        setEGLContextClientVersion(2);
        renderer = new BattleRenderer(context);
        setRenderer(renderer);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void drawLine(float[] pos){
        WaterModel waterModel = (WaterModel)renderer.models.get(renderer.waterIndex.get(0));
        LandModel landModel = (LandModel)renderer.models.get(renderer.landIndex.get(0));
        float[] waterPos = renderer.convertPos(pos, waterModel.waterLevel); //waterModel에 사영된 좌표
        float[] landPos = renderer.convertPos(pos, landModel.landLevel);    //landModel에 사영된 좌표
        if(landPos[0]>=landModel.left && landPos[0]<=landModel.right && landPos[1]>=landModel.back && landPos[1]<=landModel.front){
            return;
        }
        if(!(waterPos[0]>=waterModel.left && waterPos[0]<=waterModel.right && waterPos[1]>=waterModel.back && waterPos[1]<=waterModel.front)){
            return;
        }

        RodModel rodModel =  (RodModel)renderer.models.get(renderer.rodIndex.get(0));
        float[] rodEnd = rodModel.rodEnd;
        float theta = (float)(Math.atan2((double)(waterPos[1]-rodEnd[1]), (double)(rodEnd[2]-waterPos[2])) * 180 / Math.PI);
        float phi = (float)(Math.atan2((double)(waterPos[0]-rodEnd[0]),
                (double)(Matrix.length(0, waterPos[1]-rodEnd[1], rodEnd[2]-waterPos[2]))) * 180 / Math.PI);
        float[] axis = {0, rodEnd[2]-waterPos[2], waterPos[1]-rodEnd[1], 0};
        float[] mMMatrix = new float[16];

        Matrix.setIdentityM(mMMatrix, 0);
        Matrix.translateM(mMMatrix, 0, rodEnd[0], rodEnd[1], rodEnd[2]);
        Matrix.rotateM(mMMatrix, 0, -phi, axis[0], axis[1], axis[2]);
        Matrix.rotateM(mMMatrix, 0, theta, 1, 0, 0);
        Matrix.translateM(mMMatrix, 0, -rodEnd[0], -rodEnd[1], -rodEnd[2]);
        renderer.models.get(renderer.lineIndex.get(0)).mMMatrix =mMMatrix;
        requestRender();
    }
}
