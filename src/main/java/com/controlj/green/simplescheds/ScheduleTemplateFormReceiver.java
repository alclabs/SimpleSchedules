package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.access.schedule.Schedule;
import com.controlj.green.addonsupport.access.schedule.SchedulePeriod;
import com.controlj.green.addonsupport.access.schedule.ScheduleTemplate;
import com.controlj.green.addonsupport.access.schedule.ScheduleTemplateHandler;
import com.controlj.green.addonsupport.access.schedule.template.*;
import com.controlj.green.addonsupport.access.value.WritePrivilegeException;
import com.controlj.green.addonsupport.bacnet.data.datetime.*;
import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.*;

public class ScheduleTemplateFormReceiver implements ScheduleTemplateHandler
{
   Map<String, String> parameterValuesMap = new HashMap<String, String>();

   //error conditions
   boolean permissionFailure=false;
   List<String> badValues = new ArrayList();

   public ScheduleTemplateFormReceiver(Map parameterValuesMap)
   {
      this.parameterValuesMap = parameterValuesMap;
   }

   private boolean hasParameter(String key)
   {
      return parameterValuesMap.containsKey(key);
   }

   private String getParameter(String key)
   {
      return parameterValuesMap.get(key);
   }

   protected boolean isUpdate(String newValue, String oldValue)
   {
      return newValue!=null && !newValue.equals(oldValue);
   }

   protected boolean isUpdate(DateRule newValue, DateRule oldValue)
   {
      return newValue!=null && !newValue.equals(oldValue);
   }

   protected boolean isUpdate(TimeRule newValue, TimeRule oldValue)
   {
      return newValue!=null && !newValue.equals(oldValue);
   }

   public void setOccupiedPeriod(ScheduleTemplate schedule) throws WritePrivilegeException
   {
      TimeRule start = createTimeRule(ScheduleTemplateFormGenerator.FIELD_START_TIME);
      TimeRule end = createTimeRule(ScheduleTemplateFormGenerator.FIELD_END_TIME);

      SchedulePeriod period = getOccupiedPeriod(schedule.getPeriods());
      if(period==null)
      {
         if(start==null)
           start = new TimeRuleFactory().create(8, 0, 0);
         if(end==null)
           end = new TimeRuleFactory().create(17, 0, 0);
         period = schedule.makePeriod().from(start.getHour(), start.getMinute()).to(end.getHour(), end.getMinute()).useValue(true);
      }
      else
      {
         if(start==null)
           start = new TimeRuleFactory().create(period.getStartTime().toDate());
         if(end==null)
           end = new TimeRuleFactory().create(period.getEndTime().toDate());
         period = schedule.makePeriod().from(start.getHour(), start.getMinute()).to(end.getHour(), end.getMinute()).useValue(true);
      }
      schedule.clearPeriods();
      schedule.addPeriod(period);
   }

   protected DateRule createDateRule(String fieldName)
   {
      try
      {
         if(hasParameter(fieldName))
         {
            String dateString = getParameter(fieldName);
            if(fieldName!=null)
               return new BacnetDateRuleFormat().parse(dateString);
         }
      }
      catch(ParseException e)
      {
         badValues.add(fieldName);
      }
      return null;
   }

   protected List<DateRule> createDateRuleList(String fieldName)
   {
      List<DateRule> rules = new ArrayList<DateRule>();
      try
      {
         if(hasParameter(fieldName))
         {
            String dateString = getParameter(fieldName);
            if(dateString!=null)
            {
               String[] ruleStrs = dateString.split("[ ,\n\r\t]");
               for(String ruleStr : ruleStrs)
               {
                  if(ruleStr.trim().length()>0)
                     rules.add(new BacnetDateRuleFormat().parse(ruleStr));
               }
            }
         }
      }
      catch(ParseException e)
      {
         badValues.add(fieldName);
      }
      return rules;
   }

   protected TimeRule createTimeRule(String fieldName)
   {
      try
      {
         if(hasParameter(fieldName))
         {
            String timeString = getParameter(fieldName);
            return new BacnetTimeRuleFormat().parse(timeString);
         }
      }
      catch(ParseException e)
      {
         badValues.add(fieldName);
      }
      return null;
   }

   private SchedulePeriod getOccupiedPeriod(SortedSet<SchedulePeriod> periods)
   {
      for(SchedulePeriod period : periods)
      {
         if(period.getRawValue()!=null)
         {
            //Use the first non-null schedule period.
            //This "Simple" scheduler will not handle more than one
            return period;
         }
      }
      return null;
   }

   private Collection<DayOfWeek> updateDaysOfWeek(EnumSet<DayOfWeek> currentDays)
   {
      List<DayOfWeek> updated = new ArrayList<DayOfWeek>();
      if(hasDayOfWeek(DayOfWeek.SUNDAY, currentDays))
         updated.add(DayOfWeek.SUNDAY);

      if(hasDayOfWeek(DayOfWeek.MONDAY, currentDays))
         updated.add(DayOfWeek.MONDAY);

      if(hasDayOfWeek(DayOfWeek.TUESDAY, currentDays))
         updated.add(DayOfWeek.TUESDAY);

      if(hasDayOfWeek(DayOfWeek.WEDNESDAY, currentDays))
         updated.add(DayOfWeek.WEDNESDAY);

      if(hasDayOfWeek(DayOfWeek.THURSDAY, currentDays))
         updated.add(DayOfWeek.THURSDAY);

      if(hasDayOfWeek(DayOfWeek.FRIDAY, currentDays))
         updated.add(DayOfWeek.FRIDAY);

      if(hasDayOfWeek(DayOfWeek.SATURDAY, currentDays))
         updated.add(DayOfWeek.SATURDAY);

      return updated;
   }

   private boolean hasDayOfWeek(DayOfWeek day, EnumSet<DayOfWeek> defaultDays)
   {
      if(hasParameter(day.name()))
         return "on".equals(getParameter(day.name()));

      return defaultDays.contains(day);
   }


   @Override
   public void handleContinuous(@NotNull Continuous continuous, Object... objects)
   {
      try
      {
         String description = getParameter(ScheduleTemplateFormGenerator.FIELD_DESCRIPTION);
         if(isUpdate(description, continuous.getDescription()))
            continuous.setDescription(description);

         DateRule startDate = createDateRule(ScheduleTemplateFormGenerator.FIELD_START_DATE);
         if(isUpdate(startDate, continuous.getStartDate()))
            continuous.setStartDate(startDate.toSimpleDate());

         TimeRule startTime = createTimeRule(ScheduleTemplateFormGenerator.FIELD_START_TIME);
         if(isUpdate(startTime, continuous.getStartTime()))
            continuous.setStartTime(startTime.toSimpleTime());

         DateRule endDate = createDateRule(ScheduleTemplateFormGenerator.FIELD_END_DATE);
         if(isUpdate(endDate, continuous.getEndDate()))
            continuous.setEndDate(endDate.toSimpleDate());

         TimeRule endTime = createTimeRule(ScheduleTemplateFormGenerator.FIELD_END_TIME);
         if(isUpdate(endTime, continuous.getEndTime()))
            continuous.setEndTime(endTime.toSimpleTime());
      }
      catch(WritePrivilegeException e)
      {
         permissionFailure=true;
      }
   }

   @Override
   public void handleDated(@NotNull Dated dated, Object... objects)
   {
      try
      {
         String description = getParameter(ScheduleTemplateFormGenerator.FIELD_DESCRIPTION);
         if(isUpdate(description, dated.getDescription()))
            dated.setDescription(description);

         DateRule dateRule = createDateRule(ScheduleTemplateFormGenerator.FIELD_START_DATE);
         if(isUpdate(dateRule, dated.getDateRule()))
            dated.setDateRule(dateRule);

         setOccupiedPeriod(dated);
      }
      catch(WritePrivilegeException e)
      {
         permissionFailure=true;
      }
   }

   @Override
   public void handleDatedWeekly(@NotNull DatedWeekly datedWeekly, Object... objects)
   {
      try
      {
         String description = getParameter(ScheduleTemplateFormGenerator.FIELD_DESCRIPTION);
         if(isUpdate(description, datedWeekly.getDescription()))
            datedWeekly.setDescription(description);

         DateRule startDate = createDateRule(ScheduleTemplateFormGenerator.FIELD_START_DATE);
         if(isUpdate(startDate, datedWeekly.getStartDate()))
            datedWeekly.setStartDate(startDate.toSimpleDate());

         DateRule endDate = createDateRule(ScheduleTemplateFormGenerator.FIELD_END_DATE);
         if(isUpdate(endDate, datedWeekly.getEndDate()))
            datedWeekly.setEndDate(endDate.toSimpleDate());

         Collection<DayOfWeek> daysOfWeek = updateDaysOfWeek(datedWeekly.getDaysOfWeek());
         datedWeekly.setDaysOfWeek(daysOfWeek);

         setOccupiedPeriod(datedWeekly);
      }
      catch(WritePrivilegeException e)
      {
         permissionFailure=true;
      }
   }

   @Override
   public void handleDatedList(@NotNull DatedList dateList, Object... objects)
   {
      try
      {
         String description = getParameter(ScheduleTemplateFormGenerator.FIELD_DESCRIPTION);
         if(isUpdate(description, dateList.getDescription()))
            dateList.setDescription(description);

         List<DateRule> dateRules = createDateRuleList(ScheduleTemplateFormGenerator.FIELD_START_DATE);
         if(!dateRules.equals(dateList.getDateRuleList()))
         {
            dateList.clearDateRuleList();
            for(DateRule dateRule : dateRules)
            {
               dateList.addDateRule(dateRule);
            }
         }

         setOccupiedPeriod(dateList);
      }
      catch(WritePrivilegeException e)
      {
         permissionFailure=true;
      }
   }

   @Override
   public void handleDatedRange(@NotNull DatedRange dateRange, Object... objects)
   {
      try
      {
         String description = getParameter(ScheduleTemplateFormGenerator.FIELD_DESCRIPTION);
         if(isUpdate(description, dateRange.getDescription()))
            dateRange.setDescription(description);

         DateRule startDate = createDateRule(ScheduleTemplateFormGenerator.FIELD_START_DATE);
         if(isUpdate(startDate, dateRange.getStartDate()))
            dateRange.setStartDate(startDate.toSimpleDate());

         DateRule endDate = createDateRule(ScheduleTemplateFormGenerator.FIELD_END_DATE);
         if(isUpdate(endDate, dateRange.getEndDate()))
            dateRange.setEndDate(endDate.toSimpleDate());

         setOccupiedPeriod(dateRange);
      }
      catch(WritePrivilegeException e)
      {
         permissionFailure=true;
      }
   }

   @Override
   public void handleWeekly(@NotNull Weekly weekly, Object... objects)
   {
      try
      {
         String description = getParameter(ScheduleTemplateFormGenerator.FIELD_DESCRIPTION);
         if(isUpdate(description, weekly.getDescription()))
            weekly.setDescription(description);

         Collection<DayOfWeek> daysOfWeek = updateDaysOfWeek(weekly.getDaysOfWeek());
         weekly.setDaysOfWeek(daysOfWeek);

         setOccupiedPeriod(weekly);
      }
      catch(WritePrivilegeException e)
      {
         permissionFailure=true;
      }
   }

   @Override
   public void handleWildcard(@NotNull Wildcard wildcard, Object... objects)
   {
      try
      {
         String description = getParameter(ScheduleTemplateFormGenerator.FIELD_DESCRIPTION);
         if(isUpdate(description, wildcard.getDescription()))
            wildcard.setDescription(description);

         DateRule dateRule = createDateRule(ScheduleTemplateFormGenerator.FIELD_START_DATE);
         if(isUpdate(dateRule, wildcard.getDateRule()))
            wildcard.setDateRule(dateRule);

         if(dateRule instanceof MutableDateRule)
         {
            MutableDateRule mRule = (MutableDateRule)dateRule;
            wildcard.setWeekNumber(mRule.getWeek());
         }
         setOccupiedPeriod(wildcard);
      }
      catch(WritePrivilegeException e)
      {
         permissionFailure=true;
      }
   }

   public boolean hasPermissionsFailure()
   {
      return permissionFailure;
   }

   public boolean hasInvalidFields()
   {
      return !badValues.isEmpty();
   }

   public List<String> getInvalidFields()
   {
      return new ArrayList(badValues);
   }

   public String getResponseString()
   {
      return "";
   }
}
