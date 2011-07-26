package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.access.schedule.SchedulePeriod;
import com.controlj.green.addonsupport.access.schedule.ScheduleTemplate;
import com.controlj.green.addonsupport.access.schedule.template.Continuous;
import com.controlj.green.addonsupport.access.value.WritePrivilegeException;
import com.controlj.green.addonsupport.bacnet.data.datetime.TimeRule;
import com.controlj.green.addonsupport.bacnet.data.datetime.TimeRuleFactory;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: tirish
 * Date: Jul 22, 2011
 * Time: 1:31:42 PM
 */
public class SinglePeriodScheduler
{
   SortedSet<MutableSchedulePeriod> mutablePeriods = new TreeSet<MutableSchedulePeriod>();
   boolean holiday;
   boolean continuous;

   public SinglePeriodScheduler(SortedSet<MutableSchedulePeriod> periods, boolean holiday, boolean continuous)
   {
      this.mutablePeriods = periods;
      this.holiday=holiday;
      this.continuous=continuous;
   }

   public SinglePeriodScheduler(ScheduleTemplate schedule)
   {
      holiday = schedule.getPriority().getIndex()==1;
      continuous = (schedule instanceof Continuous);
      SortedSet<SchedulePeriod> periods = schedule.getPeriods();
      for(SchedulePeriod period : periods)
      {
         mutablePeriods.add(createMutablePeriod(period));
      }
   }

   public boolean isHoliday()
   {
      return holiday;
   }

   public boolean isContinuous()
   {
      return continuous;
   }

   public boolean isMultiplePeriods()
   {
      if(isHoliday())
         return isMultipleUndefinedPeriods();
      else
         return isMultipleOccupiedPeriods();
   }

   public SchedulePeriod getSinglePeriod()
   {
      if(isHoliday())
         return getSingleUndefinedPeriod();
      else
         return getSingleOccupiedPeriod();
   }

   private SchedulePeriod getSingleOccupiedPeriod()
   {
      SortedSet<MutableSchedulePeriod> periods = getMutablePeriods();
      for(MutableSchedulePeriod period : periods)
      {
         if(period.isOccupied())
         {
            //Use the first occupied schedule period.
            //This "Simple" scheduler will not handle more than one
            return period;
         }
      }
      return null;
   }

   private MutableSchedulePeriod getSingleUndefinedPeriod()
   {
      //Synthesize an undefined period
      //this period represents the section of the schedule that is occupied
      MutableSchedulePeriod period = new MutableSchedulePeriod();

      SortedSet<MutableSchedulePeriod> periods = getMutablePeriods();
      if(periods.size()==0)
      {
         if(continuous)
            return null;
         else
            return period.setStartOfDay().setEndOfDay().setOccupied(true);
      }
      
      SchedulePeriod firstUnoccupied = null;
      SchedulePeriod secondUnoccupied = null;
      for(MutableSchedulePeriod p : periods)
      {
         if(firstUnoccupied==null)
            firstUnoccupied = p;
         else if(secondUnoccupied==null)
         {
            secondUnoccupied = p;
            break;
         }
      }


      if(firstUnoccupied!=null)
      {
         if(isStartOfDay(firstUnoccupied.getStartTime()))
         {
            TimeRule endTime = firstUnoccupied.getEndTime();
            if(isEndOfDay(endTime))
            {
               //there is no undefined section, the schedule is fully unoccupied
               return null;
            }
            else if(secondUnoccupied==null)
            {
               //the undefined section goes from the end of the first
               //unoccupied to the end of the schedule
               return period.setStartTime(firstUnoccupied.getEndTime()).setEndOfDay().setOccupied(true);
            }
            else
            {
               //the undefined section goes from the end of the first
               //unoccupied section to the start of the second
               return period.setStartTime(firstUnoccupied.getEndTime())
                     .setEndTime(secondUnoccupied.getStartTime()).setOccupied(true);
            }
         }
         else
         {
            //the undefined section goes from the start of the day
            //to the start of the first unoccupied section
            return period.setStartOfDay()
                  .setEndTime(firstUnoccupied.getStartTime()).setOccupied(true);
         }
      }

      //the whole day is undefined
      return null;
   }

   public void setDefaultPeriod()
   {
      clearPeriods();
      if(!isContinuous())
      {
         MutableSchedulePeriod period = new MutableSchedulePeriod();
         if(isHoliday())
         {
            period.setStartOfDay().setEndOfDay();
            period.setOccupied(false);
         }
         else
         {
            period.setStartTime(8,0).setEndTime(17, 0);
         }
         addPeriod(period);
      }
   }

   private TimeRule findDefaultStartTime()
   {
      SchedulePeriod defaultPeriod = getSinglePeriod();
      if(defaultPeriod==null)
        return new MutableTimeRule().setHour(0).setMinute(0);
      else
      {
         return defaultPeriod.getStartTime();
      }
   }

   private TimeRule findDefaultEndTime()
   {
      SchedulePeriod defaultPeriod = getSinglePeriod();
      if(defaultPeriod==null)
        return new MutableTimeRule().setHour(24).setMinute(0);
      else
        return defaultPeriod.getEndTime();
   }

   public void setSinglePeriod(TimeRule start, TimeRule end)
   {
      //if we are given nothing, we should change nothing
      if(start==null && end==null)
      {
         return;
      }

      //try to use the existing values as the default
      if(start==null)
         start=findDefaultStartTime();

      if(end==null)
         end=findDefaultEndTime();

      clearPeriods();

      if(isHoliday())
         setHolidayPeriod(start, end);
      else
         setOccupiedPeriod(start, end);
   }

   private void setHolidayPeriod(TimeRule start, TimeRule end)
   {
      if(isEmptyDay(start, end))
      {
         //if 0:00 --> 0:00 fill in as fully unoccupied
         MutableSchedulePeriod fullDay = new MutableSchedulePeriod();
         fullDay.setStartOfDay().setEndOfDay().setOccupied(false);
         addPeriod(fullDay);
      }
      else
      {
         //holiday schedules fill in the periods around the undefined period with unoccupied
         if(!isStartOfDay(start))
         {
            MutableSchedulePeriod unoccupiedPeriod = new MutableSchedulePeriod();
            unoccupiedPeriod.setStartOfDay().setEndTime(start).setOccupied(false);
            addPeriod(unoccupiedPeriod);
         }
         if(!isEndOfDay(end))
         {
            MutableSchedulePeriod unoccupiedPeriod = new MutableSchedulePeriod();
            unoccupiedPeriod.setStartTime(end).setEndOfDay().setOccupied(false);
            addPeriod(unoccupiedPeriod);
         }
      }
   }

   private void setOccupiedPeriod(TimeRule start, TimeRule end)
   {
      MutableSchedulePeriod period = new MutableSchedulePeriod();
      period.setStartTime(start).setEndTime(end).setOccupied(true);
      if(!isEmptyDay(start, end))
         addPeriod(period);
   }

   private boolean isEmptyDay(TimeRule start, TimeRule end)
   {
      int startMinutes = getTimeInMinutes(start);
      int endMinutes = getTimeInMinutes(end);
      return startMinutes==0 && endMinutes==0;
   }

   private boolean isStartOfDay(TimeRule start)
   {
      int startMinutes = getTimeInMinutes(start);
      return startMinutes==0;
   }

   private boolean isEndOfDay(TimeRule end)
   {
      //special, 0:00 is translated as 24 hr, but ONLY for end times
      //this is because a full day schedule is considered as being from (O:00 --> 0:00)
      //by the Schedule API but we want to use the more clear (0:00 --> 24:00) time
      int endMinutes = getTimeInMinutes(end);
      return endMinutes==0 || endMinutes==(60*24);
   }

   private int getTimeInMinutes(TimeRule date)
   {
      return (date.getHour()*60)+date.getMinute();
   }

   private boolean isMultipleOccupiedPeriods()
   {
      SortedSet<MutableSchedulePeriod> periods = getMutablePeriods();
      SchedulePeriod occupied = null;
      for(MutableSchedulePeriod period : periods)
      {
         if(period.isOccupied())
         {
            //if we have already found a non-null period, this schedule is too complicated to edit using this "Simple" scheduler
            if(occupied!=null)
               return true;
            else
               occupied = period;
         }
      }
      return false;
   }

   private boolean isMultipleUndefinedPeriods()
   {
      boolean found=false;
      SortedSet<MutableSchedulePeriod> periods = getMutablePeriods();
      for(MutableSchedulePeriod period : periods)
      {
         if(!period.isOccupied())
         {
            if(found)
            {
               //we found a second gap if there was already a gap and there is a gap between
               //this new period and the end of the day (or another period)
               return !isEndOfDay(period.getEndTime());
            }
            else
            {
               //we found a gap if this is the first period
               //and it did not start at the beginning of the day
               found = !isStartOfDay(period.getStartTime());

               //we found a second gap if this period does not go to the end either
               if(found && !isEndOfDay(period.getEndTime()))
                  return true;
            }
         }
         else
         {
            //if the value of the period is not occupied, assume it is an undefined period
            if(found)
               return true;
            else
               found=true;
         }
      }
      return false;
   }

   private void clearPeriods()
   {
      mutablePeriods.clear();
   }

   private void addPeriod(MutableSchedulePeriod period)
   {
      mutablePeriods.add(period);
   }

   public SortedSet <MutableSchedulePeriod> getMutablePeriods()
   {
      return mutablePeriods;
   }

   private MutableSchedulePeriod createMutablePeriod(SchedulePeriod period)
   {
      MutableSchedulePeriod mutable = new MutableSchedulePeriod(period);
      if(isEndOfDay(period.getEndTime()))
         mutable.setEndTime(24, 0);
      return mutable;
   }

   public void saveTo(ScheduleTemplate schedule) throws WritePrivilegeException
   {
      schedule.clearPeriods();
      for(MutableSchedulePeriod period : mutablePeriods)
      {
         if(isEndOfDay(period.getEndTime()))
         {
            SchedulePeriod newPeriod = schedule.makePeriod().from(period.getStartTime().getHour(), period.getStartTime().getMinute())
                  .toEndOfDay().useValue(period.isOccupied());
            schedule.addPeriod(newPeriod);
         }
         else
         {
            SchedulePeriod newPeriod = schedule.makePeriod().from(period.getStartTime().getHour(), period.getStartTime().getMinute())
                  .to(period.getEndTime().getHour(), period.getEndTime().getMinute()).useValue(period.isOccupied());
            schedule.addPeriod(newPeriod);
         }
      }
   }
}
