package pkg.demo.common.threads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

/**
 * TaskGroupController is the controller used to organize groups and
 * keep tasks running asynchronously in each group.
 * 
 * TaskGroupController is able to add more groups at once and the user have to 
 * call submit method to run tasks at last. After that, result get back as TaskResult list 
 * which will keep firstly the order defined
 * 
 * 
 * @author Zhaoxin Jiang
 * 
 */
public class TaskGroupController<T extends Object> {

	private static final Logger logger = Logger.getLogger(TaskGroupController.class.getName());
	private TreeSet<TaskGroup<T>> groupCollection = Sets.newTreeSet();
	private Map<String, TaskGroupContext> groupContextMap = Maps.newConcurrentMap();
	private ExecutorService executorService;
	private List<TaskGroupResult<T>> groupList = Lists.newArrayList();
	private static final int DEFAULT_POOL_SIZE = 5;

	private static TaskGroupController<Object> instance;

	/**
	 * Create instance
	 * 
	 * @return instance
	 */
	public static synchronized TaskGroupController<Object> me(){
		if (instance == null) {  
	        instance = new TaskGroupController<Object> ();  
	    }  
		return instance;
	}
	
	/**
	 * Create instance with fixed thread pool size
	 * 
	 * @param threadPoolSize - thread pool size
	 * @return instance
	 */
	public static synchronized TaskGroupController<Object> me(int threadPoolSize){
		if (threadPoolSize < 0) {
			throw new RuntimeException("error : runtime thread pool size should greater than zero");
		}
		if (instance == null) {  
	        instance = new TaskGroupController<Object> (threadPoolSize);  
	    }  
		return instance;
	}

	/**
	 * Create instance with specific executorService
	 * 
	 * @param executorService - executorService
	 * @return instance
	 */
	public static synchronized TaskGroupController<Object> me(ExecutorService executorService){
		if (executorService == null) {
			throw new RuntimeException("error : execute service can not be null");
		}
		if (instance == null) {  
	        instance = new TaskGroupController<Object> (executorService);  
	    }  
		return instance;
	}	
	/**
	 * Create task list without group
	 * 
	 * @param taskList - task list
	 * @return instance
	 */
	public TaskGroupController<T> createDirectTaskList(List<ITaskStepService<T>> taskList) {
		groupCollection.clear();
		groupContextMap.clear();
		TaskGroup<T> group = new TaskGroup<T>().addTaskList(taskList);
		addGroup(group);
		return this;
	}

	/**
	 * default executeService with 20 threads in thread pool
	 */
	private TaskGroupController() {
		this.executorService = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);
	}

	/**
	 * set thread pool size for TaskGroupController
	 * 
	 * @param threadPoolSize - max threads count
	 */
	private TaskGroupController(int threadPoolSize) {
		this.executorService = Executors.newFixedThreadPool(threadPoolSize);
	}

	/**
	 * set custom executeService class for TaskGroupController
	 * 
	 * @param executorService - custom ExecutorService instance
	 */
	private TaskGroupController(ExecutorService executorService) {
		this.executorService = executorService;
	}

	/**
	 * Add TaskGroup
	 * 
	 * @param group - task group
	 * @return return self
	 */
	public TaskGroupController<T> addGroup(TaskGroup<T> group) {
		List<ITaskStepService<T>> taskList = group.getTaskList();
		if(taskList.isEmpty()){
			throw new RuntimeException("error : Group [" + group + "] can not be empty");
		}
		String name = group.getGroupName();
		TaskGroupContext groupContext = groupContextMap.get(name);
		if (groupContext == null) {
			groupContext = new TaskGroupContext(group.getTaskList().size());
		}
		groupContext.addTasklist(taskList);
		groupContextMap.put(name, groupContext);
		groupCollection.add(group);
		return this;
	}

	/**
	 * do submit and get result for group level
	 * 
	 * @return result for group
	 */
	public List<TaskGroupResult<T>> submitAndGetGroupResult() {
		for (TaskGroup<T> taskGroup : groupCollection) {
			TaskGroupContext context = groupContextMap.get(taskGroup.getGroupName());
			context.exec();
			List<T> resultList = context.getResult();
			TaskGroupResult<T> r = new TaskGroupResult<T>(taskGroup.getGroupName(), resultList);
			groupList.add(r);
			logger.log(Level.FINE, "Task Group [" + taskGroup.getGroupName() + "] has been submitted successfully");
		}
		return groupList;
	}
	
	/**
	 * do submit and get result
	 * 
	 * @return result 
	 */
	public List<T> submitAndGetResult(){
		List<T> resultList = Lists.newArrayList();
		List<TaskGroupResult<T>> groupResultList = this.submitAndGetGroupResult();
		for(TaskGroupResult<T> group : groupResultList){
			resultList.addAll(group.getResultList());
		}
		return resultList;
	}

	/**
	 * TaskGroupContext encapsulates the state information for result
	 * The TaskGroupContext object is called for internal use by TaskGroupController
	 * which also has methods to execute task and controls the task execution order.
	 * 
	 * @author Zhaoxin Jiang
	 *
	 */
	private class TaskGroupContext {

		private ListeningExecutorService service = MoreExecutors.listeningDecorator(executorService);
		private PriorityBlockingQueue<ITaskStepService<T>> orderingTaskList = Queues.newPriorityBlockingQueue();
		private ArrayList<ITaskStepService<T>> parallelTaskList = Lists.newArrayList();
		private List<ListenableFuture<T>> futures = Lists.newArrayList();
		private CountDownLatch latch = new CountDownLatch(1);
		private List<T> resultCollection = Lists.newArrayList();

		public List<T> getResult() {
			return resultCollection;
		}

		/**
		 * Create TaskGroupContext
		 * 
		 * @param count - task size
		 */
		public TaskGroupContext(int count) {
			// resultCollection = Queues.newLinkedBlockingQueue(capacity);
			resultCollection = new ArrayList<T>(count);
		}

		/**
		 * Add Task List
		 * 
		 * @param taskList - task list
		 */
		public void addTasklist(List<ITaskStepService<T>> taskList) {
			for (ITaskStepService<T> task : taskList) {
				if (task.isParallel()) {
					parallelTaskList.add(task);
				} else {
					orderingTaskList.add(task);
				}
			}
		}

		/**
		 * Get ordering task count
		 * 
		 * @return size
		 */
		public int countOrderTask() {
			return orderingTaskList.size();
		}

		/**
		 * Get parallel task count
		 * 
		 * @return size
		 */
		public int countParallelTask() {
			return parallelTaskList.size();
		}

		/**
		 * execute task
		 * 
		 * @return success/fail
		 */
		public boolean exec() {
			int taskCount = countParallelTask();
			// submit all concurrent tasks
			if (taskCount > 0) {
				submitTaskWithParallel(taskCount);
			} else {
				// submit all Ordering tasks
				submitTaskWithOrder(null);
			}
			try {
				latch.await();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, String.format("%s has been interrupted", Thread.currentThread()), e);
			}
			return true;
		}

		/**
		 * Submit parallel task
		 * 
		 * @param taskCount - task count
		 */
		private void submitTaskWithParallel(int taskCount) {
			for (int i = 0; i < taskCount; i++) {
				ITaskStepService<T> task = parallelTaskList.get(i);
				ListenableFuture<T> future = service.submit(task);
				futures.add(future);
			}
			ListenableFuture<List<T>> successTasks = Futures.successfulAsList(futures);
			Futures.addCallback(successTasks, new FutureCallback<List<T>>() {
				@Override
				public void onFailure(Throwable err) {
					logger.log(Level.WARNING, "Concurrent job executed failed", err);
					submitTaskWithOrder(null);
				}

				@Override
				public void onSuccess(List<T> entity) {
					logger.log(Level.INFO, entity.toString());
					resultCollection.addAll(entity);
					submitTaskWithOrder(entity); // 并行后的任务结果一把传给串行任务
				}
			});
			List<T> resultList = null;
			try {
				resultList = successTasks.get();
			} catch (InterruptedException e) {
				logger.log(Level.WARNING, String.format("%s has been interrupted", Thread.currentThread()), e);
			} catch (ExecutionException e) {
				logger.log(Level.WARNING, String.format("%s has errors", Thread.currentThread()), e);
			}
			logger.log(Level.FINE, Arrays.toString(resultList.toArray()));
		}

		/**
		 * Submit ordering/concurrent job
		 * 
		 */
		private void submitTaskWithOrder(Object previousStepResult) {
			if (countOrderTask() == 0) {
				latch.countDown();
				return;
			}
			ITaskStepService<T> task = orderingTaskList.poll();
			task.setPreviousStepResult(previousStepResult);
			ListenableFuture<T> future = service.submit(task);
			Futures.addCallback(future, new FutureCallback<T>() {
				@Override
				public void onFailure(Throwable err) {
					logger.log(Level.WARNING, "Ordering job executed failed", err);
					submitTaskWithOrder(err); // Pass previous failed object
				}
				@Override
				public void onSuccess(T entity) {
					logger.log(Level.INFO, entity.toString());
					resultCollection.add(entity);
					submitTaskWithOrder(entity);
				}
			});
		}
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

}
