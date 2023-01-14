package com.example.bykeandroid.data

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

        override fun hasInfo(): Boolean {
            return true
        }
     },
    NEW_STEP {
        override fun getCode(): Byte {
            return 0x01
        }

        override fun hasInfo(): Boolean {
            return true
        }
    };

    abstract fun getCode(): Byte
    open fun hasInfo(): Boolean {
        return false
    }
}

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
        if (c.hasInfo()) {
            info = String(bytes.copyOfRange(2, length + 2))
        }
    }
    return Triple(c, length, info)
}
