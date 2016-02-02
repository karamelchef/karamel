package se.kth.karamel.common;

import org.junit.Test;
import se.kth.karamel.common.util.NovaCredentials;

import static org.junit.Assert.assertEquals;

public class NovaCredentialsTest {

    @Test
    public void novaCredentialsSanityCheck() {
        NovaCredentials nova = new NovaCredentials();
        nova.setAccountName("pepe");
        nova.setAccountPass("12345");
        nova.setEndpoint("endpoint");
        nova.setRegion("region");
        assertEquals("pepe", nova.getAccountName());
        assertEquals("12345", nova.getAccountPass());
        assertEquals("endpoint",nova.getEndpoint());
        assertEquals("region",nova.getRegion());
    }
}