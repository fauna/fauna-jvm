package com.fauna.query.template;

import com.fauna.query.builder.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents a template for constructing Fauna queries with placeholders
 * for variable interpolation.
 */
public class FaunaTemplate implements Iterable<FaunaTemplate.TemplatePart> {

    private static final char DELIMITER = '$';
    private static final String ID_PATTERN = "[\\p{L}_][\\p{L}\\p{N}_]*";
    private static final Pattern PATTERN;

    static {
        String delim = Pattern.quote(String.valueOf(DELIMITER));
        String pattern = String.format(
                "%s(?:(?<escaped>%s)|\\{(?<braced>%s)\\}|\\{(?<invalid>[^}]*))?",
                delim, delim, ID_PATTERN
        );
        PATTERN = Pattern.compile(pattern, Pattern.COMMENTS);
    }

    private final String template;

    /**
     * Constructs a new FaunaTemplate with the given string template.
     *
     * @param template the string template containing literals and placeholders.
     */
    public FaunaTemplate(String template) {
        this.template = template;
    }

    /**
     * Creates an iterator over the parts of the template, distinguishing
     * between literals and variable placeholders.
     *
     * @return an Iterator that traverses the template parts.
     */
    @Override
    public Iterator<TemplatePart> iterator() {
        return new Iterator<>() {
            private final Matcher matcher = PATTERN.matcher(template);
            private int curPos = 0;
            private boolean foundMatch = false;

            @Override
            public boolean hasNext() {
                if (curPos < template.length()) {
                    return true;
                }
                if (foundMatch) {
                    return true;
                }
                return matcher.find(curPos);
            }

            @Override
            public TemplatePart next() {
                if (curPos >= template.length()) {
                    throw new IllegalStateException("No more elements");
                }

                if (foundMatch || (foundMatch = matcher.find(curPos))) {
                    int spanStartPos = matcher.start();
                    int spanEndPos = matcher.end();
                    String invalid = matcher.group("invalid");
                    if (invalid != null) {
                        handleInvalid(matcher.start("invalid"));
                    }
                    String escapedPart = matcher.group("escaped");
                    String variablePart = matcher.group("braced");

                    TemplatePart part;
                    if (escapedPart != null) {
                        String literalPart = template.substring(curPos, spanStartPos) + DELIMITER;
                        part = new TemplatePart(literalPart, TemplatePartType.LITERAL);
                        curPos = spanEndPos;
                    } else if (variablePart != null) {
                        if (curPos < spanStartPos) {
                            part = new TemplatePart(template.substring(curPos, spanStartPos), TemplatePartType.LITERAL);
                            curPos = spanStartPos;
                        } else {
                            part = new TemplatePart(variablePart, TemplatePartType.VARIABLE);
                            curPos = spanEndPos;
                        }
                    } else {
                        part = new TemplatePart(template.substring(curPos, spanStartPos), TemplatePartType.LITERAL);
                        curPos = spanEndPos;
                    }
                    foundMatch = false; // Reset after processing a match
                    return part;
                } else {
                    TemplatePart part = new TemplatePart(template.substring(curPos), TemplatePartType.LITERAL);
                    curPos = template.length();
                    return part;
                }
            }
        };
    }

    /**
     * Handles invalid placeholder syntax within the template.
     *
     * @param position the starting position of the invalid placeholder.
     * @throws IllegalArgumentException if the placeholder syntax is invalid.
     */
    private void handleInvalid(int position) {
        String substringUpToPosition = template.substring(0, position);
        String[] lines = substringUpToPosition.split("\r?\n");
        int colno, lineno;
        if (lines.length == 0) {
            colno = 1;
            lineno = 1;
        } else {
            String lastLine = lines[lines.length - 1];
            // Adjust the column number for invalid placeholder
            colno = position - (substringUpToPosition.length() - lastLine.length()) - 1; // -1 to exclude the dollar sign
            lineno = lines.length;
        }
        throw new IllegalArgumentException(String.format("Invalid placeholder in template: line %d, col %d", lineno, colno));
    }

    /**
     * Represents a part of the template, which can either be a literal string
     * or a variable placeholder.
     */
    public static class TemplatePart {
        private final String part;
        private final TemplatePartType type;

        /**
         * Constructs a new TemplatePart with the given text and type.
         *
         * @param part the text of this part of the template.
         * @param type the type of this part of the template.
         */
        public TemplatePart(String part, TemplatePartType type) {
            this.part = part;
            this.type = type;
        }

        /**
         * Retrieves the text of this part of the template.
         *
         * @return the text of this template part.
         */
        public String getPart() {
            return part;
        }

        /**
         * Retrieves the type of this part of the template.
         *
         * @return the type of this template part.
         */
        public TemplatePartType getType() {
            return type;
        }

        public QueryFragment toFragment(Map<String, Object> args) {
            if (this.getType().equals(TemplatePartType.VARIABLE)) {
                if (Objects.isNull(args)) {
                    throw new IllegalArgumentException(
                            String.format("No args provided for Template variable %s.", this.getPart()));
                }
                if (args.containsKey(this.getPart())) {
                    // null values are valid, so can't use computeIfPresent / computeIfAbsent here.
                    var arg = args.get(this.getPart());
                    if (arg instanceof QueryFragment) {
                        return (QueryFragment) arg;
                    } else {
                        return new QueryVal<>(arg);
                    }
                } else {
                    throw new IllegalArgumentException(
                            String.format("Template variable %s not found in provided args.", this.getPart()));
                }
            } else {
                return new QueryLiteral(this.getPart());
            }
        }
    }

}
