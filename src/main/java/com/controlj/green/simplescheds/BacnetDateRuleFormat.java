package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.bacnet.data.datetime.*;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BacnetDateRuleFormat
{
   public MutableDateRule parse(String dateString) throws ParseException
   {
      MutableDateRule rule = new MutableDateRule();
      dateString = dateString.replace("^[^0-9*]+", "") //remove leading illegal characters
                             .replace("[^0-9a-zA-Z*]+$", "") //remove trailing illegal characters
                             .replace("[^0-9*a-zA-Z]+", "/");//assume illegal character sequences in the middle were meant to be slashes,

      String year_pattern = "[0-9]{4}|[*]";
      String month_pattern = "[0-9]{1,2}|[*]|even|odd";

      String dow_pattern = "sun|mon|tue|wed|thu|fri|sat";
      String week_pattern = "(1st|2nd|3rd|4th|last) ("+dow_pattern+")";
      String day_pattern = "[0-9]{1,2}|[*]|last|"+dow_pattern;

      //String testme = "([0-9]{4}|[*])/([01]?[0-9]|[*]|even|odd)/([0123]?[0-9]|[*]|last)( (sun|mon|tue|wed|thu|fri|sat))?";

      Pattern WEEK_PATTERN = Pattern.compile(week_pattern, Pattern.CASE_INSENSITIVE);
      Pattern SIMPLE_DATE_PATTERN = Pattern.compile("("+year_pattern+")/("+
            month_pattern+")/(("+
            week_pattern+")|("+
            day_pattern+"))", Pattern.CASE_INSENSITIVE);
      Matcher matcher = SIMPLE_DATE_PATTERN.matcher(dateString);
      if(matcher.find())
      {
         String yearStr = matcher.group(1);

         rule.setYearRule(YearRule.FIXED);
         setYearFromString(rule, yearStr);

         String monthStr = matcher.group(2);
         setMonthFromString(rule, monthStr);

         String dayStr = matcher.group(3);
         Matcher weekMatcher = WEEK_PATTERN.matcher(dayStr);
         if(weekMatcher.find())
            setWeekFromString(rule, weekMatcher.group(1), weekMatcher.group(2));
         else if(dayStr.toLowerCase().matches(dow_pattern))
            setDayOfWeekFromString(rule, dayStr);
         else
            setDayFromString(rule, dayStr);
      }
      else
      {
         throw new ParseException("Invalid date string \""+dateString+"\"", 0);
      }
      return rule;
   }

   private void setYearFromString(MutableDateRule rule, String yearStr)
   {
      if(yearStr.equals("*"))
         rule.setYearRule(YearRule.ANY);
      else
         rule.setYear(Integer.parseInt(yearStr));
   }

   private void setMonthFromString(MutableDateRule rule, String monthStr)
   {
      rule.setMonthRule(MonthRule.FIXED);
      if(monthStr.equals("*"))
      {
         rule.setMonthRule(MonthRule.ANY);
      }
      else if(monthStr.equals("even"))
      {
         rule.setMonthRule(MonthRule.EVEN);
      }
      else if(monthStr.equals("odd"))
      {
         rule.setMonthRule(MonthRule.ODD);
      }
      else
      {
         int month = Integer.parseInt(monthStr);

         //handle out of bounds reasonably
         if(month< 1) month= 1;
         if(month>12) month=12;

         rule.setMonth(Month.getForNum(month));
      }
   }

   private void setDayFromString(MutableDateRule rule, String dayStr)
   {
      if(dayStr.equals("*"))
         rule.setDayRule(DayRule.ANY);
      else if(dayStr.equals("last"))
         rule.setDayRule(DayRule.LAST_OF_MONTH);
      else
      {
         int day = Integer.parseInt(dayStr);
         if(day<1) day= 1;
         while(day>31)
         {
            day=Integer.parseInt(dayStr.substring(0, dayStr.length()-1));
         }

         rule.setDay(day);
      }
   }

   private void setWeekFromString(MutableDateRule rule, String weekStr, String dowStr)
   {
      rule.setWeekRule(WeekRule.FIXED);
      if(weekStr.startsWith("1"))
         rule.setWeek(1);
      else if(weekStr.startsWith("2"))
         rule.setWeek(2);
      else if(weekStr.startsWith("3"))
         rule.setWeek(3);
      else if(weekStr.startsWith("4"))
         rule.setWeek(4);
      else if(weekStr.startsWith("last"))
         rule.setWeekRule(WeekRule.LAST);

      setDayOfWeekFromString(rule, dowStr);
   }

   private void setDayOfWeekFromString(MutableDateRule rule, String dayOfWeekStr)
   {
      if(dayOfWeekStr!=null)
      {
         dayOfWeekStr=dayOfWeekStr.toLowerCase();
         if(dayOfWeekStr.length()>0)
         {
            if(dayOfWeekStr.equals("sun"))
               rule.setDayOfWeek(DayOfWeek.SUNDAY);
            else if(dayOfWeekStr.equals("mon"))
               rule.setDayOfWeek(DayOfWeek.MONDAY);
            else if(dayOfWeekStr.equals("tue"))
               rule.setDayOfWeek(DayOfWeek.TUESDAY);
            else if(dayOfWeekStr.equals("wed"))
               rule.setDayOfWeek(DayOfWeek.WEDNESDAY);
            else if(dayOfWeekStr.equals("thu"))
               rule.setDayOfWeek(DayOfWeek.THURSDAY);
            else if(dayOfWeekStr.equals("fri"))
               rule.setDayOfWeek(DayOfWeek.FRIDAY);
            else if(dayOfWeekStr.equals("sat"))
               rule.setDayOfWeek(DayOfWeek.SATURDAY);

            rule.setDayRule(DayRule.DAY_OF_WEEK);
         }
      }
   }

   public String format(DateRule rule)
   {
      if(rule instanceof MutableDateRule)
      {
         MutableDateRule mRule = (MutableDateRule)rule;
         return format(rule, mRule.getWeek(), mRule.getWeekRule());
      }
      else
      {
         return format(rule, 0, WeekRule.ANY);
      }
   }

   public String format(DateRule rule, int week, WeekRule weekRule)
   {
      StringBuilder dateString = new StringBuilder();
      if(rule.getYearRule()==YearRule.ANY)
         dateString.append("*").append("/");
      else
         dateString.append(rule.getYear()).append("/");

      if(rule.getMonthRule()==MonthRule.ANY)
         dateString.append("*").append("/");
      else if(rule.getMonthRule()==MonthRule.EVEN)
         dateString.append("even").append("/");
      else if(rule.getMonthRule()==MonthRule.ODD)
         dateString.append("odd").append("/");
      else
         dateString.append(padMonth(rule.getMonth().getNum())).append("/");

      //if we have specified the day, then the week rule is meaningless
      if(rule.getDayRule()!=DayRule.FIXED)
      {
         if(weekRule==WeekRule.FIXED)
         {
            if(week==1)
               dateString.append("1st ");
            if(week==2)
               dateString.append("2nd ");
            if(week==3)
               dateString.append("3rd ");
            if(week==4)
               dateString.append("4th ");
         }
         else if(weekRule==WeekRule.LAST)
         {
            dateString.append("last ");
         }
      }

      if(rule.getDayRule()==DayRule.ANY)
         dateString.append("*");
      else if(rule.getDayRule()==DayRule.LAST_OF_MONTH)
         dateString.append("last");
      else if(rule.getDayRule()==DayRule.DAY_OF_WEEK)
      {
         String name = rule.getDayOfWeek().name();
         dateString.append(name.charAt(0)).append(name.substring(1,3).toLowerCase());
      }
      else
         dateString.append(padDay(rule.getDay()));

      return dateString.toString();
   }

   private String padMonth(int month)
   {
      return pad(month, 2);
   }

   private String padDay(int day)
   {
      return pad(day, 2);
   }

   private String pad(int number, int digits)
   {
      StringBuilder str = new StringBuilder(String.valueOf(number));
      while(str.length()<digits)
         str.insert(0, "0");
      return str.toString();
   }


}
