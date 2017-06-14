##### how to use TaskGroupController class to handle serial task.

##### 创建1000个串行任务
```java
// 步骤1 - 创建串行任务类，继承自TaskStepOrdering，表示这是一个串行行为
public class MyOrderTask extends TaskStepOrdering<Object>{
	public MyOrderTask(int order) {
		super(order);
	}
	@Override // (previousStepResult)是前一个任务的执行结果, (order)是当前任务的执行顺序
	public Integer doWithOrder(int order, Object previousStepResult) {
		logger.log(Level.WARNING, String.format("[串行任务]当前组内执行顺序-[%d], 拿到前一个执行的结果对象为-[%s]",order, previousStepResult.toString()));
		logger.log(Level.WARNING, String.format("[串行任务]当前线程-[%s],输出-[%d]", Thread.currentThread().getName(), order));
		return order;
	}
}
// 步骤2 - 创建1000个串行任务
List<ITaskStepService<Object>> taskList = Lists.newArrayList();
for (int i = 1; i < 1000; i++) {
	taskList.add(new MyOrderTask(i));
}
// 步骤3 - 提交后拿到结果，结果集的顺序是按照任务的添加顺序排序的
List<Object> list = TaskGroupController
					.me()
					.createDirectTaskList(taskList)
					.submitAndGetResult();
logger.log(Level.FINE, String.format("Task size: %d", list.size()));

```

##### 更多例子请参照 src/test/java/com/webex/cloudservice/common/threads/Testing.java