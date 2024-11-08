/**
 * The {@code com.fauna.mapping} package provides classes and utilities used to
 * map Fauna document fields for serialization and deserialization in the client.
 *
 * <p>The classes in this package include:
 * <ul>
 *   <li>{@link com.fauna.mapping.FieldInfo}: Holds metadata for individual fields, such as name,
 *   type, and codec, used to map and handle fields within a Fauna data model.</li>
 *
 *   <li>{@link com.fauna.mapping.FieldName}: Provides utility methods for handling field names,
 *   including a method to convert names to a canonical format.</li>
 *
 *   <li>{@link com.fauna.mapping.FieldType}: Defines various field types that can exist within
 *   Fauna mappings, such as identifiers, timestamps, and general-purpose fields.</li>
 * </ul>
 */
package com.fauna.mapping;
