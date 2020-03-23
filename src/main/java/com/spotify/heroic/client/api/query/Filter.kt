package com.spotify.heroic.client.api.query

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue


interface Filter

class KeyTagFilter(key : Key, tags: List<Tag>) : Filter {
    @JsonValue
    private var filter = mutableListOf<Any>()

    init {
        filter.add("and")
        filter.add(key)
        filter.addAll(tags)
    }
    companion object {
        @JvmStatic
        fun of(key : Key, tags: List<Tag>) : Filter {
            return KeyTagFilter(key, tags)
        }
    }
}

class TagFilter(tags: List<Tag>): Filter {
    @JsonValue
    private var filter = mutableListOf<Any>()

    init {
        filter.add("and")
        filter.addAll(tags)
    }
    companion object {
        @JvmStatic
        fun of(tags: List<Tag>) : Filter {
            return TagFilter(tags)
        }
    }
}

class TrueFilter: Filter {
    @JsonValue
    private var filter = true
}

// TODO: Support other operators
class Key constructor(key: String) {
    @JsonValue
    private var filter = listOf("key", key)

    companion object {
        @JvmStatic
        fun of(key: String) : Key {
            return Key(key)
        }
    }
}

class Tag constructor(key: String, value: String, operator: Operator) {
    @JsonValue
    private val tag = listOf(operator, key, value)

    companion object {
        @JvmStatic
        fun of(key: String, value: String, operator: Operator) : Tag {
            return Tag(key, value, operator)
        }
    }
}



/*
    TODO: add tag in, tag not in, and regex
 */
enum class Operator {
    @JsonProperty("=")
    MATCH,

    @JsonProperty("!=")
    NOT_MATCH,

    @JsonProperty("+")
    EXIST,

    @JsonProperty("!+")
    NOT_EXIST,

    @JsonProperty("^")
    PREFIX,

    @JsonProperty("!^")
    NOT_PREFIX
}
