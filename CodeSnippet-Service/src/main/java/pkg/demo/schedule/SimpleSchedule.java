package pkg.demo.schedule;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import pkg.demo.service.IDemoService;

public class SimpleSchedule implements Job {

	@Autowired
	private IDemoService demoService; 
	
	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		System.out.println(new Date().getTime());
		demoService.sechduleJob();
	}

}
