/*
 * Copyright (c) 2019.
 * João Paulo Sena <joaopaulo761@gmail.com>
 *
 * This file is part of the UNES Open Source Project.
 *
 * UNES is licensed under the MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.forcetower.uefs.core.injection.module

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.room.Room
import com.forcetower.uefs.GooglePlayGamesInstance
import com.forcetower.uefs.UApplication
import com.forcetower.uefs.core.storage.apidatabase.APIDatabase
import com.forcetower.uefs.core.storage.database.UDatabase
import com.forcetower.uefs.core.storage.database.M1TO2
import com.forcetower.uefs.core.storage.database.M2TO3
import com.forcetower.uefs.core.storage.database.M3TO4
import com.forcetower.uefs.core.storage.database.M5TO6
import com.forcetower.uefs.core.storage.database.M6TO7
import com.forcetower.uefs.core.storage.database.M7TO8
import com.forcetower.uefs.core.storage.database.M8TO9
import com.forcetower.uefs.core.storage.database.M9TO10
import com.forcetower.uefs.core.storage.database.M10TO11
import com.forcetower.uefs.core.storage.database.M11TO12
import com.forcetower.uefs.core.storage.database.M12TO13
import com.forcetower.uefs.core.storage.database.M13TOM14
import com.forcetower.uefs.core.storage.database.M14TOM15
import com.forcetower.uefs.core.storage.database.M15TOM16
import com.forcetower.uefs.core.storage.database.M16TOM17
import com.forcetower.uefs.core.storage.eventdatabase.EventDatabase
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @Singleton
    @JvmStatic
    fun provideContext(application: UApplication): Context =
            application.applicationContext

    @Provides
    @Singleton
    @JvmStatic
    fun provideSharedPreferences(context: Context): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(context)

    @Provides
    @Singleton
    @JvmStatic
    fun provideDatabase(context: Context): UDatabase =
            Room.databaseBuilder(context.applicationContext, UDatabase::class.java, "unesco.db")
                .addMigrations(M1TO2, M2TO3, M3TO4, M5TO6, M6TO7, M7TO8, M8TO9, M9TO10, M10TO11, M11TO12,
                        M12TO13, M13TOM14, M14TOM15, M15TOM16, M16TOM17)
                .fallbackToDestructiveMigration()
                .build()

    @Provides
    @Singleton
    @JvmStatic
    fun provideApiDatabase(context: Context): APIDatabase =
            Room.databaseBuilder(context.applicationContext, APIDatabase::class.java, "unesglass.db")
                .fallbackToDestructiveMigration()
                .build()

    @Provides
    @Singleton
    @JvmStatic
    fun provideEventDatabase(context: Context): EventDatabase =
            Room.databaseBuilder(context.applicationContext, EventDatabase::class.java, "unevents.db")
                    .fallbackToDestructiveMigration()
                    .build()

    @Provides
    @Singleton
    @JvmStatic
    fun providePlayGames(context: Context): GooglePlayGamesInstance =
            GooglePlayGamesInstance(context)
}