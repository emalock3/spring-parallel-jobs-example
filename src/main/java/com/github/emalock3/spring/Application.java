package com.github.emalock3.spring;

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@EnableAutoConfiguration
@ComponentScan
public class Application {
    public static void main(String ... args) {
        try (ConfigurableApplicationContext context = 
                new SpringApplicationBuilder(Application.class)
                .showBanner(false)
                .run(args)) {
            IntStream.range(0, 3).mapToObj(i -> {
                final Job job = context.getBean(Job.class);
                JobLauncher jobLauncher = context.getBean(JobLauncher.class);
                final JobParameters params = new JobParametersBuilder()
                        .addString("job-id", System.currentTimeMillis() + "-" + (i + 1))
                        .toJobParameters();
                Thread t = new Thread(() -> {
                    try {
                        jobLauncher.run(job, params);
                    } catch (JobParametersInvalidException | JobExecutionAlreadyRunningException
                            | JobInstanceAlreadyCompleteException | JobRestartException e) {
                        throw new RuntimeException(e);
                    }
                });
                t.start();
                return t;
            }).collect(Collectors.toList()).forEach(t -> {
                try {
                    // Deadlock detected...
                    t.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
