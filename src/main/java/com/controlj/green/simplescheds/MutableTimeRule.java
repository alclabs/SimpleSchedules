package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.bacnet.data.datetime.*;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MutableTimeRule implements TimeRule, SimpleTime
{
   int hour;
   AnyRule hourRule = AnyRule.FIXED;
   int minute;
   AnyRule minuteRule = AnyRule.FIXED;
   int second;
   AnyRule secondRule = AnyRule.FIXED;
   int hundredth;
   AnyRule hundredthRule = AnyRule.FIXED;

   @Override
   public int getHour() throws UnsupportedOperationException
   {
      return hour;
   }

   public void setHour(int hour)
   {
      this.hour = hour;
   }

   @NotNull
   @Override
   public AnyRule getHourRule()
   {
      return hourRule;
   }

   public void setHourRule(AnyRule hourRule)
   {
      this.hourRule = hourRule;
   }

   @Override
   public int getMinute() throws UnsupportedOperationException
   {
      return minute;
   }

   public void setMinute(int minute)
   {
      this.minute = minute;
   }

   @NotNull
   @Override
   public AnyRule getMinuteRule()
   {
      return minuteRule;
   }

   public void setMinuteRule(AnyRule minuteRule)
   {
      this.minuteRule = minuteRule;
   }

   @Override
   public int getSecond() throws UnsupportedOperationException
   {
      return second;
   }

   public void setSecond(int second)
   {
      this.second = second;
   }

   @NotNull
   @Override
   public AnyRule getSecondRule()
   {
      return secondRule;
   }

   public void setSecondRule(AnyRule secondRule)
   {
      this.secondRule = secondRule;
   }

   @Override
   public int getHundredth() throws UnsupportedOperationException
   {
      return hundredth;
   }

   public void setHundredth(int hundredth)
   {
      this.hundredth = hundredth;
   }

   @NotNull
   @Override
   public AnyRule getHundredthRule()
   {
      return hundredthRule;
   }

   public void setHundredthRule(AnyRule hundredthRule)
   {
      this.hundredthRule = hundredthRule;
   }

   @Override
   public boolean isSimpleTime()
   {
      return getHourRule()==AnyRule.FIXED &&
             getMinuteRule()==AnyRule.FIXED &&
             getSecondRule()==AnyRule.FIXED &&
             getHundredthRule()==AnyRule.FIXED;
   }

   @NotNull
   @Override
   public SimpleTime toSimpleTime() throws UnsupportedOperationException
   {
      return this;
   }

   @Override
   public int compareTo(SimpleTime o)
   {
      return toDate().compareTo(o.toDate());
   }

   @NotNull
   @Override
   public Date toDate() throws UnsupportedOperationException
   {
      return new Date(getTimeInMilliseconds());
   }

   @NotNull
   @Override
   public Calendar toCalendar()
   {
      Calendar calendar = new GregorianCalendar();
      calendar.setTimeInMillis(getTimeInMilliseconds());
      return calendar;
   }

   private long getTimeInMilliseconds()
   {
      if(isSimpleTime())
         return (getHour()*60l*60l*1000l)+(getMinute()*60l*1000l)+(getSecond()*1000l)+(getHundredth()*10l);
      throw new RuntimeException("Can't get a date from a complex time rule");
   }

   public TimeRule createFactoryRule()
   {
      TimeRuleFactory factory = new TimeRuleFactory();

      factory.hour(hour);
      if(hourRule==AnyRule.ANY)
         factory.anyHour();

      factory.minute(minute);
      if(minuteRule==AnyRule.ANY)
         factory.anyMinute();

      factory.second(second);
      if(secondRule==AnyRule.ANY)
         factory.anySecond();

      factory.hundredth(hundredth);
      if(hundredthRule==AnyRule.ANY)
         factory.anyHundredth();

      return factory.create();
   }
}