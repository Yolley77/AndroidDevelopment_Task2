package com.example.lab2

import android.content.ContentValues
import android.util.Log
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.ArrayList

val DATA_URL: String = "https://raw.githubusercontent.com/wesleywerner" +
        "/ancient-tech/02decf875616dd9692b31658d92e64a20d99f816" +
        "/src/data/techs.ruleset.json"

class JSONParser {

    fun getJSON() : ArrayList<Item> {

        val items = ArrayList<Item>() // будущий массив готовых объектов

        val url: URL
        var response = StringBuilder() // стринг не пересоздается, а изменяется
        try { // попытка создать юрл, хз почему нельзя просто взять и открыть
            url =
                URL("https://raw.githubusercontent.com/wesleywerner/ancient-tech/02decf875616dd9692b31658d92e64a20d99f816/src/data/techs.ruleset.json")
        } catch (e: MalformedURLException) {
            throw IllegalArgumentException("Invalid URL")
        }
        var connection: HttpURLConnection? = null
        try {
            connection = url.openConnection() as HttpURLConnection // открываем горе-ссылку
            connection.doOutput = false // вводить не можем
            connection.doInput = true // можем выводить
            connection.requestMethod = "GET" // get - получение ресурса

            val status = connection.responseCode // cостояние hhtp - 200 - ok
            if (status != 200) {
                throw IOException("Post failed with error code $status")
            } else {
                val instream = BufferedReader(InputStreamReader(connection.inputStream)) // открываем буферный поток ввода

                var inputLine: String? = instream.readLine()
                while ((inputLine) != null) { // читаем все строки
                    response.append(inputLine)
                    inputLine = instream.readLine()
                }
                instream.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
            val jsonString = response.toString().trim() // переводим в строку и удаляем ненужные пробелы
            Log.i(ContentValues.TAG, "Received JSON: $jsonString")
            try {
                val jsonArr = JSONArray(jsonString)
                createItems(items, jsonArr)
            } catch (je: JSONException) {
                Log.e(ContentValues.TAG, "Failed to create items", je)
            }

        }

        return items
    }

    /*
    JSON-объекты собираются самостоятельно благодаря удобно читаемому оформлению файла - каждый объект заключен в { },
    свойства прописаны чз двоеточие
    "name": "Patrick",
    "graphic": "Pic.jpg"
     */

    @Throws(JSONException::class)
    private fun createItems(items: MutableList<Item>, jsonArray: JSONArray) {
        for (i in 1 until jsonArray.length()) { // с 1, потому что сам файл начинается с описания, только потом идут объекты (конкретно в этом файле)
            val jsonObjectItem = jsonArray.getJSONObject(i) // из массива json достаем объект
            var tmp: String?
            if (jsonObjectItem.has("helptext")) { // если есть хэлптекст - добавляем в объект
                tmp = (jsonObjectItem.getString("helptext"))
            } else tmp = null
            // "https://raw.githubusercontent.com/wesleywerner/ancient-tech/02decf875616dd9692b31658d92e64a20d99f816/src/images/tech/"
            val graphic: String = "https://raw.githubusercontent.com/wesleywerner/ancient-tech/" +
                    "02decf875616dd9692b31658d92e64a20d99f816/src/images/tech/" + jsonObjectItem.getString("graphic") // получаем ссылку на картинку
            val item = Item(graphic, (jsonObjectItem.getString("name")), tmp) //собираем свой объект
            items.add(item)
        }
    }
}