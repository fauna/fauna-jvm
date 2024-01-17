package com.fauna.interfaces;

import com.fauna.common.enums.TokenType;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents a stream of JSON tokens that can be read sequentially.
 * This interface abstracts the process of iterating over a sequence of JSON tokens,
 * typically obtained from a JSON parsing process.
 */
public interface TokenStream {

    /**
     * Retrieves the next JSON token from the stream.
     *
     * @return The next JsonToken, or null if the end of the stream is reached.
     */
    TokenType nextToken() throws IOException;

    /**
     * Closes the token stream and releases any system resources associated with it.
     *
     * @throws IOException If an I/O error occurs.
     */
    void close() throws IOException;

    /**
     * Method that will try to convert value of current token to a String. JSON Strings map naturally; scalar values get converted to their textual representation. If representation can not be converted to a String value (including structured types like Objects and Arrays and null token), default value of null will be returned; no exceptions are thrown.
     *
     * @return String value current token is converted to, if possible; null otherwise
     */
    String getValueAsString();

    /**
     * Method that will try to convert value of current token to a Java int value. Numbers are coerced using default Java rules; booleans convert to 0 (false) and 1 (true), and Strings are parsed using default Java language integer parsing rules.
     * If representation can not be converted to an int (including structured type markers like start/end Object/Array) default value of 0 will be returned; no exceptions are thrown.
     *
     * @return int value current token is converted to, if possible; exception thrown otherwise
     */
    int getValueAsInt();

    /**
     * Method that will try to convert value of current token to a long. Numbers are coerced using default Java rules; booleans convert to 0 (false) and 1 (true), and Strings are parsed using default Java language integer parsing rules.
     * If representation can not be converted to a long (including structured type markers like start/end Object/Array) default value of 0L will be returned; no exceptions are thrown.
     *
     * @return long value current token is converted to, if possible; exception thrown otherwise
     */
    long getValueAsLong();

    /**
     * Method that will try to convert value of current token to a Java
     * <b>double</b>.
     * Numbers are coerced using default Java rules; booleans convert to 0.0 (false)
     * and 1.0 (true), and Strings are parsed using default Java language floating
     * point parsing rules.
     * <p>
     * If representation can not be converted to a double (including structured types
     * like Objects and Arrays),
     * default value of <b>0.0</b> will be returned; no exceptions are thrown.
     *
     * @return {@code double} value current token is converted to, if possible; exception thrown
     * otherwise
     */
    double getValueAsDouble();

    /**
     * Method that will try to convert value of current token to a
     * <b>boolean</b>.
     * JSON booleans map naturally; integer numbers other than 0 map to true, and
     * 0 maps to false
     * and Strings 'true' and 'false' map to corresponding values.
     * <p>
     * If representation can not be converted to a boolean value (including structured types
     * like Objects and Arrays),
     * default value of <b>false</b> will be returned; no exceptions are thrown.
     *
     * @return {@code boolean} value current token is converted to, if possible;
     */
    boolean getValueAsBoolean();

    /**
     * Method that will try to convert the value of the current token to a LocalDate.
     * This method is applicable when the current token represents a date value in
     * ISO-8601 format (e.g., "2023-11-20"). It attempts to parse the token's value
     * as a LocalDate.
     * <p>
     * If the current token's value is not a valid date representation, a runtime
     * exception is thrown.
     *
     * @return LocalDate representing the value of the current token, if applicable.
     * @throws RuntimeException if the current token's value cannot be converted to a LocalDate.
     */
    LocalDate getValueAsDate();

    /**
     * Method that will try to convert the value of the current token to an Instant.
     * This method is applicable when the current token represents a time value in
     * ISO-8601 format (e.g., "2023-11-20T13:33:10.300Z"). It attempts to parse the token's value
     * as an Instant, representing a point on the timeline in UTC.
     * <p>
     * If the current token's value is not a valid time representation, a runtime
     * exception is thrown.
     *
     * @return Instant representing the value of the current token, if applicable.
     * @throws RuntimeException if the current token's value cannot be converted to an Instant.
     */
    Instant getValueAsTime();
}
