package pkg.demo.test;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;

import com.google.common.collect.Lists;

import pkg.demo.common.threads.ITaskStepService;
import pkg.demo.common.threads.TaskGroup;
import pkg.demo.common.threads.TaskGroupController;
import pkg.demo.common.threads.TaskStepConcurrent;
import pkg.demo.common.threads.TaskStepOrdering;

/**
 * Getting started Examples
 * 
 * @author Zhaoxin Jiang
 * 
 */
public class ThreadsUtilSample {
	
	private final Logger logger = Logger.getLogger(getClass().getName());
	
	/**
	 * Sample1  - 创建1000个串行任务
	 */
	@Test
    public void run_sequential_task_test() {
    	// 步骤1 - 创建1000个串行任务
		List<ITaskStepService<Object>> taskList = Lists.newArrayList();
		for (int i = 1; i < 1000; i++) {
			taskList.add(new MyOrderTask(i));
		}
		// 步骤2 - 提交后拿到结果, 结果集的顺序是按照任务的添加顺序排序的
		List<Object> list = TaskGroupController
							.me()
							.createDirectTaskList(taskList)
							.submitAndGetResult();
		logger.log(Level.FINE, String.format("Task size: %d", list.size()));
	}
	
	/**
	 * Sample2  - 创建1000个并行任务
	 */
	@Test
    public void run_concurrent_task__test() {
    	// 步骤1 - 创建1000个并行任务
		List<ITaskStepService<Object>> taskList = Lists.newArrayList();
		for (int i = 1; i < 1000; i++) {
			taskList.add(new MyParallelTask());
		}
		// 步骤2 - 提交后拿到结果, 结果集的顺序是按照任务的添加顺序排序的
		List<Object> list = TaskGroupController
							.me(5)
							.createDirectTaskList(taskList)
							.submitAndGetResult();
		logger.log(Level.WARNING, String.format("Task size: %d", list.size()));
	}
	
	/**
	 * Sample3 -  创建1000个串行任务和1000个并行任务，将它们分组后，按分组的顺序执行
	 * 注意: TaskGroup 的第一个参数是给定一个唯一名称(不填的话(=null)系统会自动生成一个唯一名称), 第二个参数是Group的执行顺序,比如1要比2先执行
	 */
	@Test
    public void run_task_by_order_mixed_by_parallel_test() {
		
		// 步骤1 - 创建1000个并行任务
		List<ITaskStepService<Object>> parallelTaskList = Lists.newArrayList();
		for (int i = 1; i < 1000; i++) {
			parallelTaskList.add(new MyParallelTask());
		}
		// 步骤2 - 给这1000个并行任务创建一个组
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
		
	}
	
	/*****************************************************************************/
	/**************************** SAMPLE CLASS ***********************************/
	/*****************************************************************************/
	
	/**
	 * Sample class  - 创建串行任务类，继承自TaskStepOrdering，表示这是一个串行行为
	 * 
	 */
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
	
	/**
	 * Sample class - 创建并行任务类，继承自TaskStepConcurrent，表示这是一个并行行为
	 * 
	 */
	public class MyParallelTask extends TaskStepConcurrent<Object>{
		private final String[] fruits = {"apple","pear","strawberry","orange","pineapple","grape"};
		private final Random ran = new Random();
		@Override
		public String doWithConcurent() {
			logger.log(Level.WARNING, String.format("[并行任务]当前线程-[%s]", Thread.currentThread().getName()));
			return fruits[ran.nextInt(6)];
		}
	}
	
}
