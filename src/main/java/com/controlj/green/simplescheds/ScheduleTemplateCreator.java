package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.Schedulable;
import com.controlj.green.addonsupport.access.schedule.*;
import com.controlj.green.addonsupport.access.schedule.template.*;
import com.controlj.green.addonsupport.access.value.WritePrivilegeException;
import com.controlj.green.addonsupport.bacnet.data.datetime.*;

import javax.servlet.http.HttpServlet;
import java.util.*;

public class ScheduleTemplateCreator extends HttpServlet
{

   public void deleteSchedule(Schedule schedule, Location currentLocation)
         throws WritePrivilegeException, NoSuchAspectException
   {
      currentLocation.getAspect(Schedulable.class).deleteSchedule(schedule);
   }

   public Schedule addSchedule(WritableSystemAccess access, Location currentLocation, String templateStr, ScheduleCategory occupancy, SchedulePriority priority)
         throws WritePrivilegeException, NoSuchAspectException
   {
      ScheduleTemplate template = null;
      ScheduleTemplateFactory factory = access.getScheduleManager().getScheduleTemplateFactory();
      Date date = new Date();
      SimpleDate today = (SimpleDate)new DateRuleFactory().year(date.getYear()+1900).month(date.getMonth()).day(date.getDay()).create().toSimpleDate();


      TimeRule am = new TimeRuleFactory().hour(8).create();
      TimeRule pm = new TimeRuleFactory().hour(17).create();
      if(ScheduleTemplateFormGenerator.TYPE_CONTINUOUS.equals(templateStr))
      {
         Continuous cTemplate = factory.createContinuous(priority);
         cTemplate.setStartDate(today);
         cTemplate.setStartTime(am.toSimpleTime());
         cTemplate.setEndDate(today);
         cTemplate.setEndTime(pm.toSimpleTime());

         if(priority.getIndex()==1)
            cTemplate.setValue(false);
         else
            cTemplate.setValue(true);

         template = cTemplate;
      }
      else if(ScheduleTemplateFormGenerator.TYPE_DATED.equals(templateStr))
      {
         Dated datedTemplate = factory.createDated(priority);
         datedTemplate.setDateRule(today);
         template = datedTemplate;
      }
      else if(ScheduleTemplateFormGenerator.TYPE_DATEDWEEKLY.equals(templateStr))
      {
         DatedWeekly dwTemplate = factory.createDatedWeekly(priority);
         dwTemplate.setStartDate(today);
         dwTemplate.setEndDate(today);
         dwTemplate.setDaysOfWeek(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
         template = dwTemplate;
      }
      else if(ScheduleTemplateFormGenerator.TYPE_DATEDLIST.equals(templateStr))
      {
         DatedList dlTemplate = factory.createDatedList(priority);
         dlTemplate.addDateRule(today);
         template = dlTemplate;
      }
      else if(ScheduleTemplateFormGenerator.TYPE_DATEDRANGE.equals(templateStr))
      {
         DatedRange drTemplate = factory.createDatedRange(priority);
         drTemplate.setStartDate(today);
         drTemplate.setEndDate(today);
         template = drTemplate;
      }
      else if(ScheduleTemplateFormGenerator.TYPE_WEEKLY.equals(templateStr))
      {
         Weekly wTemplate = factory.createWeekly(priority);
         wTemplate.setDaysOfWeek(EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));
         template = wTemplate;
      }
      else if(ScheduleTemplateFormGenerator.TYPE_WILDCARD.equals(templateStr))
      {
         Wildcard wcTemplate = factory.createWildcard(priority);
         wcTemplate.setDateRule(today);
         template = wcTemplate;
      }

      if(!"continuous".equals(templateStr))
      {
         SchedulePeriod period = template.makePeriod().from(am.getHour(), am.getMinute()).to(pm.getHour(), pm.getMinute()).useValue(true);
         template.addPeriod(period);
      }

      return currentLocation.getAspect(Schedulable.class).addSchedule(template);

   }


}