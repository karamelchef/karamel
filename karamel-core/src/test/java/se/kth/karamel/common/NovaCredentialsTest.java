package se.kth.karamel.common;

import org.junit.Test;

import static org.junit.Assert.*;

public class NovaCredentialsTest {

    @Test
    public void novaCredentiaslSanityCheck() {
        NovaCredentials nova = new NovaCredentials();
        nova.setAccountName("pepe");
        nova.setAccountPass("12345");
        assertEquals("pepe", nova.getAccountName());
        assertEquals("12345", nova.getAccountPass());
    }
}