package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.bacnet.data.datetime.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BacnetTimeRuleFormat
{
   public MutableTimeRule parse(String timeString) throws ParseException
   {
      try
      {
         timeString = timeString.replace("^[^0-9*]+", "") //remove leading illegal characters
                                .replace("[^0-9*]+$", "") //remove trailing illegal characters
                                .replace("[^0-9*]+", ":");//assume illegal character sequences in the middle were meant to be colons,
         Pattern SIMPLE_TIME_PATTERN = Pattern.compile("([0-9*]{1,2}):([0-9*]{1,2})(:([0-9*]{1,2}))?(:([0-9*]{1,2}))?");
         Matcher matcher = SIMPLE_TIME_PATTERN.matcher(timeString);
         if(matcher.find())
         {
            MutableTimeRule rule = new MutableTimeRule();

            String hourStr = matcher.group(1);
            if(hourStr.equals("*"))
               rule.setHourRule(AnyRule.ANY);
            else
               rule.setHour(Integer.parseInt(hourStr));

            String minuteStr = matcher.group(2);
            if(minuteStr.equals("*"))
               rule.setMinuteRule(AnyRule.ANY);
            else
               rule.setMinute(Integer.parseInt(minuteStr));

            String secondStr = matcher.group(4);
            if(secondStr!=null && secondStr.length()>0)
            {
               if(secondStr.equals("*"))
                  rule.setSecondRule(AnyRule.ANY);
               else
                  rule.setSecond(Integer.parseInt(secondStr));
            }
            else
            {
               rule.setSecond(0);
            }


            String hundredthStr = matcher.group(6);
            if(hundredthStr!=null && hundredthStr.length()>0)
            {
               if(hundredthStr.equals("*"))
                  rule.setHundredthRule(AnyRule.ANY);
               else
                  rule.setHundredth(Integer.parseInt(hundredthStr));
            }
            else
            {
               rule.setHundredth(0);
            }
            return rule;
         }
      }
      catch(NumberFormatException e)
      {
         //bad number, fall through to parse exception
      }
      throw new ParseException("Invalid time string \""+timeString+"\"", 0);
   }

   public String format(TimeRule rule)
   {
      StringBuilder timeString = new StringBuilder();
      if(rule.getHourRule()==AnyRule.ANY)
         timeString.append("*");
      else
         timeString.append(rule.getHour());

      timeString.append(":");

      if(rule.getMinuteRule()==AnyRule.ANY)
         timeString.append("*");
      else
         timeString.append(pad(rule.getMinute(), 2));

      return timeString.toString();
   }

   private String pad(int number, int digits)
   {
      StringBuilder str = new StringBuilder(String.valueOf(number));
      while(str.length()<digits)
         str.insert(0, "0");
      return str.toString();
   }

   protected TimeRuleFactory getFactory()
   {
      return new TimeRuleFactory();
   }
}