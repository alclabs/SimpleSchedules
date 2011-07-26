package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.access.schedule.*;
import com.controlj.green.addonsupport.access.schedule.template.*;
import com.controlj.green.addonsupport.bacnet.data.datetime.*;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.*;

public class ScheduleTemplateFormGenerator implements ScheduleTemplateHandler
{
   //names of schedule templates
   public static final String TYPE_CONTINUOUS = "continuous";
   public static final String TYPE_DATED = "dated";
   public static final String TYPE_DATEDWEEKLY = "datedweekly";
   public static final String TYPE_DATEDLIST = "datedlist";
   public static final String TYPE_DATEDRANGE = "datedrange";
   public static final String TYPE_WEEKLY = "weekly";
   public static final String TYPE_WILDCARD = "wildcard";

   //fields used by the schedule form
   public static final String FIELD_DESCRIPTION = "description";
   public static final String FIELD_START_DATE = "when_start_date";
   public static final String FIELD_START_TIME = "when_start_time";
   public static final String FIELD_END_DATE = "when_end_date";
   public static final String FIELD_END_TIME = "when_end_time";
   public static final String FIELD_LOCATION_KEY = "location_key";

   private static final int daysWorthOfMinutes = 60*24;

   StringBuilder form = new StringBuilder();
   String scheduleKey;
   Schedule schedule;

   ScheduleTemplateFormGenerator(String scheduleKey, Schedule schedule)
   {
      this.scheduleKey = scheduleKey;
      this.schedule = schedule;
   }

   protected void beginSchedule()
   {
      String name = schedule.getDescription();
      if(name.length()==0)
         name="Unnamed Schedule";
      append("  <tr class=\"schedule_form\" >\n");
      beginCell("sched_name");
      append("    <input class='sched_name_field' type='text' name='"+createFieldName(FIELD_DESCRIPTION)+"' value='").append(name).append("'/>\n");
      endCell();
   }

   protected void beginCell(String style)
   {
      append("  <td class=\"sched_cell "+style+"\">\n");
   }

   protected void addBlank()
   {
      append("&nbsp;\n");
   }

   protected void endCell()
   {
      append("  </td>\n");
   }

   protected void endSchedule()
   {
      beginCell("sched_percent");
      append(getPercentOfYear()+"% of year");
      endCell();
      beginCell("sched_percent");
      append("    <input type='button' class='sched_submit delete_button' value='Delete' />\n");
      append("    <input type='hidden' name='"+createFieldName("delete")+"' value='false' />\n");
      endCell();
      append("</tr>\n");
   }

   protected void createScheduleBar()
   {
      int last = 0;
      SortedSet<SchedulePeriod> periods = schedule.getTemplate().getPeriods();
      append("<table class=\"schedule_bar_container\" cellspacing='0' cellpadding='0'><tr>");
      if(schedule.getTemplate() instanceof Continuous)
      {
         boolean continuousValue = Boolean.TRUE.equals(((Continuous)schedule.getTemplate()).getRawValue());
         createScheduleBand(daysWorthOfMinutes, continuousValue ? "occupied":"unoccupied");
      }
      else
      {
            for(SchedulePeriod period : periods)
            {
               int start = getTimeInMinutes(period.getStartTime().toDate());
               int end = getTimeInMinutes(period.getEndTime().toDate());
               //handle undefined periods
               if(last<start)
                  createScheduleBand(start-last, "undefined");

               //special, 0:00 is translated as 24 hr, but ONLY for end times
               //this is because a full day schedule is considered as being from (O:00 --> 0:00)
               //by the Schedule API but we want to use the more clear (0:00 --> 24:00) time
               if(end==0)
                 end=daysWorthOfMinutes;

               //handle occupied/unoccupied periods
               createScheduleBand(end-start, Boolean.TRUE.equals(period.getValue().getValue())?"occupied":"unoccupied");
               last=end;
            }
      }

      //handle the last unoccupied period, if any
      if(last<daysWorthOfMinutes)
         createScheduleBand(daysWorthOfMinutes-last, "undefined");

      append("</tr></table>\n");
   }

   private void createScheduleBand(int minutes, String style)
   {
      int percent = getPercentOfDay(minutes);
      if(percent>0)
         append("<td class=\"schedule_bar ").append(style)
             .append("\" style=\"width: ").append(percent).append("%;\">&nbsp;</td>");
   }

   private int getPercentOfDay(int minutes)
   {
      return (int)((minutes*100)/daysWorthOfMinutes);
   }

   private int getTimeInMinutes(java.util.Date date)
   {
      return (date.getHours()*60)+date.getMinutes();
   }

   protected void addDateRules(DateRule startDateRule, DateRule endDateRule)
   {
      addDateRules(startDateRule, null, endDateRule, null);
   }

   protected void addDateRules(List<DateRule> rules)
   {
      append("<TEXTAREA class='sched_list_field' name=\""+createFieldName(FIELD_START_DATE)+"\">");
      for(DateRule rule : rules)
      {
         append(toDateString(rule)+"\n");
      }
      append("</TEXTAREA>");
   }

   protected void addWildCardRule(DateRule wildRule, WeekRule weekRule)
   {
      append("<input class='sched_wild_field' type='text' name='"+createFieldName(FIELD_START_DATE)+"' value='"+toDateString(wildRule, weekRule)+"'/>");
   }

   protected void addDateRules(DateRule startDateRule, SimpleTime startTime, DateRule endDateRule, SimpleTime endTime)
   {
      append("    <nobr>").append(createDateField(FIELD_START_DATE, startDateRule)).append("\n");
      if(startTime!=null)
         append("    ").append(createTimeField(FIELD_START_TIME, startTime)).append("\n");

      if(endTime!=null)
      {
         append("    to </nobr><nobr>").append(createTimeField(FIELD_END_TIME, endTime)).append("\n");
      }

      if(endDateRule!=null)
      {
         if(endTime==null)
         {
            append("    to </nobr><nobr>");
         }
         else
         {
            append("    ");
         }
         append(createDateField(FIELD_END_DATE, endDateRule)).append("\n");
      }
      append("  </nobr>\n");
   }

   protected void addDaysOfWeek(EnumSet<DayOfWeek> daysOfWeek)
   {
      append("<table><tr>");
      append("    <td style=\"text-align:center; padding-left:2px;\">Mon</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">Tue</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">Wed</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">Thu</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">Fri</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">Sat</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">Sun</td>\n");
      append("</tr><tr>");
      append("    <td style=\"text-align:center; padding-left:2px;\">").append(createCheckboxField(DayOfWeek.MONDAY, daysOfWeek.contains(DayOfWeek.MONDAY))).append("</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">").append(createCheckboxField(DayOfWeek.TUESDAY, daysOfWeek.contains(DayOfWeek.TUESDAY))).append("</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">").append(createCheckboxField(DayOfWeek.WEDNESDAY, daysOfWeek.contains(DayOfWeek.WEDNESDAY))).append("</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">").append(createCheckboxField(DayOfWeek.THURSDAY, daysOfWeek.contains(DayOfWeek.THURSDAY))).append("</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">").append(createCheckboxField(DayOfWeek.FRIDAY, daysOfWeek.contains(DayOfWeek.FRIDAY))).append("</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">").append(createCheckboxField(DayOfWeek.SATURDAY, daysOfWeek.contains(DayOfWeek.SATURDAY))).append("</td>\n");
      append("    <td style=\"text-align:center; padding-left:2px;\">").append(createCheckboxField(DayOfWeek.SUNDAY, daysOfWeek.contains(DayOfWeek.SUNDAY))).append("</td>\n");
      append("</tr></table>");
   }

   private String createCheckboxField(DayOfWeek day, boolean checked)
   {
      return "<input class='check_field' type='checkbox' name='"+createFieldName(day.name())+"' "+(checked?"checked":"")+"/>";
   }

   protected void addPeriodFormElements(ScheduleTemplate schedule)
   {
      SinglePeriodScheduler scheduler = new SinglePeriodScheduler(schedule);
      SchedulePeriod period = scheduler.getSinglePeriod();

      SimpleTime start = null;
      SimpleTime end = null;
      if(period==null)
      {
         start = new SimpleTimeFactory().create(0, 0, 0, 0);
         end = new SimpleTimeFactory().create(0, 0, 0 ,0);
      }
      else
      {
         start = period.getStartTime();
         end = period.getEndTime();
         int minutes = getTimeInMinutes(end.toDate());

         //special, 0:00 is translated as 24 hr, but ONLY for end times
         //this is because a full day schedule is considered as being from (O:00 --> 0:00)
         //by the Schedule API but we want to use the more clear (0:00 --> 24:00) time
         if(minutes==0)
            end = new MutableTimeRule().setHour(24);
      }
      append("  <nobr>").append(createTimeField(FIELD_START_TIME, start));
      append("\n  to ").append(createTimeField(FIELD_END_TIME, end)).append("</nobr>\n");
      if(scheduler.isMultiplePeriods())
      {
         append("<br/><span class=\"sched_warning\">Saving will clear the other occupied periods.</span>");
      }
   }

   private String createTimeField(String name, SimpleTime time)
   {
      return "<input class='sched_time_field' type='text' name='"+createFieldName(name)+"' value='"+toTimeString(time)+"'/>";
   }

   private String createDateField(String name, DateRule dateRule)
   {
      return "<input class='sched_date_field' type='text' name='"+createFieldName(name)+"' value='"+toDateString(dateRule)+"'/>";
   }

   private String createFieldName(String baseName)
   {
      return scheduleKey+"."+baseName;
   }

   @Override
   public void handleContinuous(@NotNull Continuous continuous, Object... objects)
   {
      beginSchedule();
      beginCell("sched_affects");
      addBlank();
      endCell();
      beginCell("sched_period");
      addDateRules(continuous.getStartDate(), continuous.getStartTime(),
                   continuous.getEndDate(), continuous.getEndTime());
      createScheduleBar();
      endCell();
      endSchedule();
   }

   @Override
   public void handleDated(@NotNull Dated dated, Object... objects)
   {
      beginSchedule();
      beginCell("sched_affects");
      addDateRules(dated.getDateRule(), null, null, null);
      endCell();
      beginCell("sched_period");
      addPeriodFormElements(dated);
      createScheduleBar();
      endCell();
      endSchedule();
   }

   @Override
   public void handleDatedWeekly(@NotNull DatedWeekly datedWeekly, Object... objects)
   {
      beginSchedule();
      beginCell("sched_affects");
      addDateRules(datedWeekly.getStartDate(), datedWeekly.getStartDate());
      addDaysOfWeek(datedWeekly.getDaysOfWeek());
      endCell();
      beginCell("sched_period");
      addPeriodFormElements(datedWeekly);
      createScheduleBar();
      endCell();
      endSchedule();
   }

   @Override
   public void handleDatedList(@NotNull DatedList datedList, Object... objects)
   {
      beginSchedule();
      beginCell("sched_affects");
      addDateRules(datedList.getDateRuleList());
      endCell();
      beginCell("sched_period");
      addPeriodFormElements(datedList);
      createScheduleBar();
      endCell();
      endSchedule();
   }

   @Override
   public void handleDatedRange(@NotNull DatedRange dateRange, Object... objects)
   {
      beginSchedule();
      beginCell("sched_affects");
      addDateRules(dateRange.getStartDate(), dateRange.getStartDate());
      endCell();
      beginCell("sched_period");
      addPeriodFormElements(dateRange);
      createScheduleBar();
      endCell();
      endSchedule();
   }

   @Override
   public void handleWeekly(@NotNull Weekly weekly, Object... objects)
   {
      beginSchedule();
      beginCell("sched_affects");
      addDaysOfWeek(weekly.getDaysOfWeek());
      endCell();
      beginCell("sched_period");
      addPeriodFormElements(weekly);
      createScheduleBar();
      endCell();
      endSchedule();
   }

   @Override
   public void handleWildcard(@NotNull Wildcard wildcard, Object... objects)
   {
      beginSchedule();
      beginCell("sched_affects");
      addWildCardRule(wildcard.getDateRule(), wildcard.getWeekRule());
      endCell();
      beginCell("sched_period");
      addPeriodFormElements(wildcard);
      createScheduleBar();
      endCell();
      endSchedule();
   }

   private String toDateString(DateRule dateRule)
   {
      return new BacnetDateRuleFormat().format(dateRule);
   }

   private String toDateString(DateRule dateRule, WeekRule weekRule)
   {
      return new BacnetDateRuleFormat().format(dateRule, 1, weekRule);
   }

   private String toTimeString(SimpleTime dateTime)
   {
      return new BacnetTimeRuleFormat().format(dateTime);
   }

   private String getPercentOfYear()
   {
      java.util.Date now = new java.util.Date();
      java.util.Date nextYear = new java.util.Date(now.getYear()+1, now.getMonth(), now.getDate(),
                                                   now.getHours(), now.getMinutes(), now.getSeconds());
      Iterator<ScheduleViewPeriod> future = schedule.getView(now).getFuturePeriods();

      long last = 0;
      long totalOccupiedTime = 0;
      ScheduleViewPeriod period = future.next();
      int count= 0;
      while(period!=null && last<nextYear.getTime())
      {
         long start = period.getStartDateTimeInMillis();
         if(start>nextYear.getTime())
            break;

         long end = period.getEndDateTimeInMillis();
         if(end>nextYear.getTime())
            end=nextYear.getTime();

         if(Boolean.TRUE.equals(period.getRawValue()))
         {
            //handle occupied period
            totalOccupiedTime += end-start;

         }

         //avoid an endless loop
         if(last==end && count>365)
           break;

         count++;
         last=end;
         period = future.next();
      }
      double timeInRange = nextYear.getTime()-now.getTime();
      double percentage = (totalOccupiedTime*100)/timeInRange;

      return new DecimalFormat("0.00").format(percentage);
   }

   private ScheduleTemplateFormGenerator append(String str)
   {
      form.append(str);
      return this;
   }

   private ScheduleTemplateFormGenerator append(int number)
   {
      form.append(number);
      return this;
   }

   public String getFormString()
   {
      return form.toString();
   }

}
