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

package com.forcetower.uefs.core.storage.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.IGNORE
import androidx.room.Query
import androidx.room.Transaction
import com.forcetower.uefs.core.model.unes.Discipline

@Dao
abstract class DisciplineDao {
    @Insert(onConflict = IGNORE)
    abstract fun insert(discipline: Discipline)

    @Transaction
    open fun insert(disciplines: List<Discipline>) {
        disciplines.forEach { discipline ->
            val old = getDisciplineByCodeDirect(discipline.code)
            if (old == null) {
                insert(discipline)
            } else {
                if (old.credits == 0 || discipline.credits > 0) {
                    updateDiscipline(old.uid, discipline.credits, discipline.name)
                }
            }
        }
    }

    @Query("UPDATE Discipline SET credits = :credits, name = :name WHERE uid = :uid")
    abstract fun updateDiscipline(uid: Long, credits: Int, name: String)

    @Query("SELECT * FROM Discipline WHERE LOWER(code) = LOWER(:code) LIMIT 1")
    abstract fun getDisciplineByCodeDirect(code: String): Discipline?
}