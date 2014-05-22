package com.github.emalock3.spring;

import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {
    
    @Autowired
    private StepBuilderFactory stepBuilderFactory;
    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Bean
    public Job testJob(Step testStep) {
        return jobBuilderFactory.get("testJob")
                .incrementer(new RunIdIncrementer())
                .start(testStep)
                .build();
    }
    
    @Bean
    public Step testStep(ItemReader<String> itemReader,
            ItemProcessor<String, String> itemProcessor,
            ItemWriter<String> itemWriter) {
        return stepBuilderFactory.get("testStep")
                .<String, String> chunk(10)
                .reader(itemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();
    }
    
    @Bean
    @StepScope
    public ItemReader<String> itemReader() {
        return new ListItemReader(
                IntStream.range(0, 100)
                        .mapToObj(i -> String.format("%03d", i + 1))
                        .collect(Collectors.toList())
        );
    }
    
    @Bean
    @StepScope
    public ItemProcessor<String, String> itemProcessor() {
        Random random = new Random();
        return item -> {
            Thread.sleep(Math.abs(random.nextLong()) % 100);
            return item;
        };
    }
    
    @Bean
    @StepScope
    public ItemWriter<String> itemWriter() {
        Random random = new Random();
        return items -> {
            Thread.sleep(Math.abs(random.nextLong()) % 1000);
            System.out.println(Thread.currentThread().getName() + " write items : " + items);
        };
    }
    
}
