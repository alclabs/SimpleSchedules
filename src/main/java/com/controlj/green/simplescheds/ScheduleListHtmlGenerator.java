package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.schedule.Schedule;
import com.controlj.green.addonsupport.access.schedule.ScheduleManager;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.util.Map;

public class ScheduleListHtmlGenerator
{
   protected void generateHtml(Location location, ScheduleManager manager, Map<String, Schedule> scheduleMap, ServletOutputStream out) throws IOException
   {
      createHeader(out);

      if(hasSchedulesWithPriority(0, scheduleMap))
      {
         createPriorityHeader("occupied", "Normal", out);
         createPriorityScheduleList(0, scheduleMap, out);
      }

      if(hasSchedulesWithPriority(1, scheduleMap))
      {
         createPriorityHeader("holiday", "Holiday", out);
         createPriorityScheduleList(1, scheduleMap, out);
      }

      if(hasSchedulesWithPriority(2, scheduleMap))
      {
         createPriorityHeader("override", "Override", out);
         createPriorityScheduleList(2, scheduleMap, out);
      }
      createFooter(location, out);
   }

   private boolean hasSchedulesWithPriority(int priority, Map<String, Schedule> scheduleMap)
   {
      for(String scheduleKey : scheduleMap.keySet())
      {
         Schedule sched = scheduleMap.get(scheduleKey);
         if(sched.getPriority().getIndex()==priority)
            return true;
      }
      return false;
   }

   private void createHeader(ServletOutputStream out)
         throws IOException
   {
      out.println("<table class=\"sched_list\">");
   }

   private void createPriorityHeader(String name, String title, ServletOutputStream out)
         throws IOException
   {
      out.println("<tr>");
      out.println("<th class=\"sched_title\" colspan=\"5\">"+title+"</th>");
      out.println("</tr>");
      out.println("<tr>");
      out.println("<th class=\"sched_head\">Description</th>");
      out.println("<th class=\"sched_head\">Days Affected</th>");
      out.println("<th class=\"sched_head\" colspan=\"3\">Occupied Period</th>");
      out.println("</tr>");
   }

   private void createPriorityScheduleList(int priority, Map<String, Schedule> scheduleMap, ServletOutputStream out)
         throws IOException
   {
      for(String scheduleKey : scheduleMap.keySet())
      {
         Schedule sched = scheduleMap.get(scheduleKey);
         if(sched.getPriority().getIndex()==priority)
            out.println(createScheduleForm(scheduleKey, sched));
      }
   }

   private void createFooter(Location location, ServletOutputStream out)
         throws IOException
   {
      out.println("<tr>\n");
      out.println("</table>\n");
      out.println("<div class=\"sched_add_section\">\n");
      createTemplateDialog("occupied", "Add Normal", out);
      createTemplateDialog("holiday", "Add Holiday", out);
      createTemplateDialog("override", "Add Override", out);
      out.println("</div>\n");
      out.println("<div class=\"sched_save_cancel_section\">\n");
      out.println("    <input type='hidden' id='"+ScheduleTemplateFormGenerator.FIELD_LOCATION_KEY+
                  "' name='"+ScheduleTemplateFormGenerator.FIELD_LOCATION_KEY+
                  "' value='"+location.getTransientLookupString()+"'/>\n");
      out.println("    <input type='button' class='sched_submit' id='sched_save_button'  value='Save' />\n");
      out.println("    <input type='button' class='sched_submit' id='sched_save_down_button'  value='Save and Download' />\n");
      out.println("    <input type='button' class='sched_submit' id='sched_cancel_button' value='Cancel' />\n");
      out.println("</div>\n");
   }

   private void createTemplateDialog(String priority, String label, ServletOutputStream out)
         throws IOException
   {
      out.println("    <input type='button' class='sched_submit' id='add_"+priority+"_button' value='"+label+"' />\n");
      out.println("    <div id='"+priority+"_schedule_templates' title='Select a Type'>");
      if(priority.equals("occupied"))
         out.println("      <input type='radio' class='sched_select_template' name='"+priority+"_templates' value='"+ScheduleTemplateFormGenerator.TYPE_WEEKLY+"' checked/>Weekly<br/>");

      out.println("      <input type='radio' class='sched_select_template' name='"+priority+"_templates' value='"+ScheduleTemplateFormGenerator.TYPE_DATED+"' />Date<br/>");
      out.println("      <input type='radio' class='sched_select_template' name='"+priority+"_templates' value='"+ScheduleTemplateFormGenerator.TYPE_DATEDRANGE+"'/> Date Range<br/>");
      out.println("      <input type='radio' class='sched_select_template' name='"+priority+"_templates' value='"+ScheduleTemplateFormGenerator.TYPE_DATEDLIST+"'/> Date List<br/>");
      out.println("      <input type='radio' class='sched_select_template' name='"+priority+"_templates' value='"+ScheduleTemplateFormGenerator.TYPE_WILDCARD+"'/> Wildcard<br/>");
      out.println("      <input type='radio' class='sched_select_template' name='"+priority+"_templates' value='"+ScheduleTemplateFormGenerator.TYPE_CONTINUOUS+"'/> Continuous<br/>");

      if(priority.equals("occupied"))
         out.println("      <input type='radio' class='sched_select_template' name='"+priority+"_templates' value='"+ScheduleTemplateFormGenerator.TYPE_DATEDWEEKLY+"'/> Dated Weekly<br/>");
      out.println("    </div>");
   }

   private String createScheduleForm(String scheduleKey, Schedule schedule)
   {
      ScheduleTemplateFormGenerator formGenerator = new ScheduleTemplateFormGenerator(scheduleKey, schedule);
      schedule.getTemplate().dispatch(formGenerator);
      return formGenerator.getFormString();
   }
}