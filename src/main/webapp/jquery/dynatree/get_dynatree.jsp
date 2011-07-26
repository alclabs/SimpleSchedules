<%@ page import="org.jetbrains.annotations.NotNull" %>
<%@ page import="com.controlj.green.addonsupport.access.*" %>
<%@ page import="com.controlj.green.addonsupport.InvalidConnectionRequestException" %>
<%@ page import="com.controlj.green.addonsupport.access.util.LocationSort" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONObject" %>

<%@ page import="javax.servlet.ServletException" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="java.io.IOException" %>
<%@ page import="java.io.PrintWriter" %>
<%@ page import="java.util.Collection" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="java.io.StringWriter" %>
<%@ page import="com.controlj.green.addonsupport.access.schedule.Schedule" %>
<%@ page import="com.controlj.green.addonsupport.access.schedule.ScheduleCategory" %>
<%@ page import="com.controlj.green.addonsupport.access.aspect.Schedulable" %>
<%@ page import="com.controlj.green.addonsupport.access.aspect.Group" %>

<%=getTreeJson(request, response)%>
<%!

   public String getTreeJson(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
       resp.setContentType("application/json");

       final String id = req.getParameter("id");
       final boolean stopAtEquipment = "true".equals(req.getParameter("stop_at_equipment"));

       final String typeString = req.getParameter("type");
       final String treeString = req.getParameter("tree");
       final PrintWriter writer = resp.getWriter();

       try {
           SystemConnection connection = DirectAccess.getDirectAccess().getUserSystemConnection(req);

           connection.runReadAction(new ReadAction() {
               @Override
               public void execute(@NotNull SystemAccess access) throws Exception {

                  SystemTree tree = determineBaseTreeType(treeString);
                   JSONArray jsonArray = new JSONArray();
                   boolean root = (id==null);
                   if (root)
                   {
                      Location rootLocation = access.getTree(tree).getRoot();
                      JSONObject json = createRootNode(rootLocation, stopAtEquipment, access);
                      if (typeString.equals("schedule"))
                         addScheduleGroupRoot(json, access);

                      jsonArray.put(json);
                   }
                   else
                   {
                      Location location = null;
                      try
                      {
                         location = access.getTree(tree).resolve(id);
                      }
                      catch(UnresolvableException e)
                      {
                         Location grpTreeRoot = access.getTree(SystemTree.Schedule_Group).getRoot();
                         location = findLocationByWalking(grpTreeRoot, id);
                         if(location==null)
                            throw e;

                      }
                      jsonArray = getChildArray(location, stopAtEquipment, access);
                   }

                   jsonArray.write(writer);

               }

           });
       }
       catch (InvalidConnectionRequestException e)
       {
          writeErrorNode("Invalid Connection. Please refresh the page.", e, writer);
       }
       catch (SystemException e)
       {
          writeErrorNode("System Error", e, writer);
       }
       catch (ActionExecutionException e)
       {
          writeErrorNode("Invalid Action for tree "+treeString, e, writer);
       }
      return "";
   }

   //this method get around a bug with Tree.resolve
   //it does not handle Schedule Group Directories
   private Location findLocationByWalking(Location parent, String key)
   {
      for(Location location : parent.getChildren())
      {
         if(key.equals(location.getTransientLookupString()))
            return location;
         Location found = findLocationByWalking(location, key);
         if(found!=null)
            return found;
      }
      return null;
   }

   private void addScheduleGroupRoot(JSONObject json, SystemAccess access)
         throws JSONException, UnresolvableException
   {
      Location groupRoot = access.getTree(SystemTree.Schedule_Group).getRoot();
      JSONArray children = json.getJSONArray("children");
      children.put(createNode(groupRoot, hasChildren(groupRoot, true), access));
   }

   private SystemTree determineBaseTreeType(String treeString)
   {
      SystemTree tree;
      if (treeString == null || treeString.equals("geo")) {
          tree = SystemTree.Geographic;
      } else if (treeString.equals("net")) {
          tree = SystemTree.Network;
      } else if (treeString.equals("grp")) {
          tree = SystemTree.Schedule_Group;
      } else {
          tree = SystemTree.Geographic;
      }
      return tree;
   }

   private JSONArray getChildArray(Location location, boolean stopAtEquipment, SystemAccess access) throws JSONException
   {
      JSONArray arrayData = new JSONArray();

      Collection<Location> children = location.getChildren(LocationSort.NATURAL);
      for (Location child : children)
      {
          JSONObject next = createNode(child, hasChildren(child, stopAtEquipment), access);
          arrayData.put(next);
      }
      return arrayData;
   }

   private JSONObject createRootNode(Location rootLocation, boolean stopAtEquipment, SystemAccess access) throws JSONException
   {
      JSONObject json = createNode(rootLocation, true, access);
      json.put("expand", true);
      json.put("children", getChildArray(rootLocation, stopAtEquipment, access));
      return json;
   }

   private JSONObject createNode(Location node, boolean hasChildren, SystemAccess access)
         throws JSONException
   {
      JSONObject next = new JSONObject();
      next.put("title",getText(node));
      next.put("key", node.getTransientLookupString());
      next.put("tree", getTreeName(node.getTree()));
      next.put("path", node.getDisplayPath());

      if(hasSchedules(node, access))
         next.put("addClass", "HasSchedules");

      if (hasChildren)
          next.put("isLazy", true);

      next.put("icon", getIcon(node));
      return next;
   }

   private boolean hasChildren(Location location, boolean stopAtEquipment)
   {
      boolean haschildren = !location.getChildren().isEmpty();
      if(stopAtEquipment)
         return haschildren && location.getType()!=LocationType.Equipment;
      return haschildren;
   }

   private String getText(Location node)
   {
      return node.getDisplayName();
   }

   private String getTreeName(Tree tree)
   {
      String treeString = tree.toString().toLowerCase();
      if(treeString.contains("nettree"))
          return "net";
      else if (treeString.contains("grptree"))
          return "grp";
      else
          return "geo";
   }

   private String getIcon(Location node)
   {
      return getIconForType(node.getType());
   }

   private boolean hasSchedules(Location node, SystemAccess access)
   {
      try
      {
         ScheduleCategory occupancy = access.getScheduleManager().getBooleanScheduleCategoryByReferenceName("occupancy");
         Collection<Schedule> schedules = node.getAspect(Schedulable.class).getSchedules(occupancy);
         return schedules.size()>0;
      }
      catch(Exception e)
      {
         return false;
      }
   }

   private String getIconForType(LocationType type) {
       String urlBase = "../../../../_common/lvl5/skin/graphics/type/";
       String image;

       switch (type) {
           case System:
               image = "system.gif";
               break;

           case Area:
               image = "area.gif";
               break;

           case Site:
               image = "site.gif";
               break;

           case Network:
               image = "network.gif";
               break;

           case Device:
               image = "hardware.gif";
               break;

           case Driver:
               image = "dir.gif";
               break;

           case Equipment:
               image = "equipment.gif";
               break;

           case Microblock:
               image = "io_point.gif";
               break;

           case MicroblockComponent:
               image = "io_point.gif";
               break;

           case Group:
               image = "groups.gif";
               break;

           case Directory:
               image = "dir.gif";
               break;

           default:
               image = "unknown.gif";
               break;
       }

       return urlBase + image;
   }

   private void writeErrorNode(String errorMessage, Exception e, PrintWriter writer)
   {
      try
      {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         String stack = sw.toString();

         JSONObject errorNode = new JSONObject();
         errorNode.put("title", errorMessage+": "+e.getMessage());
         errorNode.put("icon", "ltError.gif");
         errorNode.put("tooltip", stack);
         errorNode.put("unselectable", "true");
         errorNode.put("error", "true");

         JSONArray arrayData = new JSONArray();
         arrayData.put(errorNode);
         arrayData.write(writer);
      }
      catch(JSONException e1)
      {
         //error in the error handling, a System.out is appropriate
         e1.printStackTrace();
      }

   }

%>