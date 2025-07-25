# kotlin-default-throws-plugin

[![Maven Central](https://img.shields.io/maven-central/v/com.ensody.kotlindefaultthrows/gradle-plugin?color=%2345cf00)](https://central.sonatype.com/artifact/com.ensody.kotlindefaultthrows/gradle-plugin)

This Kotlin compiler plugin adds default `@Throws(Throwable::class)` annotations to every `suspend fun` in your whole code. The reason for this plugin is to **prevent hard-crashing** iOS apps if you forget that annotation.

In other words, it turns this:

```kotlin
suspend fun foo() {
    // Without this plugin this line would crash the whole app instead of throwing
    error("Throws")
}
```

into this:

```kotlin
@Throws(Throwable::class) // now it won't crash, but just throw
suspend fun foo() {
    error("Throws")
}
```

## More details

Note: You only need this plugin if your code gets exported as an XCFramework.

Let's pretend you forget to add `@Throws` on `suspend fun foo()` like in the first example above. This means Kotlin behaves as if the function had `@Throws(CancellationException::class)`. So, only `CancellationException` is allowed and any other exception type **immediately hard-crashes** your iOS app. I'll repeat: **your app gets killed!**

Even worse, for an iOS app integrating your XCFramework it looks like error handling would work:

```swift
// In Swift: try is needed even if @Throws is missing
try await foo()
```

So, an iOS developer wouldn't even notice that something is wrong in the Kotlin code. However, calling that function is dangerous.

If you install this compiler plugin, it annotates every `suspend fun` for you automatically. No need to annotate, anymore.

**IMPORTANT:** You still have to think of adding those annotations to **non-suspend** functions, but that is usually needed in far fewer places.

## Overriding the plugin's default annotation

If you add an explicit `@Throws(...)` annotation to your function, this plugin won't add the default annotation. So you still have control over the exception type.

## Installation

Define an entry in your version catalog:

```toml
[plugins]
defaultThrows = { id = "com.ensody.kotlindefaultthrows", version = "..." }
```

In your root build.gradle.kts:

```kotlin
plugins {
    alias(libs.plugins.defaultThrows) apply false
}
```

In each module's build.gradle.kts that should apply this plugin:

```kotlin
plugins {
    id("com.ensody.kotlindefaultthrows")
}
```

## iOS example project

In example/IosTestProject you can find a sample project with unit tests to confirm the compiler plugin's behavior. Just run the following in the IosTestProject folder:

```shell
./generate-project.sh
open IosTestProject.xcworkspace
```

Once Xcode has opened you can play with the unit tests.

If you edit example/lib/build.gradle.kts and in the `plugins` section remove the `id("com.ensody.kotlindefaultthrows")` and re-run the unit tests you'll see what happens without this plugin when you forget to add `@Throws` annotations.

## License

```
Copyright 2025 Ensody GmbH, Waldemar Kornewald

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
