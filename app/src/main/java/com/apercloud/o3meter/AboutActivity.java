/*
 *   This file is part of O₃METER.
 *
 *   O₃METER is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   O₃METER is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with O₃METER.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.apercloud.o3meter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Activity for loading about text.
 *
 * This activity is used to display the license and author description.
 *
 * @author AperCloud
 * @version 2018.0226
 */

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
