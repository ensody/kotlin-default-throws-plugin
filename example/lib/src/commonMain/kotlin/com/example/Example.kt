package com.example

public fun globalFoo() {}

@Throws(RuntimeException::class)
public fun globalFooAnnotated() { error("Throw but no crash") }

public suspend fun coGlobalFoo() { error("Throw but no crash") }

@Throws(RuntimeException::class)
public suspend fun coGlobalFooAnnotated() { error("Throw but no crash") }

public interface IFoo {
    public fun foo() {}

    @Throws(RuntimeException::class)
    public fun fooAnnotated() { error("Throw but no crash") }

    public suspend fun coFoo() { error("Throw but no crash") }

    @Throws(RuntimeException::class)
    public suspend fun coFooAnnotated() { error("Throw but no crash") }

    public suspend fun <T> coFooGeneric(x: T): T { error("Throw but no crash") }

    @Throws(RuntimeException::class)
    public suspend fun <T> coFooGenericAnnotated(x: T): T { error("Throw but no crash") }

    public companion object {
        public fun foo() {}

        @Throws(RuntimeException::class)
        public fun fooAnnotated() { error("Throw but no crash") }

        public suspend fun coFoo() { error("Throw but no crash") }

        @Throws(RuntimeException::class)
        public suspend fun coFooAnnotated() { error("Throw but no crash") }

        public suspend fun <T> coFooGeneric(x: T) { error("Throw but no crash") }

        @Throws(RuntimeException::class)
        public suspend fun <T> coFooGenericAnnotated(x: T) { error("Throw but no crash") }
    }
}

public class Foo {
    public fun foo() {}

    @Throws(RuntimeException::class)
    public fun fooAnnotated() { error("Throw but no crash") }

    public suspend fun coFoo() { error("Throw but no crash") }

    @Throws(RuntimeException::class)
    public suspend fun coFooAnnotated() { error("Throw but no crash") }

    public suspend fun <T> coFooGeneric(x: T) { error("Throw but no crash") }

    @Throws(RuntimeException::class)
    public suspend fun <T> coFooGenericAnnotated(x: T) { error("Throw but no crash") }

    public companion object {
        public fun foo() {}

        @Throws(RuntimeException::class)
        public fun fooAnnotated() { error("Throw but no crash") }

        public suspend fun coFoo() { error("Throw but no crash") }

        @Throws(RuntimeException::class)
        public suspend fun coFooAnnotated() { error("Throw but no crash") }

        public suspend fun <T> coFooGeneric(x: T) { error("Throw but no crash") }

        @Throws(RuntimeException::class)
        public suspend fun <T> coFooGenericAnnotated(x: T) { error("Throw but no crash") }
    }
}

public enum class EnumFoo {
    X,
    ;

    public fun foo() {}

    @Throws(RuntimeException::class)
    public fun fooAnnotated() { error("Throw but no crash") }

    public suspend fun coFoo() { error("Throw but no crash") }

    @Throws(RuntimeException::class)
    public suspend fun coFooAnnotated() { error("Throw but no crash") }

    public suspend fun <T> coFooGeneric(x: T) { error("Throw but no crash") }

    @Throws(RuntimeException::class)
    public suspend fun <T> coFooGenericAnnotated(x: T) { error("Throw but no crash") }

    public companion object {
        public fun foo() {}

        @Throws(RuntimeException::class)
        public fun fooAnnotated() { error("Throw but no crash") }

        public suspend fun coFoo() { error("Throw but no crash") }

        @Throws(RuntimeException::class)
        public suspend fun coFooAnnotated() { error("Throw but no crash") }

        public suspend fun <T> coFooGeneric(x: T) { error("Throw but no crash") }

        @Throws(RuntimeException::class)
        public suspend fun <T> coFooGenericAnnotated(x: T) { error("Throw but no crash") }
    }
}
