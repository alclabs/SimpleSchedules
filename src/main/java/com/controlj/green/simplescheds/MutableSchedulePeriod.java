package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.access.schedule.SchedulePeriod;
import com.controlj.green.addonsupport.access.schedule.ScheduleValue;
import com.controlj.green.addonsupport.bacnet.data.datetime.SimpleTime;
import com.controlj.green.addonsupport.bacnet.data.datetime.TimeRule;

/**
 * Created by IntelliJ IDEA.
 * User: tirish
 * Date: Jul 25, 2011
 * Time: 9:44:03 AM
 */
public class MutableSchedulePeriod<T> implements SchedulePeriod<T>, Comparable<MutableSchedulePeriod<T>>
{
   boolean occupied = true;
   MutableTimeRule start = new MutableTimeRule();
   MutableTimeRule end = new MutableTimeRule();

   public MutableSchedulePeriod()
   {
      setStartTime(0, 0);
      setEndTime(24, 0);
   }

   public MutableSchedulePeriod(SchedulePeriod period)
   {
      setStartTime(period.getStartTime());
      setEndTime(period.getEndTime());
      if(period.getValue()==null)
        setOccupied(false);
      else
         setOccupied(Boolean.TRUE.equals(period.getValue().getValue()));
   }

   @org.jetbrains.annotations.NotNull
   @Override
   public SimpleTime getStartTime()
   {
      return start;
   }

   public MutableSchedulePeriod setStartTime(TimeRule time)
   {
      setStartTime(time.getHour(), time.getMinute());
      return this;
   }

   public MutableSchedulePeriod setStartTime(int hours, int minutes)
   {
      this.start.setHour(hours);
      this.start.setMinute(minutes);
      return this;
   }
   public MutableSchedulePeriod setStartOfDay()
   {
      setStartTime(0,0);
      return this;
   }

   @org.jetbrains.annotations.NotNull
   @Override
   public SimpleTime getEndTime()
   {
      return end;
   }

   public MutableSchedulePeriod setEndTime(TimeRule time)
   {
      setEndTime(time.getHour(), time.getMinute());
      return this;
   }

   public MutableSchedulePeriod setEndTime(int hours, int minutes)
   {
      this.end.setHour(hours);
      this.end.setMinute(minutes);
      return this;
   }

   public MutableSchedulePeriod setEndOfDay()
   {
      setEndTime(24, 0);
      return this;
   }

   public boolean isOccupied()
   {
      return occupied;
   }
   
   public MutableSchedulePeriod setOccupied(boolean occupied)
   {
      this.occupied=occupied;
      return this;
   }

   @org.jetbrains.annotations.NotNull
   @Override
   public ScheduleValue getValue()
   {
      throw new RuntimeException("Invalid Method: use isOccupied() instead");
   }

   public MutableSchedulePeriod setValue(ScheduleValue value)
   {
      throw new RuntimeException("Invalid Method: use setOccupied() instead");
   }

   public T getRawValue()
   {
      throw new RuntimeException("Invalid Method: use isOccupied() instead");
   }

   public int compareTo(MutableSchedulePeriod<T> o)
   {
      int cmp = start.compareTo(o.start);
      if (cmp != 0) return cmp;

      cmp = end.compareTo(o.end);
      if (cmp != 0) return cmp;

      return 0;
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MutableSchedulePeriod that = (MutableSchedulePeriod) o;
      return start.equals(that.start) &&
             end.equals(that.end) &&
             occupied==that.occupied;
   }

   @Override
   public int hashCode()
   {
      int result = start.hashCode();
      result = 31 * result + end.hashCode();
      result = 31 * result + (occupied?Boolean.TRUE.hashCode():Boolean.FALSE.hashCode());
      return result;
   }

   public String toString()
   {
      return getStartTime().getHour()+":"+getStartTime().getMinute()+"-"+getEndTime().getHour()+":"+getEndTime().getMinute()+" = "+isOccupied();
   }
}
