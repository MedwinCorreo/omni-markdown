plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.2.0"
    id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

dependencies {
    intellijPlatform {
        // Use locally installed Android Studio — avoids download catalog lookup
        // for builds not yet indexed (e.g. AI-252.25557.131.2521.14432022)
        local("/Users/digitalworkplaceadmin/Applications/Android Studio.app")
        bundledPlugin("com.intellij.java")
    }
}

intellijPlatform {
    pluginConfiguration {
        id = "com.omnimarkdown.plugin"
        name = "Omni Markdown"
        version = providers.gradleProperty("pluginVersion").get()
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild").get()
        }
    }
}

kotlin {
    jvmToolchain(21)
}
