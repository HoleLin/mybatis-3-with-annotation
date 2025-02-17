/*
 *    Copyright 2009-2022 the original author or authors.
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
package org.apache.ibatis.scripting.xmltags;

/**
 * VarDeclSqlNode 抽象了 <bind> 标签,其核心功能是将一个 OGNL 表达式的值绑定到一个指定的变量名上，并记录到 DynamicContext 上下文中
 * @author Frank D. Martinez [mnesarco]
 */
public class VarDeclSqlNode implements SqlNode {

  private final String name;
  /**
   * 记录了 <bind> 标签中 value 属性的值（一般是一个 OGNL 表达式）
   */
  private final String expression;

  public VarDeclSqlNode(String name, String exp) {
    this.name = name;
    this.expression = exp;
  }

  @Override
  public boolean apply(DynamicContext context) {
    // VarDeclSqlNode 首先会通过 OGNL 工具类解析 expression 这个表达式的值，
    // 然后将解析结果与 name 字段的值一起绑定到 DynamicContext 上下文中，这样后面就可以通过 name 字段值获取这个表达式的值了
    final Object value = OgnlCache.getValue(expression, context.getBindings());
    context.bind(name, value);
    return true;
  }

}
