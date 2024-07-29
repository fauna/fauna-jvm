package com.fauna.client;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FaunaRoleTest {

    @Test
    public void testBuiltInRoles() {
        assertEquals("admin", FaunaRole.ADMIN.toString());
        assertEquals("server", FaunaRole.SERVER.toString());
        assertEquals("server-readonly", FaunaRole.SERVER_READ_ONLY.toString());
    }

    @Test
    public void testValidUserDefinedRoles() {
        assertEquals("@role/foo", FaunaRole.named("foo").toString());
        assertEquals("@role/slartibartfast", FaunaRole.named("slartibartfast").toString());
    }

    @Test
    public void testInvalidUserDefinedRoles() {
        assertThrows(IllegalArgumentException.class, () -> FaunaRole.named("server").toString());
        assertThrows(IllegalArgumentException.class, () -> FaunaRole.named("1foo").toString());
        assertThrows(IllegalArgumentException.class, () -> FaunaRole.named("foo$").toString());
        assertThrows(IllegalArgumentException.class, () -> FaunaRole.named("foo bar").toString());
    }

}
