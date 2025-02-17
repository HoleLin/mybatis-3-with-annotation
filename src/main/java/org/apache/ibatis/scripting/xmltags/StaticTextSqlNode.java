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
package org.apache.ibatis.scripting.xmltags;

/**
 * StaticTextSqlNode 用于表示非动态的 SQL 片段
 * @author Clinton Begin
 */
public class StaticTextSqlNode implements SqlNode {
  /**
   * 用于记录非动态 SQL 片段的文本内容
   */
  private final String text;

  public StaticTextSqlNode(String text) {
    this.text = text;
  }

  @Override
  public boolean apply(DynamicContext context) {
    // apply() 方法会直接将 text 字段值追加到 DynamicContext.sqlBuilder 的最末尾
    context.appendSql(text);
    return true;
  }

}
