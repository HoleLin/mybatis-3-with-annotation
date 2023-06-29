/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.executor.loader;

import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.ExecutorException;
import org.apache.ibatis.executor.ResultExtractor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.apache.ibatis.transaction.TransactionFactory;

/**
 * MyBatis 中的“延迟加载”是指在查询数据库的时候，MyBatis 不会立即将完整的对象加载到服务内存中，
 * 而是在业务逻辑真正需要使用这个对象或使用到对象中某些属性的时候，才真正执行数据库查询操作，将完整的对象加载到内存中
 * 通过字节码生成方式实现的动态代理，底层依赖 cglib 和 javassit 两个库实现动态代码生成
 * @author Clinton Begin
 */
public class ResultLoader {

  protected final Configuration configuration;
  /**
   * 用于执行延迟 SQL 的线程池
   */
  protected final Executor executor;
  protected final MappedStatement mappedStatement;
  /**
   * SQL 的实参
   */
  protected final Object parameterObject;
  /**
   * 延迟加载的对象类型
   */
  protected final Class<?> targetType;
  protected final ObjectFactory objectFactory;
  protected final CacheKey cacheKey;
  /**
   * 延迟执行的 SQL 语句
   */
  protected final BoundSql boundSql;
  protected final ResultExtractor resultExtractor;
  protected final long creatorThreadId;

  protected boolean loaded;
  protected Object resultObject;

  public ResultLoader(Configuration config, Executor executor, MappedStatement mappedStatement, Object parameterObject,
      Class<?> targetType, CacheKey cacheKey, BoundSql boundSql) {
    this.configuration = config;
    this.executor = executor;
    this.mappedStatement = mappedStatement;
    this.parameterObject = parameterObject;
    this.targetType = targetType;
    this.objectFactory = configuration.getObjectFactory();
    this.cacheKey = cacheKey;
    this.boundSql = boundSql;
    this.resultExtractor = new ResultExtractor(configuration, objectFactory);
    this.creatorThreadId = Thread.currentThread().getId();
  }

  public Object loadResult() throws SQLException {
    // 通过 selectList() 方法执行 boundSql 这条延迟加载的 SQL 语句
    List<Object> list = selectList();
    // 通过 ResultExtractor 从这个 List 集合中提取到延迟加载的真正对象
    resultObject = resultExtractor.extractObjectFromList(list, targetType);
    return resultObject;
  }

  private <E> List<E> selectList() throws SQLException {
    Executor localExecutor = executor;
    if (Thread.currentThread().getId() != this.creatorThreadId || localExecutor.isClosed()) {
      localExecutor = newExecutor();
    }
    try {
      return localExecutor.query(mappedStatement, parameterObject, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER,
          cacheKey, boundSql);
    } finally {
      if (localExecutor != executor) {
        localExecutor.close(false);
      }
    }
  }

  private Executor newExecutor() {
    final Environment environment = configuration.getEnvironment();
    if (environment == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  Environment was not configured.");
    }
    final DataSource ds = environment.getDataSource();
    if (ds == null) {
      throw new ExecutorException("ResultLoader could not load lazily.  DataSource was not configured.");
    }
    final TransactionFactory transactionFactory = environment.getTransactionFactory();
    final Transaction tx = transactionFactory.newTransaction(ds, null, false);
    return configuration.newExecutor(tx, ExecutorType.SIMPLE);
  }

  public boolean wasNull() {
    return resultObject == null;
  }

}
