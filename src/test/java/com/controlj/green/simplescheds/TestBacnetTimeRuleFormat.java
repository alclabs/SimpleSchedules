package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.bacnet.data.datetime.*;
import junit.framework.TestCase;

import java.text.ParseException;

public class TestBacnetTimeRuleFormat extends TestCase
{
   public void testStringToTime() throws Exception
   {
      MutableTimeRule expected = new MutableTimeRule();

      BacnetTimeRuleFormat format = new BacnetTimeRuleFormat();

      expected.setHour(10);
      expected.setMinute(20);
      assertEquals(expected, format.parse("10:20"));

      expected.setSecond(30);
      assertEquals(expected, format.parse("10:20:30"));

      expected.setHundredth(40);
      assertEquals(expected, format.parse("10:20:30:40"));

      expected.setHour(0);
      expected.setHourRule(AnyRule.ANY);
      expected.setSecond(0);
      expected.setHundredth(0);
      assertEquals(expected, format.parse("*:20"));

      expected.setMinute(0);
      expected.setMinuteRule(AnyRule.ANY);
      assertEquals(expected, format.parse("*:*"));

      expected.setSecondRule(AnyRule.ANY);
      assertEquals(expected, format.parse("*:*:*"));

      expected.setHundredthRule(AnyRule.ANY);
      assertEquals(expected, format.parse("*:*:*:*"));

      try
      {
         format.parse("**:23");
         fail("boo, accepted a bad year value");
      }
      catch(ParseException e)
      {
         //yay it failed
      }

      try
      {
         format.parse("abc");
         fail("boo, accepted a bad value");
      }
      catch(ParseException e)
      {
         //yay it failed
      }
   }

   public void testTimeToString() throws Exception
   {
      BacnetTimeRuleFormat formater = new BacnetTimeRuleFormat();

      MutableTimeRule rule = new MutableTimeRule();
      rule.setHour(10);
      rule.setMinute(20);
      assertEquals("10:20", formater.format(rule));

      rule.setSecond(30);
      assertEquals("10:20", formater.format(rule));

      rule.setHundredth(40);
      assertEquals("10:20", formater.format(rule));

      rule.setHourRule(AnyRule.ANY);
      assertEquals("*:20", formater.format(rule));

      rule.setMinuteRule(AnyRule.ANY);
      assertEquals("*:*", formater.format(rule));

      rule.setSecondRule(AnyRule.ANY);
      assertEquals("*:*", formater.format(rule));

      rule.setHundredthRule(AnyRule.ANY);
      assertEquals("*:*", formater.format(rule));
   }


   public void assertEquals(TimeRule expected, TimeRule actual)
   {
      assertEquals("Hour, failed to match", expected.getHour(), actual.getHour());
      assertEquals("HourRule, failed to match", expected.getHourRule(), actual.getHourRule());
      assertEquals("Minute, failed to match", expected.getMinute(), actual.getMinute());
      assertEquals("MinuteRule, failed to match", expected.getMinuteRule(), actual.getMinuteRule());
      assertEquals("Second, failed to match", expected.getSecond(), actual.getSecond());
      assertEquals("SecondRule, failed to match", expected.getSecondRule(), actual.getSecondRule());
      assertEquals("Hundredth, failed to match", expected.getHundredth(), actual.getHundredth());
      assertEquals("HundredthRule, failed to match", expected.getHundredthRule(), actual.getHundredthRule());
   }

}