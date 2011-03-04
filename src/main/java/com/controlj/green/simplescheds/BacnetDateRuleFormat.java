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

      String year_pattern = "([0-9]{4}|[*])";
      String month_pattern = "([0-9]{1,2}|[*]|even|odd)";
      String day_pattern = "([0-9]{1,2}|[*]|last)";

      //String testme = "([0-9]{4}|[*])/([01]?[0-9]|[*]|even|odd)/([0123]?[0-9]|[*]|last)( (sun|mon|tue|wed|thu|fri|sat))?";

      String days_of_week_pattern = "( (sun|mon|tue|wed|thu|fri|sat))?";
      Pattern SIMPLE_DATE_PATTERN = Pattern.compile(year_pattern+"/"+month_pattern+"/"+day_pattern+days_of_week_pattern, Pattern.CASE_INSENSITIVE);
      Matcher matcher = SIMPLE_DATE_PATTERN.matcher(dateString);
      if(matcher.find())
      {
         String yearStr = matcher.group(1);

         rule.setYearRule(YearRule.FIXED);
         setYearFromString(rule, yearStr);

         String monthStr = matcher.group(2);
         setMonthFromString(rule, monthStr);

         String dayStr = matcher.group(3);
         setDayFromString(rule, dayStr);

         if(matcher.groupCount()==5)
         {
            String dayOfWeekStr = matcher.group(5);
            setDayOfWeekFromString(rule, dayOfWeekStr);
         }
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

      if(rule.getDayRule()==DayRule.ANY)
         dateString.append("*");
      else if(rule.getDayRule()==DayRule.LAST_OF_MONTH)
         dateString.append("last");
      else if(rule.getDayRule()==DayRule.DAY_OF_WEEK)
      {
         String name = rule.getDayOfWeek().name();
         dateString.append("*");
         dateString.append(" ").append(name.charAt(0)).append(name.substring(1,3).toLowerCase());
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
