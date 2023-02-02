package engine.util

import java.util.*


fun generateUUID(): String {
    return UUID.randomUUID().toString().replace("-", "")
}