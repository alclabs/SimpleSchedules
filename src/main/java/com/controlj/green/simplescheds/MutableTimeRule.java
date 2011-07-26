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

   public MutableTimeRule setHour(int hour)
   {
      this.hour = hour;
      return this;
   }

   @NotNull
   @Override
   public AnyRule getHourRule()
   {
      return hourRule;
   }

   public MutableTimeRule setHourRule(AnyRule hourRule)
   {
      this.hourRule = hourRule;
      return this;
   }

   @Override
   public int getMinute() throws UnsupportedOperationException
   {
      return minute;
   }

   public MutableTimeRule setMinute(int minute)
   {
      this.minute = minute;
      return this;
   }

   @NotNull
   @Override
   public AnyRule getMinuteRule()
   {
      return minuteRule;
   }

   public MutableTimeRule setMinuteRule(AnyRule minuteRule)
   {
      this.minuteRule = minuteRule;
      return this;
   }

   @Override
   public int getSecond() throws UnsupportedOperationException
   {
      return second;
   }

   public MutableTimeRule setSecond(int second)
   {
      this.second = second;
      return this;
   }

   @NotNull
   @Override
   public AnyRule getSecondRule()
   {
      return secondRule;
   }

   public MutableTimeRule setSecondRule(AnyRule secondRule)
   {
      this.secondRule = secondRule;
      return this;
   }

   @Override
   public int getHundredth() throws UnsupportedOperationException
   {
      return hundredth;
   }

   public MutableTimeRule setHundredth(int hundredth)
   {
      this.hundredth = hundredth;
      return this;
   }

   @NotNull
   @Override
   public AnyRule getHundredthRule()
   {
      return hundredthRule;
   }

   public MutableTimeRule setHundredthRule(AnyRule hundredthRule)
   {
      this.hundredthRule = hundredthRule;
      return this;
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

   @Override
   public boolean equals(Object o)
   {
      if(this==o) return true;
      if(!(o instanceof MutableTimeRule)) return false;

      MutableTimeRule that = (MutableTimeRule)o;

      if(hour!=that.hour) return false;
      if(hundredth!=that.hundredth) return false;
      if(minute!=that.minute) return false;
      if(second!=that.second) return false;
      if(hourRule!=that.hourRule) return false;
      if(hundredthRule!=that.hundredthRule) return false;
      if(minuteRule!=that.minuteRule) return false;
      if(secondRule!=that.secondRule) return false;

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = hour;
      result = 31*result+(hourRule!=null ? hourRule.hashCode() : 0);
      result = 31*result+minute;
      result = 31*result+(minuteRule!=null ? minuteRule.hashCode() : 0);
      result = 31*result+second;
      result = 31*result+(secondRule!=null ? secondRule.hashCode() : 0);
      result = 31*result+hundredth;
      result = 31*result+(hundredthRule!=null ? hundredthRule.hashCode() : 0);
      return result;
   }
}