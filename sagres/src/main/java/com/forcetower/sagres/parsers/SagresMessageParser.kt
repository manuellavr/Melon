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

package com.forcetower.sagres.parsers

import com.forcetower.sagres.database.model.SMessage
import org.jsoup.nodes.Document
import java.util.ArrayList

/**
 * Created by João Paulo on 06/03/2018.
 */

object SagresMessageParser {
    private const val MESSAGE_CLASS_RECEIVED = "class=\"recado-escopo\">"
    private const val MESSAGE_DATE_RECEIVED = "class=\"recado-data\">"
    private const val MESSAGE_MESSAGE_RECEIVED = "class=\"recado-texto\">"
    private const val MESSAGE_FROM_RECEIVED = "class=\"recado-remetente\">"

    @JvmStatic
    fun getMessages(html: String): List<SMessage> {
        val messages = ArrayList<SMessage>()

        var position = 0
        var found = html.indexOf("<article id", position) != -1

        while (found) {
            val start = html.indexOf("<article id", position)
            val end = html.indexOf("</article>", start)

            if (start == -1)
                return messages

            found = true

            val article = html.substring(start, end)

            val message = extractInfoArticle(article)
            if (message != null) messages.add(message)
            position = end
        }

        return messages
    }

    @JvmStatic
    fun getMessages(document: Document): List<SMessage> {
        val html = document.html()
        return SagresMessageParser.getMessages(html)
    }

    private fun extractInfoArticle(article: String): SMessage? {
        val clazz = extractArticleForm1(MESSAGE_CLASS_RECEIVED, article)
        val date = extractArticleForm1(MESSAGE_DATE_RECEIVED, article)
        val message = extractArticleForm2(MESSAGE_MESSAGE_RECEIVED, article)
        val from = extractArticleForm2(MESSAGE_FROM_RECEIVED, article)
        return SMessage(
            message!!.toLowerCase().hashCode().toLong(),
            null,
            null,
            message,
            -2,
            from,
            null
        ).apply {
            isFromHtml = true
            discipline = clazz
            dateString = date
            processingTime = System.currentTimeMillis()
        }
    }

    private fun extractArticleForm2(regex: String, article: String): String? {
        val startRRE = article.indexOf(regex)
        if (startRRE != -1) {
            val endRRE = article.indexOf("</span>", startRRE)
            var message = article.substring(startRRE, endRRE).trim { it <= ' ' }
            message = message.substring(regex.length + 1)

            message = message.substring(message.indexOf(">") + 1).trim { it <= ' ' }
            return message
        }

        return null
    }

    private fun extractArticleForm1(regex: String, article: String): String? {
        val startCRE = article.indexOf(regex)
        if (startCRE != -1) {
            val endCRE = article.indexOf("</span>", startCRE)
            var extracted = article.substring(startCRE, endCRE)

            // CLASS NAME
            extracted = extracted.substring(extracted.indexOf(">") + 1).trim { it <= ' ' }
            return extracted
        }

        return null
    }
}
