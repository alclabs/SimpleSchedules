package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.bacnet.data.datetime.*;
import junit.framework.TestCase;

import java.text.ParseException;
import java.util.Date;

public class TestBacnetDateRuleFormat extends TestCase
{
   public void testStringToDate() throws Exception
   {
      MutableDateRule expected = new MutableDateRule();
      BacnetDateRuleFormat format = new BacnetDateRuleFormat();

      expected.setYear(2011).setMonth(Month.DECEMBER).setDay(31);
      assertEquals(expected, format.parse("2011/12/31"));

      expected.setYear(2011).setMonth(Month.FEBRUARY).setDay(3);
      assertEquals(expected, format.parse("2011/02/03"));

      expected.setYear(2000).setYearRule(YearRule.ANY);
      assertEquals(expected, format.parse("*/02/03"));

      expected.setYear(2011).setYearRule(YearRule.FIXED);
      expected.setMonth(Month.JANUARY).setMonthRule(MonthRule.ANY);
      assertEquals(expected, format.parse("2011/*/03"));

      expected.setMonthRule(MonthRule.EVEN);
      assertEquals(expected, format.parse("2011/even/03"));

      expected.setMonthRule(MonthRule.ODD);
      assertEquals(expected, format.parse("2011/odd/03"));

      expected.setMonth(Month.FEBRUARY).setMonthRule(MonthRule.FIXED);
      expected.setDay(1).setDayRule(DayRule.ANY);
      assertEquals(expected, format.parse("2011/02/*"));

      expected.setDayRule(DayRule.LAST_OF_MONTH);
      assertEquals(expected, format.parse("2011/02/last"));

      expected.setDay(3).setDayRule(DayRule.DAY_OF_WEEK);
      assertEquals(expected, format.parse("2011/02/03 Sun"));

      expected.setDay(1).setDayRule(DayRule.DAY_OF_WEEK);
      expected.setDayOfWeek(DayOfWeek.MONDAY);
      assertEquals(expected, format.parse("2011/02/last Mon"));


      expected.setYear(2011).setYearRule(YearRule.FIXED);
      expected.setMonth(Month.DECEMBER).setMonthRule(MonthRule.FIXED);
      expected.setDay(5).setDayRule(DayRule.FIXED);
      //should handle an out of bounds value gracefully
      assertEquals(expected, format.parse("2011/23/50"));

      try
      {
         format.parse("2011/abc/3");
         fail("boo, accepted a bad month value");
      }
      catch(ParseException e)
      {
         //yay it failed
      }
   }

   public void testDateToString() throws Exception
   {
      MutableDateRule rule = new MutableDateRule();
      rule.setYear(2011).setMonth(Month.FEBRUARY).setDay(3);

      BacnetDateRuleFormat formater = new BacnetDateRuleFormat();
      assertEquals("2011/02/03", formater.format(rule));

      rule.setYear(2000).setYearRule(YearRule.ANY);
      assertEquals("*/02/03", formater.format(rule));

      rule.setYear(2011).setYearRule(YearRule.FIXED);
      rule.setMonth(Month.JANUARY).setMonthRule(MonthRule.ANY);
      assertEquals("2011/*/03", formater.format(rule));

      rule.setMonthRule(MonthRule.EVEN);
      assertEquals("2011/even/03", formater.format(rule));

      rule.setMonthRule(MonthRule.ODD);
      assertEquals("2011/odd/03", formater.format(rule));

      rule.setMonth(Month.FEBRUARY).setMonthRule(MonthRule.FIXED);
      rule.setDay(1).setDayRule(DayRule.ANY);
      assertEquals("2011/02/*", formater.format(rule));

      rule.setDayRule(DayRule.LAST_OF_MONTH);
      assertEquals("2011/02/last", formater.format(rule));

      rule.setDayRule(DayRule.DAY_OF_WEEK);
      rule.setDayOfWeek(DayOfWeek.MONDAY);
      assertEquals("2011/02/* Mon", formater.format(rule));
   }


   public void assertEquals(DateRule expected, DateRule actual)
   {
      assertEquals("Year, failed to match", expected.getYear(), actual.getYear());
      assertEquals("YearRule, failed to match", expected.getYearRule(), actual.getYearRule());
      assertEquals("Month, failed to match", expected.getMonth(), actual.getMonth());
      assertEquals("MonthRule, failed to match", expected.getMonthRule(), actual.getMonthRule());
      assertEquals("Day, failed to match", expected.getDay(), actual.getDay());
      assertEquals("DayRule, failed to match", expected.getDayRule(), actual.getDayRule());
      assertEquals("DayOfWeek, failed to match", expected.getDayOfWeek(), actual.getDayOfWeek());
   }

}
