package pkg.demo.common.threads;
/**
 * Concurrent Task
 * 
 * @author Zhaoxin Jiang
 *
 */
public abstract class TaskStepConcurrent<T> extends AbstractTaskStep<T>{
	public TaskStepConcurrent(){
		this.setParallel(true);
	}

	@Override
	public T doAction() {
		return this.doWithConcurent();
	}

	@Override
	public T doAction(int order, Object previousStepResult) {
		return null;
	}
	
	public abstract T doWithConcurent();
	
}
