package com.fauna.query.template;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

class FaunaTemplateTest {

    @Test
    void testTemplate_WithSingleVariable() {
        FaunaTemplate template = new FaunaTemplate("let x = ${my_var}");
        List<FaunaTemplate.TemplatePart> expanded = new ArrayList<>();
        template.forEach(expanded::add);
        assertEquals(2, expanded.size());
        assertEquals("let x = ", expanded.get(0).getPart());
        assertEquals(TemplatePartType.LITERAL, expanded.get(0).getType());
        assertEquals("my_var", expanded.get(1).getPart());
        assertEquals(TemplatePartType.VARIABLE, expanded.get(1).getType());
    }

    @Test
    void testTemplate_WithDuplicateVariable() {
        FaunaTemplate template = new FaunaTemplate("let x = ${my_var}\nlet y = ${my_var}\nx * y");
        List<FaunaTemplate.TemplatePart> expanded = new ArrayList<>();
        template.forEach(expanded::add);
        assertEquals(5, expanded.size());
        assertEquals("let x = ", expanded.get(0).getPart());
        assertEquals(TemplatePartType.LITERAL, expanded.get(0).getType());
        assertEquals("my_var", expanded.get(1).getPart());
        assertEquals(TemplatePartType.VARIABLE, expanded.get(1).getType());
        assertEquals("\nlet y = ", expanded.get(2).getPart());
        assertEquals(TemplatePartType.LITERAL, expanded.get(2).getType());
        assertEquals("my_var", expanded.get(3).getPart());
        assertEquals(TemplatePartType.VARIABLE, expanded.get(3).getType());
        assertEquals("\nx * y", expanded.get(4).getPart());
        assertEquals(TemplatePartType.LITERAL, expanded.get(4).getType());
    }

    @Test
    void testTemplate_WithVariableAtStart() {
        FaunaTemplate template = new FaunaTemplate("${my_var} { .name }");
        List<FaunaTemplate.TemplatePart> expanded = new ArrayList<>();
        template.forEach(expanded::add);
        assertEquals(2, expanded.size());
        assertEquals("my_var", expanded.get(0).getPart());
        assertEquals(TemplatePartType.VARIABLE, expanded.get(0).getType());
        assertEquals(" { .name }", expanded.get(1).getPart());
        assertEquals(TemplatePartType.LITERAL, expanded.get(1).getType());
    }

    @Test
    void testTemplates_WithEscapes() {
        FaunaTemplate template = new FaunaTemplate("let x = '$${not_a_var}'");
        List<FaunaTemplate.TemplatePart> expanded = new ArrayList<>();
        template.forEach(expanded::add);
        assertEquals(2, expanded.size());
        assertEquals("let x = '$", expanded.get(0).getPart());
        assertEquals(TemplatePartType.LITERAL, expanded.get(0).getType());
        assertEquals("{not_a_var}'", expanded.get(1).getPart());
        assertEquals(TemplatePartType.LITERAL, expanded.get(1).getType());
    }

    @Ignore
    @Test
    void testTemplates_WithUnsupportedIdentifiers() {
        FaunaTemplate template = new FaunaTemplate("let x = ${かわいい}");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            List<FaunaTemplate.TemplatePart> expanded = new ArrayList<>();
            template.forEach(expanded::add);
        });
        String expectedMessage = "Invalid placeholder in template: line 1, col 9";
        String actualMessage = exception.getMessage();
        assertTrue(actualMessage.contains(expectedMessage));
    }

}