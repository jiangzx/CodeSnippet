package pkg.demo.common.threads;
/**
 * Ordering Task
 * 
 * @author Zhaoxin Jiang
 *
 */
public abstract class TaskStepOrdering<T> extends AbstractTaskStep<T>{
	
	public TaskStepOrdering(int order){
		this.setOrder(order);
		this.setParallel(false);
	}

	@Override
	public T doAction() {
		return null;
	}

	@Override
	public T doAction(int order, Object lastStepResult) {
		return this.doWithOrder(order, lastStepResult);
	}
	
	public abstract T doWithOrder(int order, Object previousStepResult);
	
}
