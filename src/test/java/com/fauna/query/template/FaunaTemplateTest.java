package com.fauna.query.template;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import com.fauna.query.builder.Fragment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @ParameterizedTest
    @ValueSource(strings = {"let x = $my_var", "let x = \\$my_var"})
    void testTemplate_WithDollarSignDoesNotInfiniteLoop(String literal) {
        FaunaTemplate template = new FaunaTemplate(literal);
        FaunaTemplate.TemplatePart[] parts = StreamSupport.stream(
                template.spliterator(), true).toArray(FaunaTemplate.TemplatePart[]::new);
        // The dollar sign gets swallowed, even if it's escaped?
        assertEquals(2, parts.length);
        for (FaunaTemplate.TemplatePart part : parts) {
            assertEquals(TemplatePartType.LITERAL, part.getType());
        }
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

    @Test
    void testTemplateParts() {
        // The 4 Fauna line terminators are:
        //      U+000A \n   Line feed
        //      U+000D \r   Carriage return
        //      U+2028      Line separator
        //      U+2029      Paragraph separator
        // https://docs.fauna.com/fauna/current/reference/fql_reference/lexical#line-terminators
        String my_var = "my_var";
        String fql = String.join("", new String[] {
                "let x = ${my_var}", Character.toString(0x000a),
                "let y = ${my_var}", Character.toString(0x000d),
                "let z = ${my_var}", Character.toString(0x2028),
                "x * y", Character.toString(0x2029),
                "x + y"});
        FaunaTemplate template = new FaunaTemplate(fql);
        String[] parts = StreamSupport.stream(template.spliterator(), true).map(
                FaunaTemplate.TemplatePart::getPart).toArray(String[]::new);
        assertArrayEquals(new String[] {
                fql.substring(0, 8), my_var,
                fql.substring(17, 26), my_var,
                fql.substring(35, 44), my_var,
                fql.substring(53, 65)}, parts);

    }

}