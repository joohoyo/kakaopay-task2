package com.joohoyo.kakaopay.task;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoDBConfig extends AbstractMongoClientConfiguration {
    @Override
    protected String getDatabaseName() {
        return "kakaopay";
    }

    @Override
    protected boolean autoIndexCreation() {
        return true;
    }
}
