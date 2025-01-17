package org.javacs.kt

import org.hamcrest.Matchers.*
import org.javacs.kt.classpath.*
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Files

class ClassPathTest {
    @Test fun `find gradle classpath`() {
        val workspaceRoot = testResourcesRoot().resolve("additionalWorkspace")
        val buildFile = workspaceRoot.resolve("build.gradle")

        assertTrue(Files.exists(buildFile))

        val classPath = findClassPath(listOf(workspaceRoot))

        assertThat(classPath, hasItem(hasToString(containsString("junit"))))
    }

    @Test fun `find kotlin stdlib`() {
        assertThat(findKotlinStdlib(), notNullValue())
    }
}
