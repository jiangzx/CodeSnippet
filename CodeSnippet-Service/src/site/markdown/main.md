A Tiny tool to reduce your effort on Multithreaded programming

Features
--------------------
1. Advanced encapsulation to threads features, let developer focus on business logic code
2. Support serial tasks and parallel tasks working together
3. Support binding dependences between previous task and next task in one group
4. Provide interface to extend custom task class for business needs

Architecture
--------------------
![design](images/design.png)

Caveats
--------
1. If serial tasks and parallel tasks work together in same group, parallel tasks is prior to serial tasks.
1. From above case, if need to  the serials tasks first, serials tasks and parallel tasks have to be assigned to different groups.
2. Tasks from different group have no relationship with each other, that means tasks from group-ONE could not get result from the tasks from group-TWO,
3. General cases is only one group for tasks running, in this case, no need to create group instead of using createDirectTaskList method to create tasks directly.
4. The sequence for the Get-Back result after task running align to the sequence of adding to the group at the beginning. 
5. Normally tasks from different groups will be combined together as a result list after task running over. also be able to call submitAndGetGroupResult to get group level result

Environment
----------
jdk1.8

Latest release
--------------

The current release is  **cmse-common-threads-0.0.1-SNAPSHOT**

To add a dependency to your project using Maven as follows:

```xml
<dependency>
  <groupId>com.webex.cloudservice</groupId>
  <artifactId>cmse-common-threads</artifactId>
  <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Add repository as follows to get latest version :

```xml
<repositories>
  <repository>
    <id>cmse-mrepo-releases</id>
    <name>CMSE Release Repository</name>
    <url>http://mrepo-cmse.qa.webex.com/nexus/content/repositories/releases/</url>
  </repository>
  <repository>
    <id>cmse-mrepo-snapshots</id>
    <name>CMSE Snapshot Repository</name>
    <url>http://mrepo-cmse.qa.webex.com/nexus/content/repositories/snapshots/</url>
  </repository>
</repositories>
```

Getting Started
--------------

##### Simple example: 1000 concurrent tasks
```java
//Step 1 - create a parallel task class that inherits from TaskStepConcurrent,  this was a parallel behaviour
public class MyParallelTask extends TaskStepConcurrent<Object>{
	private final String[] fruits = {"apple","pear","strawberry","orange","pineapple","grape"};
	private final Random ran = new Random();
	@Override
	public String doWithConcurent() {
		logger.log(Level.WARNING, String.format("[并行任务]当前线程-[%s]", Thread.currentThread().getName()));
		return fruits[ran.nextInt(6)];
	}
}
// Step 2 - create 1000 concurrent tasks
List<ITaskStepService<Object>> taskList = Lists.newArrayList();
for (int i = 1; i < 1000; i++) {
	taskList.add(new MyParallelTask());
}
// Step 3-submit and get results
List<Object> list = TaskGroupController
					.me(5)
					.createDirectTaskList(taskList)
					.submitAndGetResult();
logger.log(Level.WARNING, String.format("Task size: %d", list.size()));
```

FAQ
--------------------
Coming up soon...

##### Read more examples, please refer to code repos: src/test/java/com/webex/cloudservice/common/threads/Testing.java

