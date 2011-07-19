package com.controlj.green.simplescheds;

import com.controlj.green.addonsupport.InvalidConnectionRequestException;
import com.controlj.green.addonsupport.access.*;
import com.controlj.green.addonsupport.access.aspect.Schedulable;
import com.controlj.green.addonsupport.access.schedule.*;
import com.controlj.green.addonsupport.access.value.WritePrivilegeException;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

public class ScheduleServlet extends HttpServlet
{
   @Override
   protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
   {
      resp.setHeader("Expires", "Wed, 01 Jan 2003 12:00:00 GMT");
      resp.setHeader("Cache-Control", "no-cache");
      try
      {
         SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
         WriteAction writeSchedules = new WriteAction()
         {

            @Override
            public void execute(@NotNull final WritableSystemAccess access) throws Exception
            {
               String locationKey = req.getParameter("location_key");
               Location currentLocation = access.getTree(SystemTree.Geographic).resolve(locationKey);
               ServletOutputStream out = resp.getOutputStream();

               boolean add = "true".equals(req.getParameter("add_schedule"));
               String response = "";
               if(add)
               {

                  String templateStr = req.getParameter("template");
                  String priorityStr = req.getParameter("priority");
                  response = addSchedule(access, currentLocation, templateStr, priorityStr);
                  out.println(response);
               }
               else
               {
                  Map<String, Schedule> scheduleMap = (Map<String, Schedule>)req.getSession(false).getAttribute("schedule_map");
                  if(scheduleMap==null)
                    return;

                  for(String key : scheduleMap.keySet())
                  {
                     Map<String, String> changes = getChangedFieldsForSchedule(key, req);
                     if(changes.size()>0)
                     {
                        Schedule schedule = scheduleMap.get(key);
                        if("true".equals(changes.get("delete")))
                           response = deleteSchedule(schedule, currentLocation);
                        else
                           response = modifySchedule(changes, schedule);
                     }
                  }
               }
               out.println(response);
               String message = "Message: done posting";
               out.println(message);
            }
         };

         if("true".equals(req.getParameter("download")))
            connection.runWriteAction(FieldAccessFactory.newFieldAccess(), "SimpleSchedules changed schedules", writeSchedules);
         else
            connection.runWriteAction("SimpleSchedules changed schedules", writeSchedules);
      }
      catch (WriteAbortedException e)
      {
         ServletOutputStream out = resp.getOutputStream();
         out.println("<div class=\"big_error\">Failed to write changes to server. "+e.getMessage()+"</div>");
      }
      catch (InvalidConnectionRequestException e)
      {
         ServletOutputStream out = resp.getOutputStream();
         out.println("<div class=\"big_error\">Invalid Connection. Please refresh the page.</div>");
      }
      catch (SystemException e)
      {
         ServletOutputStream out = resp.getOutputStream();
         out.println("<div class=\"big_error\">An error occured:"+e.getMessage()+"</div>");
      }
      catch (ActionExecutionException e)
      {
         ServletOutputStream out = resp.getOutputStream();
         out.println("<div class=\"big_error\">Failed to execute action:"+e.getMessage()+"</div>");
      }
      catch(Throwable t)
      {
         resp.getOutputStream().println(t.getMessage());
         t.printStackTrace();
      }
   }

   private String deleteSchedule(Schedule schedule, Location currentLocation)
         throws WritePrivilegeException, NoSuchAspectException
   {
      new ScheduleTemplateCreator().deleteSchedule(schedule, currentLocation);
      return "deleted schedule "+schedule.getDescription();
   }

   private String addSchedule(WritableSystemAccess access, Location currentLocation, String templateStr, String priorityStr)
         throws Exception
   {

      ScheduleCategory occupancy = getOccupancyCategory(access);

      int priorityIndex = Integer.parseInt(priorityStr);

      SchedulePriority priority = occupancy.getPriorityByIndex(priorityIndex);

      Schedule schedule = new ScheduleTemplateCreator().addSchedule(access, currentLocation, templateStr, occupancy, priority);

      return "added new "+templateStr+" schedule";
   }

   private String modifySchedule(Map<String, String> changes, Schedule schedule)
   {
      ScheduleTemplateFormReceiver formReceiver = new ScheduleTemplateFormReceiver(changes);
      try
      {
         schedule.getTemplate().dispatch(formReceiver);
      }
      catch(AbstractMethodError abstractMethodError)
      {
         abstractMethodError.printStackTrace();
      }
      return formReceiver.getResponseString();
   }

   private Map<String, String> getChangedFieldsForSchedule(String scheduleKey, HttpServletRequest req)
   {
      Map<String, String[]> parameters = req.getParameterMap();
      Map<String, String> myParameters = new HashMap();
      for(String key : parameters.keySet())
      {
         if(key.startsWith(scheduleKey))
         {
            String myKey = key.substring(scheduleKey.length()+1);
            myParameters.put(myKey, parameters.get(key)[0]);
         }
      }
      return myParameters;
   }

   @Override
   protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException
   {
      resp.setHeader("Expires", "Wed, 01 Jan 2003 12:00:00 GMT");
      resp.setHeader("Cache-Control", "no-cache");
      try
      {
         SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);
         ReadAction readSchedules = new ReadAction()
         {
            @Override
            public void execute(@NotNull SystemAccess access) throws Exception
            {
                  ServletOutputStream out = resp.getOutputStream();
                  String locationKey = req.getParameter("location_key");
                  Location currentLocation = access.getTree(SystemTree.Geographic).resolve(locationKey);

                  ScheduleCategory occupancy = getOccupancyCategory(access);
                  try
                  {
                     Map<String, Schedule> scheduleMap = generateScheduleMap(currentLocation, occupancy);
                     req.getSession(false).setAttribute("schedule_map", scheduleMap);
                     new ScheduleListHtmlGenerator().generateHtml(currentLocation, access.getScheduleManager(), scheduleMap, out);
                  }
                  catch(NoSuchAspectException e)
                  {
                     //no schedules here, just use an empty table
                  }
            }
         };

         connection.runReadAction(readSchedules);
      }
      catch (InvalidConnectionRequestException e)
      {
         ServletOutputStream out = resp.getOutputStream();
         out.println("<div class=\"big_error\">Invalid Connection. Please refresh the page.</div>");
      }
      catch (SystemException e)
      {
         ServletOutputStream out = resp.getOutputStream();
         out.println("<div class=\"big_error\">An error occured:"+e.getMessage()+"</div>");
      }
      catch (ActionExecutionException e)
      {
         ServletOutputStream out = resp.getOutputStream();
         out.println("<div class=\"big_error\">Failed to execute action:"+e.getMessage()+"</div>");
      }
      catch(Throwable t)
      {
         t.printStackTrace();
      }

   }

   private ScheduleCategory getOccupancyCategory(SystemAccess access) throws Exception
   {
      return access.getScheduleManager().getBooleanScheduleCategoryByReferenceName("occupancy");
   }

   private Map<String, Schedule> generateScheduleMap(Location currentLocation, ScheduleCategory occupancy)
         throws NoSuchAspectException
   {
      Collection<Schedule> schedules = currentLocation.getAspect(Schedulable.class).getSchedules(occupancy);

      Map<String, Schedule> scheduleMap = new LinkedHashMap<String, Schedule>();

      int counter = 0;
      for(Schedule schedule : schedules)
      {
         String id = "sched_"+counter;
         scheduleMap.put(id, schedule);
         counter++;
      }
      return scheduleMap;
   }

}
