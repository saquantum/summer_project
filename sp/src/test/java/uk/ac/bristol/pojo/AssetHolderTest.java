package uk.ac.bristol.pojo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class AssetHolderTest {

    @Test
    public void testAddressSetterWithNullInput() {
        AssetHolder holder = new AssetHolder();
        assertThrows(IllegalArgumentException.class, () -> holder.setAddress(null));
    }

    @Test
    public void testContactPreferencesSetterWithNullInput() {
        AssetHolder holder = new AssetHolder();
        assertThrows(IllegalArgumentException.class, () -> holder.setContactPreferences(null));
    }

    @Test
    public void testAddressSetterIgnoreExtraFields(){
        AssetHolder holder = new AssetHolder();

        Map<String, String> input = new HashMap<>();
        input.put("assetHolderId", "user_001");
        input.put("street", "123 Main St");
        input.put("city", "Testville");
        input.put("postcode", "ABC123");
        input.put("country", "Testland");
        input.put("extra", "should be ignored");

        holder.setAddress(input);
        Map<String, String> result = holder.getAddress();

        assertEquals("user_001", result.get("assetHolderId"));
        assertEquals("123 Main St", result.get("street"));
        assertEquals("Testville", result.get("city"));
        assertEquals("ABC123", result.get("postcode"));
        assertEquals("Testland", result.get("country"));
        assertFalse(result.containsKey("extra"));
        assertEquals(5, result.size());
    }

    @Test
    public void testAddressSetterWithPartialFields() {
        AssetHolder holder = new AssetHolder();

        Map<String, String> input = new HashMap<>();
        input.put("street", "45 Short Ln");

        holder.setAddress(input);
        Map<String, String> result = holder.getAddress();

        assertEquals("45 Short Ln", result.get("street"));
        assertEquals("", result.get("city"));
        assertEquals("", result.get("postcode"));
        assertEquals("", result.get("country"));
        assertEquals(5, result.size());
    }

    @Test
    public void testContactPreferencesSetterIgnoreExtraFields() {
        AssetHolder holder = new AssetHolder();

        Map<String, Object> input = new HashMap<>();
        input.put("assetHolderId", "user_001");
        input.put("email", true);
        input.put("phone", false);
        input.put("post", true);
        input.put("whatsapp", true);
        input.put("discord", false);
        input.put("telegram", true);
        input.put("weixin", true);
        input.put("qq", true);

        holder.setContactPreferences(input);
        Map<String, Object> result = holder.getContactPreferences();

        assertEquals("user_001", result.get("assetHolderId"));
        assertEquals(true, result.get("email"));
        assertEquals(false, result.get("phone"));
        assertEquals(true, result.get("post"));
        assertEquals(true, result.get("whatsapp"));
        assertEquals(false, result.get("discord"));
        assertEquals(true, result.get("telegram"));
        assertFalse(result.containsKey("weixin"));
        assertFalse(result.containsKey("qq"));
        assertEquals(7, result.size());
    }

    @Test
    public void testContactPreferencesSetterWithPartialFields() {
        AssetHolder holder = new AssetHolder();

        Map<String, Object> input = new HashMap<>();
        input.put("email", true);
        input.put("phone", false);

        holder.setContactPreferences(input);
        Map<String, Object> result = holder.getContactPreferences();

        assertEquals(true, result.get("email"));
        assertEquals(false, result.get("phone"));
        assertEquals(false, result.get("post"));
        assertEquals(false, result.get("whatsapp"));
        assertEquals(false, result.get("discord"));
        assertEquals(false, result.get("telegram"));
        assertEquals(7, result.size());
    }
}
