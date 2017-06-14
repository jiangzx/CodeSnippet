package pkg.demo.common.threads;

/**
 * TaskStep is abstract class that must be inherited by subClass which need override [doAction] method to do something,
 * in which the [Step] means execution order of the task you create.
 *
 * @author Zhaoxin Jiang
 *
 */
public abstract class AbstractTaskStep<T> implements ITaskStepService<T> {

	/**
	 * If it's a concurrent task in TaskGroup
	 */
	private boolean isParallel;
	
	/**
	 * Task running order in TaskGroup
	 */
	private int order = 0; 
	
	private Object previousStepResult;

	public void setOrder(int order) {
		this.order = order;
	}
	
	@Override
	public T call() throws Exception {
		if(isParallel()){
			return this.doAction();
		}else{
			return this.doAction(order, previousStepResult);
		}
	}

	@Override
	public int compareTo(AbstractTaskStep<T> target) {
		return this.order > target.order ? 1 : this.order < target.order ? -1 : 0;
	}

	public boolean isParallel() {
		return isParallel;
	}

	public void setParallel(boolean isParallel) {
		this.isParallel = isParallel;
	}

	public Object getPreviousStepResult() {
		return previousStepResult;
	}

	public void setPreviousStepResult(Object previousStepResult) {
		this.previousStepResult = previousStepResult;
	}

}
