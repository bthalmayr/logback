/**
 * LOGBack: the reliable, fast and flexible logging library for Java.
 * 
 * Copyright (C) 1999-2006, QOS.ch
 * 
 * This library is free software, you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation.
 */

package ch.qos.logback.core.rolling.helper;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import ch.qos.logback.core.Context;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.pattern.parser.ScanException;
import ch.qos.logback.core.pattern.util.AlmostAsIsEscapeUtil;
import ch.qos.logback.core.spi.ContextAwareBase;


/**
 * 
 * After parsing file name patterns, given a number or a date, instances of this class 
 * can be used to compute a file name according to the file name pattern and the given 
 * integer or date.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * 
 */
public class FileNamePattern extends ContextAwareBase {

  static final Map<String, String> CONVERTER_MAP = new HashMap<String, String>();
  static {
    CONVERTER_MAP.put("i", IntegerTokenConverter.class.getName());
    CONVERTER_MAP.put("d", DateTokenConverter.class.getName());
  }

  String pattern;
  Converter<Object> headTokenConverter;

  public FileNamePattern(String patternArg, Context contextArg) {
    setPattern(patternArg);
    setContext(contextArg);
    parse();
    ConverterUtil.startConverters(this.headTokenConverter);
  }

  void parse() {
    try {
      Parser<Object> p = new Parser<Object>(pattern, new AlmostAsIsEscapeUtil());
      p.setContext(context);
      Node t = p.parse();
      this.headTokenConverter = p.compile(t, CONVERTER_MAP);

    } catch (ScanException sce) {
      addError("Failed to parse pattern \"" + pattern + "\".", sce);
    }
  }

  public String toString() {
    return pattern;
  }

  public DateTokenConverter getDateTokenConverter() {
    Converter p = headTokenConverter;

    while (p != null) {
      if (p instanceof DateTokenConverter) {
        return (DateTokenConverter) p;
      }

      p = p.getNext();
    }

    return null;
  }

  public IntegerTokenConverter getIntegerTokenConverter() {
    Converter p = headTokenConverter;

    while (p != null) {
      if (p instanceof IntegerTokenConverter) {
        return (IntegerTokenConverter) p;
      }

      p = p.getNext();
    }
    return null;
  }

  public String convert(Object o) {
    Converter<Object> p = headTokenConverter;
    StringBuffer buf = new StringBuffer();
    while (p != null) {
      buf.append(p.convert(o));
      p = p.getNext();
    }
    return buf.toString();
  }

  public String convertInt(int i) {
    Integer integerArg = new Integer(i);
    return convert(integerArg);
  }

  public String convertDate(Date date) {
    return convert(date);
  }

  public void setPattern(String pattern) {
    if (pattern != null) {
      // Trailing spaces in the pattern are assumed to be undesired.
      this.pattern = pattern.trim();
    }
  }

  public String getPattern() {
    return pattern;
  }
}
