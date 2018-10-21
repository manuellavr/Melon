/*
 * Copyright (c) 2018.
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

package com.forcetower.uefs.core.storage.repository

import android.content.Context
import android.content.SharedPreferences
import com.crashlytics.android.Crashlytics
import com.forcetower.uefs.core.model.unes.Message
import com.forcetower.uefs.core.storage.database.UDatabase
import com.forcetower.uefs.service.NotificationCreator
import com.google.firebase.messaging.RemoteMessage
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseMessageRepository @Inject constructor(
    private val database: UDatabase,
    private val preferences: SharedPreferences,
    private val context: Context
) {
    fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        when {
            data != null -> onDataMessageReceived(data)
            message.notification != null -> onSimpleMessageReceived(message)
            else -> Timber.d("An invalid message was received")
        }
    }

    private fun onDataMessageReceived(data: Map<String, String>) {
        when (data["identifier"]) {
            "event" -> eventNotification(data)
            "teacher" -> teacherNotification(data)
            "remote_database" -> promoteDatabase(data)
            "service" -> serviceNotification(data)
            null -> Crashlytics.log("Invalid notification received. No Identifier.")
        }
    }

    private fun teacherNotification(data: Map<String, String>) {
        val message = data["message"]
        val teacher = data["teacher"]
        val timestamp = data["timestamp"]

        if (message == null || teacher == null || timestamp == null) {
            Crashlytics.log("Invalid notification received. No message, teacher or timestamp")
            return
        }

        val sent = timestamp.toLongOrNull()
        if (sent == null) {
            Crashlytics.log("Invalid notification received. Send time is invalid. Teacher: $teacher, $message")
            return
        }

        val default = Message(content = message, sagresId = System.currentTimeMillis(), notified = true, senderName = teacher, senderProfile = -2, timestamp = sent)
        val uid = database.messageDao().insert(default)
        NotificationCreator.showSagresMessageNotification(default, context, uid)
    }

    private fun serviceNotification(data: Map<String, String>) {
        val title = data["title"]
        val message = data["message"]
        val image = data["image"]

        if (title == null || message == null) {
            Crashlytics.log("Bad notification created. It was ignored")
            return
        }

        NotificationCreator.showServiceMessageNotification(context, message.hashCode().toLong(), title, message, image)
    }

    private fun eventNotification(data: Map<String, String>) {
        val id = data["eventId"]
        val title = data["title"]
        val description = data["description"]
        val image = data["image"]

        if (id == null || title == null || description == null) {
            Crashlytics.log("Bad notification created. It was ignored")
            return
        }

        NotificationCreator.showEventNotification(context, id, title, description, image)
    }

    //This call is by far the most dangerous call that the hole code may have
    //Call allows the server to perform an update in the database at any moment
    //It will also be extremely helpful when something wrong happens and the server will be able to fix everyone at once
    private fun promoteDatabase(data: Map<String, String>) {
        val query = data["query"]
        val unique = data["unique"]

        if (unique != null) {
            val executed = preferences.getBoolean(unique, false)
            if (executed) {
                Timber.d("Promotion dismissed")
                return
            }
        }

        try {
            database.openHelper.writableDatabase.execSQL(query)
            if (unique != null)
                preferences.edit().putBoolean(unique, true).apply()
        } catch (t: Throwable) {
            Timber.d("Failed executing database promotion. ${t.message}")
            Crashlytics.logException(t)
        }

    }

    private fun onSimpleMessageReceived(message: RemoteMessage) {
        val notification = message.notification
        if (notification == null) {
            Crashlytics.log("Invalidation of notification happened really quickly")
            return
        }

        val content = notification.body
        val title = notification.title

        if (content == null || title == null) {
            Crashlytics.log("Bad notification created. It was ignored")
            return
        }

        NotificationCreator.showSimpleNotification(context, title, content)


    }
}