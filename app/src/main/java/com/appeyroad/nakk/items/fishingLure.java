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

package com.appeyroad.nakk.items;

import com.appeyroad.nakk.item;

import java.util.ArrayList;

public class fishingLure extends item {
    ArrayList<Integer> grade;       //잡을 수 있는 물고기 등급
    float attraction;               //추가효과 - 배틀 시작 시까지 대기시간 감소(비율)
}
