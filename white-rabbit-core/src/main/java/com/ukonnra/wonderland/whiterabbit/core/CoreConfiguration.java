package com.ukonnra.wonderland.whiterabbit.core;

import com.ukonnra.wonderland.whiterabbit.core.entity.AbstractEntity;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.DecoratingProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.CrudMethodMetadata;
import org.springframework.nativex.hint.JdkProxyHint;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
@EntityScan(basePackageClasses = AbstractEntity.class)
@EnableJpaAuditing
@ComponentScan
/*
 For error when compiling from GitHub Action:
 <code>
  com.oracle.svm.core.jdk.UnsupportedFeatureError: Proxy class defined by interfaces
  [ interface org.springframework.data.jpa.repository.support.CrudMethodMetadata,
    interface org.springframework.aop.SpringProxy,
    interface org.springframework.aop.framework.Advised,
    interface org.springframework.core.DecoratingProxy ]
  not found
 </code>
*/
@JdkProxyHint(
    types = {CrudMethodMetadata.class, SpringProxy.class, Advised.class, DecoratingProxy.class})
public class CoreConfiguration {}
