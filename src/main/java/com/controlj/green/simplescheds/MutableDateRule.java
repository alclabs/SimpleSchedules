package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.bacnet.data.datetime.*;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MutableDateRule implements DateRule, SimpleDate
{
   int year=2000;
   YearRule yearRule=YearRule.FIXED;

   Month month=Month.JANUARY;
   MonthRule monthRule=MonthRule.FIXED;

   int day=1;
   DayRule dayRule=DayRule.FIXED;
   DayOfWeek dayOfWeek;

   int week;
   WeekRule weekRule;

   @Override
   public int getDay()
   {
      return day;
   }

   public MutableDateRule setDay(int day)
   {
      this.day = day;
      return this;
   }

   @NotNull
   @Override
   public YearRule getYearRule()
   {
      return yearRule;
   }

   public MutableDateRule setYearRule(YearRule yearRule)
   {
      this.yearRule = yearRule;
      return this;
   }

   @Override
   public int getYear()
   {
      return year;
   }

   public MutableDateRule setYear(int year)
   {
      this.year = year;
      return this;
   }

   @NotNull
   @Override
   public MonthRule getMonthRule()
   {
      return monthRule;
   }

   public MutableDateRule setMonthRule(MonthRule monthRule)
   {
      this.monthRule = monthRule;
      return this;
   }

   @NotNull
   @Override
   public Month getMonth()
   {
      return month;
   }

   public MutableDateRule setMonth(Month month)
   {
      this.month = month;
      return this;
   }

   @Override
   public int getMonthNum()
   {
      return month.getNum();
   }

   @NotNull
   @Override
   public DayRule getDayRule()
   {
      return dayRule;
   }

   public MutableDateRule setDayRule(DayRule dayRule)
   {
      this.dayRule = dayRule;
      return this;
   }

   @NotNull
   @Override
   public DayOfWeek getDayOfWeek()
   {
      if(isSimpleDate())
      {
         int dow =toCalendar().get(Calendar.DAY_OF_WEEK);
         return DayOfWeek.getForNum(dow);
      }
      else
      {
         if(dayOfWeek==null)
            return DayOfWeek.SUNDAY;
      }
      return dayOfWeek;
   }

   public MutableDateRule setDayOfWeek(DayOfWeek dayOfWeek)
   {
      this.dayOfWeek = dayOfWeek;
      return this;
   }

   public int getWeek()
   {
      return week;
   }

   public MutableDateRule setWeek(int week)
   {
      this.week=week;
      return this;
   }

   public WeekRule getWeekRule()
   {
      return weekRule;
   }

   public MutableDateRule setWeekRule(WeekRule weekRule)
   {
      this.weekRule = weekRule;
      return this;
   }

   @Override
   public int getDayOfWeekNumber()
   {
      return dayOfWeek.getNum();
   }

   @Override
   public boolean isSimpleDate()
   {
      return getDayRule()==DayRule.FIXED &&
             getMonthRule()==MonthRule.FIXED &&
             getYearRule()==YearRule.FIXED;
   }

   @NotNull
   @Override
   public SimpleDate toSimpleDate() throws UnsupportedOperationException
   {
      if(isSimpleDate())
         return this;
      throw new RuntimeException("Can't get a simple date rule from a complex date rule");
   }

   @NotNull
   public Date toDate()
   {
      if(isSimpleDate())
         return new Date(getYear(), getMonthNum(), getDay());
      throw new RuntimeException("Can't get a Date from a complex date rule");
   }

   @NotNull
   @Override
   public Calendar toCalendar()
   {
      return new GregorianCalendar(getYear(), getMonthNum(), getDay());
   }

   @Override
   public int compareTo(SimpleDate o)
   {
      return toDate().compareTo(o.toDate());
   }

   public DateRule createFactoryRule()
   {
      DateRuleFactory factory = new DateRuleFactory();

      factory.year(year);
      if(yearRule==YearRule.ANY)
         factory.anyYear();

      factory.month(month);
      if(monthRule==MonthRule.ANY)
         factory.anyMonth();
      if(monthRule==MonthRule.EVEN)
         factory.evenMonths();
      if(monthRule==MonthRule.ODD)
         factory.oddMonths();

      factory.day(day);
      if(dayRule==DayRule.ANY)
         factory.anyDay();
      if(dayRule==DayRule.DAY_OF_WEEK)
         factory.dayOfWeek(dayOfWeek);
      if(dayRule==DayRule.LAST_OF_MONTH)
         factory.lastDayOfMonth();

      return factory.create();
   }

   @Override
   public boolean equals(Object o)
   {
      if(this==o) return true;
      if(!(o instanceof MutableDateRule)) return false;

      MutableDateRule that = (MutableDateRule)o;

      if(day!=that.day) return false;
      if(week!=that.week) return false;
      if(year!=that.year) return false;
      if(dayOfWeek!=that.dayOfWeek) return false;
      if(dayRule!=that.dayRule) return false;
      if(month!=that.month) return false;
      if(monthRule!=that.monthRule) return false;
      if(weekRule!=that.weekRule) return false;
      if(yearRule!=that.yearRule) return false;

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = year;
      result = 31*result+(yearRule!=null ? yearRule.hashCode() : 0);
      result = 31*result+(month!=null ? month.hashCode() : 0);
      result = 31*result+(monthRule!=null ? monthRule.hashCode() : 0);
      result = 31*result+day;
      result = 31*result+(dayRule!=null ? dayRule.hashCode() : 0);
      result = 31*result+(dayOfWeek!=null ? dayOfWeek.hashCode() : 0);
      result = 31*result+week;
      result = 31*result+(weekRule!=null ? weekRule.hashCode() : 0);
      return result;
   }
}
