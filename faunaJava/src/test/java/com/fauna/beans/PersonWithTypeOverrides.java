package com.fauna.beans;


import com.fauna.annotation.FieldAttribute;
import com.fauna.annotation.ObjectAttribute;
import com.fauna.common.enums.FaunaType;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@ObjectAttribute
public class PersonWithTypeOverrides {

    // Long Conversions
    @FieldAttribute(name = "short_to_long", type = FaunaType.LONG)
    public Short shortToLong = 10;
    @FieldAttribute(name = "ushort_to_long", type = FaunaType.LONG)
    public Integer ushortToLong = 11;
    @FieldAttribute(name = "byte_to_long", type = FaunaType.LONG)
    public Byte byteToLong = 12;
    @FieldAttribute(name = "sbyte_to_long", type = FaunaType.LONG)
    public Byte sbyteToLong = 13;
    @FieldAttribute(name = "int_to_long", type = FaunaType.LONG)
    public Integer intToLong = 20;
    @FieldAttribute(name = "uint_to_long", type = FaunaType.LONG)
    public Long uintToLong = 21L;
    @FieldAttribute(name = "long_to_long", type = FaunaType.LONG)
    public Long longToLong = 30L;

    // Int Conversions
    @FieldAttribute(name = "short_to_int", type = FaunaType.INT)
    public Short shortToInt = 40;
    @FieldAttribute(name = "ushort_to_int", type = FaunaType.INT)
    public Short ushortToInt = 41;
    @FieldAttribute(name = "byte_to_int", type = FaunaType.INT)
    public Byte byteToInt = 42;
    @FieldAttribute(name = "sbyte_to_int", type = FaunaType.INT)
    public Byte sbyteToInt = 43;
    @FieldAttribute(name = "int_to_int", type = FaunaType.INT)
    public Integer intToInt = 50;

    // Double Conversions
    @FieldAttribute(name = "short_to_double", type = FaunaType.DOUBLE)
    public Short shortToDouble = 60;
    @FieldAttribute(name = "int_to_double", type = FaunaType.DOUBLE)
    public Integer intToDouble = 70;
    @FieldAttribute(name = "long_to_double", type = FaunaType.DOUBLE)
    public Long longToDouble = 80L;
    @FieldAttribute(name = "double_to_double", type = FaunaType.DOUBLE)
    public Double doubleToDouble = 10.1;
    @FieldAttribute(name = "float_to_double", type = FaunaType.DOUBLE)
    public Float floatToDouble = 1.3445f;

    // Bool conversions
    @FieldAttribute(name = "true_to_true", type = FaunaType.BOOLEAN)
    public Boolean trueToTrue = true;
    @FieldAttribute(name = "false_to_false", type = FaunaType.BOOLEAN)
    public Boolean falseToFalse = false;

    // String conversions
    @FieldAttribute(name = "class_to_string", type = FaunaType.STRING)
    public ThingWithStringOverride thingToString = new ThingWithStringOverride();
    @FieldAttribute(name = "string_to_string", type = FaunaType.STRING)
    public String stringToString = "aString";

    // Date conversions
    @FieldAttribute(name = "datetime_to_date", type = FaunaType.DATE)
    public Instant instantToDate = Instant.parse("2023-12-13T12:12:12.001001Z");
    @FieldAttribute(name = "dateonly_to_date", type = FaunaType.DATE)
    public LocalDate dateOnlyToDate = LocalDate.of(2023, 12, 13);
    @FieldAttribute(name = "datetimeoffset_to_date", type = FaunaType.DATE)
    public ZonedDateTime dateTimeOffsetToDate = ZonedDateTime.parse("2023-12-13T12:12:12.001001Z",
        DateTimeFormatter.ISO_DATE_TIME);

    // Time conversions
    @FieldAttribute(name = "datetime_to_time", type = FaunaType.TIME)
    public Instant InstantToTime = Instant.parse("2023-12-13T12:12:12.001001Z");
    @FieldAttribute(name = "datetimeoffset_to_time", type = FaunaType.TIME)
    public OffsetDateTime dateTimeOffsetToTime = OffsetDateTime.parse("2023-12-13T12:12:12.001001Z",
        DateTimeFormatter.ISO_DATE_TIME);
}
