package com.example.dlms_parser.domain.model

import kotlinx.serialization.Serializable

@Serializable
sealed class ParseResult<out T> {
    @Serializable
    data class Success<T>(val data: T) : ParseResult<T>()
    
    @Serializable
    data class Error(val message: String, val exception: String? = null) : ParseResult<Nothing>()
}

inline fun <T> ParseResult<T>.onSuccess(action: (T) -> Unit): ParseResult<T> {
    if (this is ParseResult.Success) {
        action(data)
    }
    return this
}

inline fun <T> ParseResult<T>.onError(action: (String, String?) -> Unit): ParseResult<T> {
    if (this is ParseResult.Error) {
        action(message, exception)
    }
    return this
}

fun <T> ParseResult<T>.getOrNull(): T? {
    return when (this) {
        is ParseResult.Success -> data
        is ParseResult.Error -> null
    }
}

fun <T> ParseResult<T>.getOrThrow(): T {
    return when (this) {
        is ParseResult.Success -> data
        is ParseResult.Error -> throw Exception("Parse error: $message")
    }
}