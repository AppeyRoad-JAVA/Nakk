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

public class fishingBoat extends item {
    int grade;          //낚싯배 분류(나룻배, 보트, 원양어선)
    float bonusHp;      //추가효과 - user 체력 증가
    float bonusMoney;   //추가효과 - 버는 돈 증가(비율)
    float bonusExp;     //추가효과 - 버는 경험치 증가(비율)
    float attraction;   //추가효과 - 배틀 시작 시까지 대기시간 감소(비율)
}
