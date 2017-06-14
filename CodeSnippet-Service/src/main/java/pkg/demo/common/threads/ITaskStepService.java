package pkg.demo.common.threads;

import java.util.concurrent.Callable;
/**
 * TaskStep Service provides basic operation for Task behavior
 * 
 * @author Zhaoxin Jiang
 *
 */
public interface ITaskStepService<T> extends Callable<T>, Comparable<AbstractTaskStep<T>>{
	public void setOrder(int order);
	public boolean isParallel();
	public void setParallel(boolean isParallel);
	public abstract T doAction();
	public abstract T doAction(int order, Object previousStepResult);
	public void setPreviousStepResult(Object previousStepResult);
}
