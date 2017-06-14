package pkg.demo.config;

import org.quartz.JobDetail;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.spi.JobFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.CronTriggerFactoryBean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import pkg.demo.common.spring.SpringAutoWiringSpringBeanJobFactory;
import pkg.demo.schedule.SimpleSchedule;


/**
 * Job Configuration
 * @author zhajiang
 *
 */
@Configuration
@ConditionalOnProperty("enable-scheduling")
public class ScheduleConfig {


	@Bean
	public JobFactory springBeanJobFactory(ApplicationContext applicationContext) {
		SpringAutoWiringSpringBeanJobFactory jobFactory = new SpringAutoWiringSpringBeanJobFactory();
		jobFactory.setApplicationContext(applicationContext);
		return jobFactory;
	}

	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * ~~~~~~~~~~~~~ DEMO JOB ~~~~~~~~~~~~~~~~~~~
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
	@Bean(name = "demoJobFactory")
	public SchedulerFactoryBean scheduler(@Qualifier("demoJobTrigger") Trigger trigger,
			@Qualifier("demoJobDetail") JobDetail job, JobFactory jobFactory) {
		SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();
		schedulerFactory.setConfigLocation(new ClassPathResource("config/quartz.properties"));
		schedulerFactory.setJobFactory(jobFactory);
		schedulerFactory.setJobDetails(job);
		schedulerFactory.setTriggers(trigger);
		return schedulerFactory;
	}
	
	@Bean(name = "demoJobDetail")
	public JobDetailFactoryBean asrJobDetail() {
		JobDetailFactoryBean factoryBean = new JobDetailFactoryBean();
		factoryBean.setJobClass(SimpleSchedule.class);
		factoryBean.setDurability(true);
		return factoryBean;
	}

	@Bean(name = "demoJobTrigger")
	public CronTriggerFactoryBean asrJobTrigger(@Qualifier("demoJobDetail") JobDetail jobDetail,
			@Value("${demo.job.expression}") String cronExpression) {
		CronTriggerFactoryBean factoryBean = new CronTriggerFactoryBean();
		factoryBean.setJobDetail(jobDetail);
		factoryBean.setCronExpression(cronExpression);
		factoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
		return factoryBean;
	}
	
	/*~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 * ~~~~~~~~~~~~~ DEMO JOB ~~~~~~~~~~~~~~~~~~~
	 * ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	 */
}
