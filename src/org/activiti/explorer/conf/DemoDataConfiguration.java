package org.activiti.explorer.conf;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import javax.annotation.PostConstruct;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.GroupQuery;
import org.activiti.engine.identity.Picture;
import org.activiti.engine.identity.User;
import org.activiti.engine.identity.UserQuery;
import org.activiti.engine.impl.asyncexecutor.AsyncExecutor;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.jobexecutor.JobExecutor;
import org.activiti.engine.impl.util.IoUtil;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.Model;
import org.activiti.engine.repository.ModelQuery;
import org.activiti.engine.runtime.Clock;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class DemoDataConfiguration
{
  protected static final Logger LOGGER = LoggerFactory.getLogger(DemoDataConfiguration.class);
  @Autowired
  protected IdentityService identityService;
  @Autowired
  protected RepositoryService repositoryService;
  @Autowired
  protected RuntimeService runtimeService;
  @Autowired
  protected TaskService taskService;
  @Autowired
  protected ManagementService managementService;
  @Autowired
  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  @Autowired
  protected Environment environment;
  
  @PostConstruct
  public void init()
  {
    if (Boolean.valueOf(this.environment.getProperty("create.demo.users", "true")).booleanValue())
    {
      LOGGER.info("Initializing demo groups");
      //initDemoGroups();
      LOGGER.info("Initializing demo users");
      //initDemoUsers();
    }
    if (Boolean.valueOf(this.environment.getProperty("create.demo.definitions", "true")).booleanValue())
    {
      LOGGER.info("Initializing demo process definitions");
      //initProcessDefinitions();
    }
    if (Boolean.valueOf(this.environment.getProperty("create.demo.models", "true")).booleanValue())
    {
      LOGGER.info("Initializing demo models");
      //initModelData();
    }
    if (Boolean.valueOf(this.environment.getProperty("create.demo.reports", "true")).booleanValue())
    {
      LOGGER.info("Initializing demo report data");
      //generateReportData();
    }
  }
  
  protected void initDemoGroups()
  {
    String[] assignmentGroups = { "management", "sales", "marketing", "engineering" };
    for (String groupId : assignmentGroups) {
      createGroup(groupId, "assignment");
    }
    String[] securityGroups = { "user", "admin" };
    for (String groupId : securityGroups) {
      createGroup(groupId, "security-role");
    }
  }
  
  protected void createGroup(String groupId, String type)
  {
    if (this.identityService.createGroupQuery().groupId(groupId).count() == 0L)
    {
      Group newGroup = this.identityService.newGroup(groupId);
      newGroup.setName(groupId.substring(0, 1).toUpperCase() + groupId.substring(1));
      newGroup.setType(type);
      this.identityService.saveGroup(newGroup);
    }
  }
  
  protected void initDemoUsers()
  {
    createUser("kermit", "Kermit", "The Frog", "kermit", "kermit@activiti.org", "org/activiti/explorer/images/kermit.jpg", Arrays.asList(new String[] { "management", "sales", "marketing", "engineering", "user", "admin" }), Arrays.asList(new String[] { "birthDate", "10-10-1955", "jobTitle", "Muppet", "location", "Hollywoord", "phone", "+123456789", "twitterName", "alfresco", "skype", "activiti_kermit_frog" }));
    




    createUser("gonzo", "Gonzo", "The Great", "gonzo", "gonzo@activiti.org", "org/activiti/explorer/images/gonzo.jpg", Arrays.asList(new String[] { "management", "sales", "marketing", "user" }), null);
    


    createUser("fozzie", "Fozzie", "Bear", "fozzie", "fozzie@activiti.org", "org/activiti/explorer/images/fozzie.jpg", Arrays.asList(new String[] { "marketing", "engineering", "user" }), null);
  }
  
  protected void createUser(String userId, String firstName, String lastName, String password, String email, String imageResource, List<String> groups, List<String> userInfo)
  {
    if (this.identityService.createUserQuery().userId(userId).count() == 0L)
    {
      User user = this.identityService.newUser(userId);
      user.setFirstName(firstName);
      user.setLastName(lastName);
      user.setPassword(password);
      user.setEmail(email);
      this.identityService.saveUser(user);
      if (groups != null) {
        for (String group : groups) {
          this.identityService.createMembership(userId, group);
        }
      }
    }
    if (imageResource != null)
    {
      byte[] pictureBytes = IoUtil.readInputStream(getClass().getClassLoader().getResourceAsStream(imageResource), null);
      Picture picture = new Picture(pictureBytes, "image/jpeg");
      this.identityService.setUserPicture(userId, picture);
    }
    if (userInfo != null) {
      for (int i = 0; i < userInfo.size(); i += 2) {
        this.identityService.setUserInfo(userId, (String)userInfo.get(i), (String)userInfo.get(i + 1));
      }
    }
  }
  
  protected void initProcessDefinitions()
  {
    String deploymentName = "Demo processes";
    List<Deployment> deploymentList = this.repositoryService.createDeploymentQuery().deploymentName(deploymentName).list();
    if ((deploymentList == null) || (deploymentList.isEmpty())) {
      this.repositoryService.createDeployment().name(deploymentName).addClasspathResource("org/activiti/explorer/demo/process/createTimersProcess.bpmn20.xml").addClasspathResource("org/activiti/explorer/demo/process/VacationRequest.bpmn20.xml").addClasspathResource("org/activiti/explorer/demo/process/VacationRequest.png").addClasspathResource("org/activiti/explorer/demo/process/FixSystemFailureProcess.bpmn20.xml").addClasspathResource("org/activiti/explorer/demo/process/FixSystemFailureProcess.png").addClasspathResource("org/activiti/explorer/demo/process/simple-approval.bpmn20.xml").addClasspathResource("org/activiti/explorer/demo/process/Helpdesk.bpmn20.xml").addClasspathResource("org/activiti/explorer/demo/process/Helpdesk.png").addClasspathResource("org/activiti/explorer/demo/process/reviewSalesLead.bpmn20.xml").deploy();
    }
    String reportDeploymentName = "Demo reports";
    deploymentList = this.repositoryService.createDeploymentQuery().deploymentName(reportDeploymentName).list();
    if ((deploymentList == null) || (deploymentList.isEmpty())) {
      this.repositoryService.createDeployment().name(reportDeploymentName).addClasspathResource("org/activiti/explorer/demo/process/reports/taskDurationForProcessDefinition.bpmn20.xml").addClasspathResource("org/activiti/explorer/demo/process/reports/processInstanceOverview.bpmn20.xml").addClasspathResource("org/activiti/explorer/demo/process/reports/helpdeskFirstLineVsEscalated.bpmn20.xml").addClasspathResource("org/activiti/explorer/demo/process/reports/employeeProductivity.bpmn20.xml").deploy();
    }
  }
  
  protected void generateReportData()
  {
    Thread thread = new Thread(new Runnable()
    {
      public void run()
      {
        if ((DemoDataConfiguration.this.processEngineConfiguration.isAsyncExecutorEnabled()) && (DemoDataConfiguration.this.processEngineConfiguration.getAsyncExecutor() != null)) {
          DemoDataConfiguration.this.processEngineConfiguration.getAsyncExecutor().shutdown();
        } else if ((!DemoDataConfiguration.this.processEngineConfiguration.isAsyncExecutorEnabled()) && (DemoDataConfiguration.this.processEngineConfiguration.getJobExecutor() != null)) {
          DemoDataConfiguration.this.processEngineConfiguration.getJobExecutor().shutdown();
        }
        Random random = new Random();
        
        Date now = new Date(new Date().getTime() - 86400000L);
        DemoDataConfiguration.this.processEngineConfiguration.getClock().setCurrentTime(now);
        for (int i = 0; i < 50; i++)
        {
          if (random.nextBoolean()) {
            DemoDataConfiguration.this.runtimeService.startProcessInstanceByKey("fixSystemFailure");
          }
          if (random.nextBoolean())
          {
            DemoDataConfiguration.this.identityService.setAuthenticatedUserId("kermit");
            Map<String, Object> variables = new HashMap();
            variables.put("customerName", "testCustomer");
            variables.put("details", "Looks very interesting!");
            variables.put("notEnoughInformation", Boolean.valueOf(false));
            DemoDataConfiguration.this.runtimeService.startProcessInstanceByKey("reviewSaledLead", variables);
          }
          if (random.nextBoolean()) {
            DemoDataConfiguration.this.runtimeService.startProcessInstanceByKey("escalationExample");
          }
          if (random.nextInt(100) < 20)
          {
            now = new Date(now.getTime() - 82800000L);
            DemoDataConfiguration.this.processEngineConfiguration.getClock().setCurrentTime(now);
          }
        }
        List<Job> jobs = DemoDataConfiguration.this.managementService.createJobQuery().list();
        for (int i = 0; i < jobs.size() / 2; i++)
        {
          DemoDataConfiguration.this.processEngineConfiguration.getClock().setCurrentTime(((Job)jobs.get(i)).getDuedate());
          DemoDataConfiguration.this.managementService.executeJob(((Job)jobs.get(i)).getId());
        }
        List<Task> tasks = DemoDataConfiguration.this.taskService.createTaskQuery().list();
        while (!tasks.isEmpty())
        {
          for (Task task : tasks)
          {
            if (task.getAssignee() == null)
            {
              String assignee = random.nextBoolean() ? "kermit" : "fozzie";
              DemoDataConfiguration.this.taskService.claim(task.getId(), assignee);
            }
            DemoDataConfiguration.this.processEngineConfiguration.getClock().setCurrentTime(new Date(task.getCreateTime().getTime() + random.nextInt(3600000)));
            

            DemoDataConfiguration.this.taskService.complete(task.getId());
          }
          tasks = DemoDataConfiguration.this.taskService.createTaskQuery().list();
        }
        DemoDataConfiguration.this.processEngineConfiguration.getClock().reset();
        if ((DemoDataConfiguration.this.processEngineConfiguration.isAsyncExecutorEnabled()) && (DemoDataConfiguration.this.processEngineConfiguration.getAsyncExecutor() != null)) {
          DemoDataConfiguration.this.processEngineConfiguration.getAsyncExecutor().start();
        } else if ((!DemoDataConfiguration.this.processEngineConfiguration.isAsyncExecutorEnabled()) && (DemoDataConfiguration.this.processEngineConfiguration.getJobExecutor() != null)) {
          DemoDataConfiguration.this.processEngineConfiguration.getJobExecutor().start();
        }
        DemoDataConfiguration.LOGGER.info("Demo report data generated");
      }
    });
    thread.start();
  }
  
  protected void initModelData()
  {
    createModelData("Demo model", "This is a demo model", "org/activiti/explorer/demo/model/test.model.json");
  }
  
  protected void createModelData(String name, String description, String jsonFile)
  {
    List<Model> modelList = this.repositoryService.createModelQuery().modelName("Demo model").list();
    if ((modelList == null) || (modelList.isEmpty()))
    {
      Model model = this.repositoryService.newModel();
      model.setName(name);
      
      ObjectNode modelObjectNode = new ObjectMapper().createObjectNode();
      modelObjectNode.put("name", name);
      modelObjectNode.put("description", description);
      model.setMetaInfo(modelObjectNode.toString());
      
      this.repositoryService.saveModel(model);
      try
      {
        InputStream svgStream = getClass().getClassLoader().getResourceAsStream("org/activiti/explorer/demo/model/test.svg");
        this.repositoryService.addModelEditorSourceExtra(model.getId(), IOUtils.toByteArray(svgStream));
      }
      catch (Exception e)
      {
        LOGGER.warn("Failed to read SVG", e);
      }
      try
      {
        InputStream editorJsonStream = getClass().getClassLoader().getResourceAsStream(jsonFile);
        this.repositoryService.addModelEditorSource(model.getId(), IOUtils.toByteArray(editorJsonStream));
      }
      catch (Exception e)
      {
        LOGGER.warn("Failed to read editor JSON", e);
      }
    }
  }
}
