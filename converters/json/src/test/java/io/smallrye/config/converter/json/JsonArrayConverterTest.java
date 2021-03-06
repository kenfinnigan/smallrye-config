package io.smallrye.config.converter.json;

import java.io.File;
import java.io.StringReader;

import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonReader;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Testing the injection of a JsonArray
 * 
 * @author <a href="mailto:phillip.kruger@redhat.com">Phillip Kruger</a>
 */
@RunWith(Arquillian.class)
public class JsonArrayConverterTest {

    @Inject
    @ConfigProperty(name = "someJsonArray")
    private JsonArray someValue;

    @Deployment
    public static WebArchive createDeployment() {
        final File[] thorntailMPConfigFiles = Maven.resolver().resolve("io.thorntail:microprofile-config:2.4.0.Final")
                .withoutTransitivity().asFile();

        return ShrinkWrap.create(WebArchive.class, "JsonArrayConverterTest.war")
                .addPackage(JsonArrayConverter.class.getPackage())
                .addAsLibraries(thorntailMPConfigFiles)
                .addAsResource(
                        new File("src/main/resources/META-INF/services/org.eclipse.microprofile.config.spi.Converter"),
                        "META-INF/services/org.eclipse.microprofile.config.spi.Converter")
                .addAsResource(JsonArrayConverterTest.class.getClassLoader().getResource("test.properties"),
                        "META-INF/microprofile-config.properties")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void testInjection() {
        try (JsonReader jsonReader = Json.createReader(new StringReader("[\"value1\",\"value2\",\"value3\"]"))) {
            JsonArray jsonArray = jsonReader.readArray();
            Assert.assertEquals(jsonArray, someValue);
        }
    }
}
