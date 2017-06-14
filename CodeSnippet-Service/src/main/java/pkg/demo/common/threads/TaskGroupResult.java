package pkg.demo.common.threads;

import java.util.List;

/**
 * TaskGroupResult is the Object which wraps getting back result for TaskGroup
 * 
 * @author Zhaoxin Jiang
 *
 */
public class TaskGroupResult<T> {

	private String groupName;
	private List<T> resultList;

	public TaskGroupResult() {
	}

	/**
	 * Create TaskResult
	 * 
	 * @param groupName - group name
	 * @param resultList - task list as a result
	 */
	public TaskGroupResult(String groupName, List<T> resultList) {
		this.groupName = groupName;
		this.resultList = resultList;
	}

	/**
	 * Get group name
	 * 
	 * @return group name
	 */
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/**
	 * Get result task list
	 * 
	 * @return result
	 */
	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}

}
