package ro.pub.cs.systems.eim.lab09.chatservicejmdns.general

import android.util.Log
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.Random

object Utilities {
    fun getReader(socket: Socket): BufferedReader? {
        return try {
            BufferedReader(InputStreamReader(socket.getInputStream()))
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.printStackTrace()
            }
            null
        }
    }

    fun getWriter(socket: Socket): PrintWriter? {
        return try {
            PrintWriter(BufferedOutputStream(socket.getOutputStream()))
        } catch (ioException: IOException) {
            Log.e(Constants.TAG, "An exception has occurred: ${ioException.message}")
            if (Constants.DEBUG) {
                ioException.message
            }
            null
        }
    }

    fun generateIdentifier(length: Int): String {
        val result = StringBuilder("-")
        val random = Random()
        for (index in 0 until length) {
            result.append((Constants.FIRST_LETTER.code + random.nextInt(Constants.ALPHABET_LENGTH)).toChar())
        }
        return result.toString()
    }
}

