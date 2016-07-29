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

import com.appeyroad.nakk.items.fish;
import java.util.ArrayList;

public class place {
    int grade;                      //장소 분류(개울, 강, 근해, 원해)
    ArrayList<fish> fishes;         //잡히는 물고기 풀
    ArrayList<Float> probability;   //각 물고기가 나오는 확률
}
