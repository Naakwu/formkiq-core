/**
 * MIT License
 * 
 * Copyright (c) 2018 - 2020 FormKiQ
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.formkiq.aws.dynamodb;

import java.util.Map;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;

/**
 * 
 * DynamoDB Wrapper Service.
 *
 */
public interface DynamoDbService {

  /**
   * Delete DynamoDb Record.
   * 
   * @param pk {@link AttributeValue}
   * @param sk {@link AttributeValue}
   */
  void deleteItem(AttributeValue pk, AttributeValue sk);

  /**
   * Whether Database Record Exists.
   * 
   * @param pk {@link AttributeValue}
   * @param sk {@link AttributeValue}
   * @return boolean
   */
  boolean exists(AttributeValue pk, AttributeValue sk);

  /**
   * Gets DynamoDB Record.
   * 
   * @param pk {@link AttributeValue}
   * @param sk {@link AttributeValue}
   * @return {@link Map}
   */
  Map<String, AttributeValue> get(AttributeValue pk, AttributeValue sk);

  /**
   * Put DynamoDb Record.
   * 
   * @param attr {@link Map}
   */
  void putItem(Map<String, AttributeValue> attr);

  /**
   * Query DynamoDB Records.
   * 
   * @param pk {@link AttributeValue}
   * @param exclusiveStartKey {@link Map}
   * @param limit int
   * @return {@link QueryResponse}
   */
  QueryResponse query(AttributeValue pk, Map<String, AttributeValue> exclusiveStartKey, int limit);

  /**
   * Query DynamoDB Records.
   * 
   * @param config {@link QueryConfig}
   * @param pk {@link AttributeValue}
   * @param sk {@link AttributeValue}
   * @param exclusiveStartKey {@link Map}
   * @param limit int
   * @return {@link QueryResponse}
   */
  QueryResponse queryBeginsWith(QueryConfig config, AttributeValue pk, AttributeValue sk,
      Map<String, AttributeValue> exclusiveStartKey, int limit);

  /**
   * Update DynamoDB Record.
   * 
   * @param pk {@link String}
   * @param sk {@link String}
   * @param updateValues {@link Map}
   * @return {@link Map}
   */
  Map<String, AttributeValue> updateFields(AttributeValue pk, AttributeValue sk,
      Map<String, AttributeValue> updateValues);
}
