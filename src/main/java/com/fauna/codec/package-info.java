/**
 * The {@code com.fauna.codec} package provides classes and interfaces for encoding and decoding data types used in
 * Fauna.
 * <p>
 * Key classes and interfaces:
 * <ul>
 *     <li>{@link com.fauna.codec.Codec} - An interface for implementing codecs that define encoding and decoding
 *     behavior.</li>
 *     <li>{@link com.fauna.codec.CodecProvider} - Provides codecs, allowing retrieval based on class and type
 *     arguments.</li>
 *     <li>{@link com.fauna.codec.CodecRegistry} - A registry for managing codecs.</li>
 *     <li>{@link com.fauna.codec.UTF8FaunaParser} - A custom parser for reading Fauna's wire format.</li>
 *     <li>{@link com.fauna.codec.UTF8FaunaGenerator} - A generator for writing data in Fauna's wire format.</li>
 *     <li>{@link com.fauna.codec.DefaultCodecProvider} - A standard codec provider implementation.</li>
 *     <li>{@link com.fauna.codec.Generic} - A utility class for generating codecs for generic types.</li>
 * </ul>
 */
package com.fauna.codec;
