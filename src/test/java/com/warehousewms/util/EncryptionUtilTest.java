package com.warehousewms.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncryptionUtilTest {

    @Test
    void encryptAndDecryptRoundTrip() {
        String original = "MySecretPassword123!";
        String encrypted = EncryptionUtil.encrypt(original);
        assertNotNull(encrypted);
        assertNotEquals(original, encrypted);

        String decrypted = EncryptionUtil.decrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void decryptInvalidReturnsNull() {
        String result = EncryptionUtil.decrypt("not-valid-base64-cipher");
        assertNull(result, "Decrypting invalid text should return null");
    }

    @Test
    void encryptProducesDifferentOutputForDifferentInput() {
        String enc1 = EncryptionUtil.encrypt("password1");
        String enc2 = EncryptionUtil.encrypt("password2");
        assertNotEquals(enc1, enc2);
    }

    @Test
    void encryptWithRandomIVProducesDifferentOutputForSameInput() {
        String enc1 = EncryptionUtil.encrypt("same");
        String enc2 = EncryptionUtil.encrypt("same");
        assertNotEquals(enc1, enc2, "AES/GCM with random IV should produce different ciphertext for same plaintext");
        assertEquals("same", EncryptionUtil.decrypt(enc1), "First ciphertext should decrypt correctly");
        assertEquals("same", EncryptionUtil.decrypt(enc2), "Second ciphertext should decrypt correctly");
    }
}
