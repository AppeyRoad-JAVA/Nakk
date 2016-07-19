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
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
public class Line extends View {
    Paint paint = new Paint();
    int fromX, fromY;
    int toX, toY;
    public Line(Context context, int color, int fromX, int fromY, int toX, int toY){
        super(context);
        this.fromX=fromX; this.fromY=fromY; this.toX=toX;   this.toY=toY;
        paint.setColor(color);
    }

    @Override
    public void onDraw(Canvas canvas){
        canvas.drawLine(fromX, fromY, toX, toY, paint);
    }
    public void cutX(int x0){    //x=x0 로 자르기
        toY = (x0-fromX)*(toY-fromY)/(toX-fromX) + fromY;
        toX = x0;
    }
    public void cutY(int y0){    //y=y0 로 자르기
        toX = (y0-fromY)*(toX-fromX)/(toY-fromY) + fromX;
        toY = y0;
    }
}