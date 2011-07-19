<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
     <title>Simple Schedules</title>

     <link type="text/css" href="jquery/css/start/jquery-ui-1.8.9.custom.css" rel="stylesheet" />
     <script type="text/javascript" src="jquery/jquery-1.4.4.min.js"></script>
     <script type="text/javascript" src="jquery/jquery-ui-1.8.9.custom.min.js"></script>
     <script type="text/javascript" src="jquery/jquery.cookie.js"></script>

     <!-- dyntree from http://code.google.com/p/dynatree/ -->
     <link type="text/css" href="jquery/dynatree/skin/ui.dynatree.css"  rel="stylesheet">
     <script type="text/javascript" src="jquery/dynatree/jquery.dynatree.min.js"></script>

     <link type="text/css" href="css/content.css"  rel="stylesheet">
  </head>

  <body class="content-document">
   <div style="position:relative;">
   <div class="content-container" style="margin-top:5px; position:relative; float:left; margin-top:5px;">
     <div id="treetitle"  class="content-header">Select Location</div>
     <div id="geotree" class="content-body">&nbsp;</div>
   </div>
   <div id="scheduleforms" class="content-container" style="margin-top:5px; margin-left:5px; position:relative; float:left;">&nbsp;</div>

   <!-- div id="debug" class="content-container" style="margin-top:5px; display:block; margin-top:5px; margin-left:5px; position:relative; float:left;">
      <div class="content-header">DEBUG AREA</div>
      <button class="mybutton" onclick="$('#debug_screen').text($('#geotree').html())">Tree Source</button>
      <button class="mybutton" onclick="$('#debug_screen').text($('#scheduleforms').html())">Form Source</button>
      <div id="debug_screen" class="content-body" >&nbsp;</div>
  </div-->
   </div>

  <script type='text/javascript'>

     var treeLocation =null;
     function handlePostResponse(data)
     {
        //$("#scheduleforms .changed").removeClass("changed");
        $('#scheduleforms').load("schedules?location_key="+treeLocation, bindForm);
     }

     function handleAddSchedule(template, priority)
     {
        $.post("schedules", {"location_key":treeLocation , "add_schedule":"true", "template":template, "priority":priority}, handlePostResponse);
     }

     function bindTemplateButton(priorityName, priorityValue)
     {
        var menu=$( "#"+priorityName+"_schedule_templates" );
        menu.dialog({ autoOpen: false,
           buttons: { "Ok":
                 function()
                 {
                    $(this).dialog("close");
                    var template = menu.find("input:checked").attr("value");
                    handleAddSchedule(template, priorityValue);
                 },
                 "Cancel":
                 function()
                 {
                    $(this).dialog("close");
                 }
           }
         });
         $("#add_"+priorityName+"_button").bind("click", function(e)
         {
            menu.dialog('open');
         });

     }

     function bindDeleteButton()
     {
        $(".delete_button").bind("click", function(e)
        {
           var disable = false;
           //the button that deletes/restores a achedule
           var del = $(e.target);
           //the field that indicates this schedule should be deleted
           var delField = del.next();
           if(del.attr("value")=="Delete")
           {
              disable=true;
              del.attr("value", "Restore");
              delField.attr("value", "true");
              delField.addClass("changed");
           }
           else
           {
              disable=false;
              del.attr("value", "Delete");
              delField.attr("value", "false");
              delField.removeClass("changed");
           }

           var inputs = del.parent().parent().find("input");
           inputs.attr("disabled", disable);

           //the delete button and field are never disabled
           del.attr("disabled", false);
           delField.attr("disabled", false);
        });
     }
     function commitChanges(download)
     {
        var submitElements = $("#scheduleforms .changed, #location_key");

        var serializedFields =  submitElements.serializeArray();

        //unchecked checkboxes are not serialized by default, we have to do that ourselves
        $("#scheduleforms .changed:input[type=checkbox]:not(:checked)").each(
          function()
          {
             serializedFields.push({ name: $(this).attr('name'), value: "off" });
          });
        serializedFields.push({ name:"download", value:download});

        $.post("schedules", serializedFields, handlePostResponse);
     }

     function bindSaveCancelButtons()
     {
         $("#sched_save_button").bind("click", function(e)
         {
            commitChanges(false);
         });

         $("#sched_save_down_button").bind("click", function(e)
         {
            commitChanges(true);
         });

         $("#sched_cancel_button").bind("click", function(e)
         {
            $('#scheduleforms').load("schedules?location_key="+treeLocation, bindForm);
         });
     }

     function bindFormElements()
     {
        $("#scheduleforms input, #scheduleforms select, #scheduleforms textarea").change(function() {

             //'this' in the current context is the element that changed
             $(this).addClass("changed");
        });

        $( ".sched_date_field" ).datepicker({ dateFormat: 'yy/mm/dd' });
        $("#scheduleforms button, input:submit, input:button").button();
     }

     function bindForm()
     {
        bindFormElements();
        bindTemplateButton("occupied", "0");
        bindTemplateButton("holiday", "1");
        bindTemplateButton("override", "2");
        bindDeleteButton();
        bindSaveCancelButtons();
     }

     $(function(){
         // Attach the dynatree widget to an existing <div id="tree"> element
         // and pass the tree options as an argument to the dynatree() function:
         $("#geotree").dynatree({
             title: "System",
             selectMode:1,
             autoFocus:false,
             autoCollapse: true,
             fx: { height: "toggle", duration: 200 },
            
             initAjax: {
                 url: "jquery/dynatree/get_dynatree.jsp",
                 data: { type:'schedule',
                         stop_at_equipment:'true'
                       }
             },

             onLazyRead: function(dtnode) {
                 dtnode.appendAjax({
                    url: "jquery/dynatree/get_dynatree.jsp",
                     data: {
                         id:dtnode.data.key,
                         type: 'schedule',
                         stop_at_equipment:'true'
                     }
                 })
             },
             onQueryActivate: function(node)
             {
                if($("#scheduleforms .changed").length>0)
                {
                   alert("You have unsaved changes. Save or Cancel before viewing another location.");
                   return false;
                }
                return true;
             },

             onActivate: function(node)
             {
                treeLocation = node.data.key;
                //$('#selection').text("Schedules at: "+node.data.path);
                $('#scheduleforms').load("schedules?location_key="+node.data.key, bindForm);
             },

             cache: false
         });
        
         $("button, input:button, input:submit").button();
     });

  </script>

  </body>
</html>