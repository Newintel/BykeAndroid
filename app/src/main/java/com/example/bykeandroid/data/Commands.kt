package com.example.bykeandroid.data

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

enum class Commands {
    NONE {
        override fun getCode(): Byte {
            return 0x00
        }
         },
    OK {
        override fun getCode(): Byte {
            return 0x04
        }
         },
    GET_NEXT_STEP {
        override fun getCode(): Byte {
            return 0x03
        }
         },
    NEXT_STEP {
        override fun getCode(): Byte {
            return 0x02
        }

        override fun has_info(): Boolean {
            return true
        }
     },
    NEW_STEP {
        override fun getCode(): Byte {
            return 0x01
        }

        override fun has_info(): Boolean {
            return true
        }
    };

    abstract fun getCode(): Byte
    open fun has_info(): Boolean {
        return false
    }

}

interface Info {}

fun parseCommand(bytes : ByteArray): Triple<Commands, Int, String?>? {
    if (bytes.size < 2) {
        return null
    }
    val code = bytes[0]
    val length = bytes[1].toInt()

    var c = Commands.NONE
    var info: String? = null
    if (length + 2 <= bytes.size) {
        for (command in Commands.values()) {
            if (command.getCode() == code) {
                c = command
                break
            }
        }
        if (c.has_info()) {
            info = String(bytes.copyOfRange(2, length + 2))
        }
    }
    return Triple(c, length, info)
}
