package se.kth.karamel.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class NovaCredentialsTest {

    @Test
    public void novaCredentialsSanityCheck() {
        NovaCredentials nova = new NovaCredentials();
        nova.setAccountName("pepe");
        nova.setAccountPass("12345");
        nova.setEndpoint("endpoint");
        assertEquals("pepe", nova.getAccountName());
        assertEquals("12345", nova.getAccountPass());
        assertEquals("endpoint",nova.getEndpoint());
    }
}