##### how to use TaskGroupController class to handle serial tasks combined with parallel tasks.

#####  创建1000个串行任务和1000个并行任务，将它们分组后，按分组的顺序执行
```java
// 步骤1 - 创建1000个并行任务
List<ITaskStepService<Object>> parallelTaskList = Lists.newArrayList();
for (int i = 1; i < 1000; i++) {
	parallelTaskList.add(new MyParallelTask());
}
// 步骤2 - 给这1000个并行任务创建一个组,TaskGroup 的第一个参数是给定一个唯一名称(不填的话(=null)系统会自动生成一个唯一名称), 第二个参数是Group的执行顺序,比如1要比2先执行
TaskGroup<Object> parallelTaskListGroup = new TaskGroup<Object>("myParallelGroup",2).addTaskList(parallelTaskList);
// 步骤3 - 创建1000个串行任务 
List<ITaskStepService<Object>> sequentialTaskList = Lists.newArrayList();
for (int i = 1; i < 1000; i++) {
	sequentialTaskList.add(new MyOrderTask(i));
}
// 步骤4 - 给这1000个串行任务创建一个组
TaskGroup<Object> sequentialTaskListGroup = new TaskGroup<Object>("mySquentialGroup",1).addTaskList(sequentialTaskList);
// 步骤5 - 提交后拿到结果，因为串行任务优先级(=1)比并行任务(=2)要高, 所以实际上串行任务先执行完后再执行并行任务，所以拿到的结果集里串行的任务在前而并行任务在后.
// 	      PS: 可以把1与2的顺序调一下，看看执行结果
List<Object> list = TaskGroupController
		.me(Executors.newFixedThreadPool(1))
		.addGroup(sequentialTaskListGroup)
		.addGroup(parallelTaskListGroup)
		.submitAndGetResult();		

logger.log(Level.WARNING, String.format("结果集大小-[%d]", list.size()));

```

##### 更多例子请参照 src/test/java/com/webex/cloudservice/common/threads/Testing.java