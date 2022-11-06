package message

abstract class Message {
}

data class UserFormMessage(val form:Map<String,Any?>): Message()