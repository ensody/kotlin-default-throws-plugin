# kotlin-default-throws-plugin

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.ensody.kotlindefaultthrows/gradle-plugin/badge.svg?gav=true)](https://maven-badges.herokuapp.com/maven-central/com.ensody.kotlindefaultthrows/gradle-plugin?gav=true)

This Kotlin compiler plugin adds default `@Throws(Throwable::class)` annotations to all `suspend fun` in the whole code.

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

The reason for this plugin is that for native exports (especially iOS XCFrameworks) any `suspend fun` already requires error handling from Swift/Objective-C. However, if you forget to add an explicit `@Throws` annotation and the `suspend fun` throws any exception other than `CancellationException` your whole app hard-crashes fatally! This is never what you want.

With this plugin you can't forget these annotations on `suspend fun`, anymore. You only need to think of adding those annotations to non-suspend functions which are usually far fewer places.

Note: If you add an explicit `@Throws(...)` annotation this plugin won't add the default annotation. So you still have control over the exception type.

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
