package pkg.demo.common.threads;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.google.common.collect.Lists;

/**
 * TaskGroup is collection of tasks and is able to add more tasks at once
 * 
 * TaskGroup has a unique group name in the running context. user should create one TaskGroup at least. 
 * if user doesn't create TaskGroup manually, The system will create one TaskGroup automatically.
 * 
 * @author Zhaoxin Jiang
 *
 */
public class TaskGroup<T> implements Comparable<TaskGroup<T>> {
	
	/**
	 * Unique identifier for this group, if none, The system will create one.
	 */
	private String groupName;
	private int order;
	private List<ITaskStepService<T>> taskList = Lists.newArrayList();

	public List<ITaskStepService<T>> getTaskList() {
		return taskList;
	}

	/**
	 * Create a group
	 */
	public TaskGroup() {
		this.groupName = UUID.randomUUID().toString();
		this.order = 0;
	}

	/**
	 * Create a group
	 * 
	 * @param name - group name
	 */
	public TaskGroup(String name) {
		super();
		this.groupName = name;
		this.order = 0;
	}

	/**
	 * Create Group
	 * 
	 * @param name - group name
	 * @param order - execution order in context
	 */
	public TaskGroup(String name, int order) {
		super();
		if(groupName == null || groupName.equals("")){
			this.groupName = UUID.randomUUID().toString();
		}else{
			this.groupName = name;
		}
		this.order = order;
	}

	/**
	 * Add a Task
	 * 
	 * @param task - task
	 * @return taskgroup
	 */
	public TaskGroup<T> addTask(ITaskStepService<T> task) {
		taskList.add(task);
		return this;
	}

	/**
	 * Add batch Tasks
	 * 
	 * @param taskList - task list
	 * @return taskgroup
	 */
	public TaskGroup<T>  addTaskList(List<ITaskStepService<T>> taskList) {
		this.taskList.addAll(taskList);
		return this;
	}

	/**
	 * Get group name
	 * 
	 * @return group name
	 */
	public String getGroupName() {
		return groupName;
	}

	/**
	 * Get order
	 * 
	 * @return group order
	 */
	public int getOrder() {
		return order;
	}

	@Override
	public int compareTo(TaskGroup<T> o) {
		if(Objects.equals(this.order, o.order)){
			return this.groupName.compareTo(o.groupName);
		}else{
			return this.order - o.order;
		}
	}
}
