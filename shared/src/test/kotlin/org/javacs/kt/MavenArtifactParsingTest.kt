package org.javacs.kt

import org.hamcrest.Matchers.*
import org.javacs.kt.classpath.parseMavenArtifact
import org.javacs.kt.classpath.Artifact
import org.junit.Assert.assertThat
import org.junit.Test

class MavenArtifactParsingTest {
    @Test
    fun `parse maven artifacts`() {
        assertThat(parseMavenArtifact("net.sf.json-lib:json-lib:jar:jdk15:2.4:compile"), equalTo(Artifact(
            group = "net.sf.json-lib",
            artifact = "json-lib",
            packaging = "jar",
            classifier = "jdk15",
            version = "2.4",
            scope = "compile"
        )))
        
        assertThat(parseMavenArtifact("io.netty:netty-transport-native-epoll:jar:linux-x86_64:4.1.36.Final:compile"), equalTo(Artifact(
            group = "io.netty",
            artifact = "netty-transport-native-epoll",
            packaging = "jar",
            classifier = "linux-x86_64",
            version = "4.1.36.Final",
            scope = "compile"
        )))
        
        assertThat(parseMavenArtifact("org.codehaus.mojo:my-project:1.0"), equalTo(Artifact(
            group = "org.codehaus.mojo",
            artifact = "my-project",
            packaging = null,
            classifier = null,
            version = "1.0",
            scope = null
        )))
    }
}
