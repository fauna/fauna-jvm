package com.fauna.beans;


import com.fauna.annotation.FaunaField;
import com.fauna.annotation.FaunaObject;
import com.fauna.enums.FaunaType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@FaunaObject
public class PersonWithTypeOverrides {

    // Long Conversions
    @FaunaField(name = "short_to_long", type = FaunaType.LONG)
    public Short shortToLong = 10;
    @FaunaField(name = "ushort_to_long", type = FaunaType.LONG)
    public Integer ushortToLong = 11;
    @FaunaField(name = "byte_to_long", type = FaunaType.LONG)
    public Byte byteToLong = 12;
    @FaunaField(name = "sbyte_to_long", type = FaunaType.LONG)
    public Byte sbyteToLong = 13;
    @FaunaField(name = "int_to_long", type = FaunaType.LONG)
    public Integer intToLong = 20;
    @FaunaField(name = "uint_to_long", type = FaunaType.LONG)
    public Long uintToLong = 21L;
    @FaunaField(name = "long_to_long", type = FaunaType.LONG)
    public Long longToLong = 30L;

    // Int Conversions
    @FaunaField(name = "short_to_int", type = FaunaType.INT)
    public Short shortToInt = 40;
    @FaunaField(name = "ushort_to_int", type = FaunaType.INT)
    public Short ushortToInt = 41;
    @FaunaField(name = "byte_to_int", type = FaunaType.INT)
    public Byte byteToInt = 42;
    @FaunaField(name = "sbyte_to_int", type = FaunaType.INT)
    public Byte sbyteToInt = 43;
    @FaunaField(name = "int_to_int", type = FaunaType.INT)
    public Integer intToInt = 50;

    // Double Conversions
    @FaunaField(name = "short_to_double", type = FaunaType.DOUBLE)
    public Short shortToDouble = 60;
    @FaunaField(name = "int_to_double", type = FaunaType.DOUBLE)
    public Integer intToDouble = 70;
    @FaunaField(name = "long_to_double", type = FaunaType.DOUBLE)
    public Long longToDouble = 80L;
    @FaunaField(name = "double_to_double", type = FaunaType.DOUBLE)
    public Double doubleToDouble = 10.1;
    @FaunaField(name = "float_to_double", type = FaunaType.DOUBLE)
    public Float floatToDouble = 1.3445f;

    // Bool conversions
    @FaunaField(name = "true_to_true", type = FaunaType.BOOLEAN)
    public Boolean trueToTrue = true;
    @FaunaField(name = "false_to_false", type = FaunaType.BOOLEAN)
    public Boolean falseToFalse = false;

    // String conversions
    @FaunaField(name = "class_to_string", type = FaunaType.STRING)
    public ThingWithStringOverride thingToString = new ThingWithStringOverride();
    @FaunaField(name = "string_to_string", type = FaunaType.STRING)
    public String stringToString = "aString";

    // Date conversions
    @FaunaField(name = "instant_to_date", type = FaunaType.DATE)
    public Instant instantToDate = Instant.parse("2023-12-13T12:12:12.001001Z");
    @FaunaField(name = "localdate_to_date", type = FaunaType.DATE)
    public LocalDate localDateToDate = LocalDate.of(2023, 12, 13);
    @FaunaField(name = "zoneddatetime_to_date", type = FaunaType.DATE)
    public ZonedDateTime zonedDateTimeToDate = ZonedDateTime.parse("2023-12-13T12:12:12.001001Z",
        DateTimeFormatter.ISO_DATE_TIME);

    // Time conversions
    @FaunaField(name = "instant_to_time", type = FaunaType.TIME)
    public Instant instantToTime = Instant.parse("2023-12-13T12:12:12.001001Z");
    @FaunaField(name = "offsetdatetime_to_time", type = FaunaType.TIME)
    public OffsetDateTime offsetDateTimeToTime = OffsetDateTime.parse("2023-12-13T12:12:12.001001Z",
        DateTimeFormatter.ISO_DATE_TIME);
    @FaunaField(name = "zoneddatetime_to_time", type = FaunaType.TIME)
    public ZonedDateTime zonedDateTimeToTime = ZonedDateTime.parse("2023-12-13T12:12:12.001001Z",
        DateTimeFormatter.ISO_DATE_TIME);
}
