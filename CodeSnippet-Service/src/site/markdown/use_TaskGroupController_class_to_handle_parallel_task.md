##### how to use TaskGroupController class to handle parallel task.

##### 创建1000个并行任务
```java

// 步骤1 - 创建并行任务类，继承自TaskStepConcurrent，表示这是一个并行行为
public class MyParallelTask extends TaskStepConcurrent<Object>{
	private final String[] fruits = {"apple","pear","strawberry","orange","pineapple","grape"};
	private final Random ran = new Random();
	@Override
	public String doWithConcurent() {
		logger.log(Level.WARNING, String.format("[并行任务]当前线程-[%s]", Thread.currentThread().getName()));
		return fruits[ran.nextInt(6)];
	}
}
// 步骤2 - 创建1000个并行任务
List<ITaskStepService<Object>> taskList = Lists.newArrayList();
for (int i = 1; i < 1000; i++) {
	taskList.add(new MyParallelTask());
}
// 步骤3 - 提交后拿到结果, 结果集的顺序是按照任务的添加顺序排序的
List<Object> list = TaskGroupController
					.me(5)
					.createDirectTaskList(taskList)
					.submitAndGetResult();
logger.log(Level.WARNING, String.format("Task size: %d", list.size()));


```

##### 更多例子请参照 src/test/java/com/webex/cloudservice/common/threads/Testing.java