package com.example

import kotlin.reflect.jvm.javaMethod
import kotlin.test.Test
import kotlin.test.assertEquals

class ExampleTest {
    @Test
    fun testGlobalFoo() {
        assertEquals(
            listOf(),
            ::globalFoo.javaMethod!!.exceptionTypes.toList(),
        )
        assertEquals(
            listOf(RuntimeException::class.java),
            ::globalFooAnnotated.javaMethod!!.exceptionTypes.toList(),
        )
        assertEquals(
            listOf(Throwable::class.java),
            ::coGlobalFoo.javaMethod!!.exceptionTypes.toList(),
        )
        assertEquals(
            listOf(RuntimeException::class.java),
            ::coGlobalFooAnnotated.javaMethod!!.exceptionTypes.toList(),
        )
    }

    @Test
    fun testIFoo() {
        testClassLike(IFoo::class.java)
    }

    @Test
    fun testIFooCompanion() {
        testClassLike(IFoo.Companion::class.java)
    }

    @Test
    fun testFoo() {
        testClassLike(Foo::class.java)
    }

    @Test
    fun testFooCompanion() {
        testClassLike(Foo.Companion::class.java)
    }

    @Test
    fun testEnumFoo() {
        testClassLike(EnumFoo::class.java)
    }

    @Test
    fun testEnumFooCompanion() {
        testClassLike(EnumFoo.Companion::class.java)
    }

    private fun testClassLike(cls: Class<*>) {
        val methods = cls.declaredMethods.associateBy { it.name }
        assertEquals(
            listOf(),
            methods.getValue("foo").exceptionTypes.toList(),
        )
        assertEquals(
            listOf(RuntimeException::class.java),
            methods.getValue("fooAnnotated").exceptionTypes.toList(),
        )
        assertEquals(
            listOf(Throwable::class.java),
            methods.getValue("coFoo").exceptionTypes.toList(),
        )
        assertEquals(
            listOf(RuntimeException::class.java),
            methods.getValue("coFooAnnotated").exceptionTypes.toList(),
        )
        assertEquals(
            listOf(Throwable::class.java),
            methods.getValue("coFooGeneric").exceptionTypes.toList(),
        )
        assertEquals(
            listOf(RuntimeException::class.java),
            methods.getValue("coFooGenericAnnotated").exceptionTypes.toList(),
        )
    }
}
