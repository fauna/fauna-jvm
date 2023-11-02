package com.fauna.query.template;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public FaunaTemplate(String template) {
        this.template = template;
    }

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
                        curPos = spanStartPos;
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

    public static class TemplatePart {
        private final String part;
        private final TemplatePartType type;

        public TemplatePart(String part, TemplatePartType type) {
            this.part = part;
            this.type = type;
        }

        public String getPart() {
            return part;
        }

        public TemplatePartType getType() {
            return type;
        }
    }

}
