package com.example.p3_30_monumentos_gabriel_jorge.data

import android.content.Context
import com.example.p3_30_monumentos_gabriel_jorge.R
import com.example.p3_30_monumentos_gabriel_jorge.model.Monumento
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException

class Datasource(private val context: Context) {

    fun loadMonuments(): List<Monumento> {
        val jsonString: String
        try {
            val inputStream = context.resources.openRawResource(R.raw.monumentos)
            jsonString = inputStream.bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            return emptyList()
        }

        val listType = object : TypeToken<List<Monumento>>() {}.type
        return Gson().fromJson(jsonString, listType)
    }
}