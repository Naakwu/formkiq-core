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
package com.formkiq.stacks.api.handler;

import static com.formkiq.aws.dynamodb.SiteIdKeyGenerator.createS3Key;
import static com.formkiq.aws.services.lambda.ApiResponseStatus.SC_NOT_FOUND;
import static com.formkiq.aws.services.lambda.ApiResponseStatus.SC_OK;
import static software.amazon.awssdk.utils.StringUtils.isEmpty;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.formkiq.aws.dynamodb.DynamoDbConnectionBuilder;
import com.formkiq.aws.dynamodb.model.DocumentItem;
import com.formkiq.aws.s3.PresignGetUrlConfig;
import com.formkiq.aws.s3.S3Service;
import com.formkiq.aws.services.lambda.ApiAuthorizer;
import com.formkiq.aws.services.lambda.ApiGatewayRequestEvent;
import com.formkiq.aws.services.lambda.ApiGatewayRequestEventUtil;
import com.formkiq.aws.services.lambda.ApiGatewayRequestHandler;
import com.formkiq.aws.services.lambda.ApiRequestHandlerResponse;
import com.formkiq.aws.services.lambda.exceptions.NotFoundException;
import com.formkiq.module.lambdaservices.AwsServiceCache;
import com.formkiq.stacks.api.ApiEmptyResponse;
import com.formkiq.stacks.api.ApiUrlResponse;
import com.formkiq.stacks.dynamodb.DocumentFormat;
import com.formkiq.stacks.dynamodb.DocumentService;
import com.formkiq.stacks.dynamodb.DocumentVersionService;

/** {@link ApiGatewayRequestHandler} for "/documents/{documentId}/url". */
public class DocumentIdUrlRequestHandler
    implements ApiGatewayRequestHandler, ApiGatewayRequestEventUtil {

  /**
   * constructor.
   *
   */
  public DocumentIdUrlRequestHandler() {}

  @Override
  public ApiRequestHandlerResponse get(final LambdaLogger logger,
      final ApiGatewayRequestEvent event, final ApiAuthorizer authorizer,
      final AwsServiceCache awsservice) throws Exception {

    String documentId = event.getPathParameters().get("documentId");
    String siteId = authorizer.getSiteId();
    boolean inline = "true".equals(getParameter(event, "inline"));

    DocumentService documentService = awsservice.getExtension(DocumentService.class);
    DocumentItem item = documentService.findDocument(siteId, documentId);

    if (item == null) {
      throw new NotFoundException("Document " + documentId + " not found.");
    }

    String versionKey = getParameter(event, "versionKey");
    if (!isEmpty(versionKey) && !versionKey.startsWith("document#")) {
      versionKey = URLDecoder.decode(versionKey, StandardCharsets.UTF_8);
    }

    DocumentVersionService versionService = awsservice.getExtension(DocumentVersionService.class);
    DynamoDbConnectionBuilder connection = awsservice.getExtension(DynamoDbConnectionBuilder.class);
    String versionId = versionService.getVersionId(connection, siteId, documentId, versionKey);

    URL url = getS3Url(logger, authorizer, awsservice, event, item, versionId, inline);

    return url != null
        ? new ApiRequestHandlerResponse(SC_OK, new ApiUrlResponse(url.toString(), documentId))
        : new ApiRequestHandlerResponse(SC_NOT_FOUND, new ApiEmptyResponse());
  }

  /**
   * Look at {@link ApiGatewayRequestEvent} for "duration".
   * 
   * @param event {@link ApiGatewayRequestEvent}
   * @return int
   */
  private int getDurationHours(final ApiGatewayRequestEvent event) {
    final int defaultDurationHours = 48;

    Map<String, String> map =
        event.getQueryStringParameters() != null ? event.getQueryStringParameters()
            : new HashMap<>();
    String durationHours = map.getOrDefault("duration", "" + defaultDurationHours);

    try {
      return Integer.parseInt(durationHours);
    } catch (NumberFormatException e) {
      return defaultDurationHours;
    }
  }

  @Override
  public String getRequestUrl() {
    return "/documents/{documentId}/url";
  }

  /**
   * Get S3 URL.
   * 
   * @param logger {@link LambdaLogger}
   * @param authorizer {@link ApiAuthorizer}
   * @param awsservice {@link AwsServiceCache}
   * @param event {@link ApiGatewayRequestEvent}
   * @param item {@link DocumentItem}
   * @param versionId {@link String}
   * @param inline boolean
   * @return {@link URL}
   */
  private URL getS3Url(final LambdaLogger logger, final ApiAuthorizer authorizer,
      final AwsServiceCache awsservice, final ApiGatewayRequestEvent event, final DocumentItem item,
      final String versionId, final boolean inline) {

    final String documentId = item.getDocumentId();

    URL url = null;
    String contentType = getContentType(event);
    String siteId = authorizer.getSiteId();
    int hours = getDurationHours(event);
    Duration duration = Duration.ofHours(hours);

    if (awsservice.debug()) {
      logger.log("Finding S3 Url for 'Content-Type' " + contentType);
    }

    S3Service s3Service = awsservice.getExtension(S3Service.class);

    PresignGetUrlConfig config =
        new PresignGetUrlConfig().contentDispositionByPath(item.getPath(), inline);

    if (contentType == null || contentType.equals(item.getContentType())) {

      logger.log("Found default format " + contentType + " for siteId: " + siteId + " documentId: "
          + documentId);

      config.contentType(item.getContentType());
      String s3key = createS3Key(siteId, documentId);
      url = s3Service.presignGetUrl(awsservice.environment("DOCUMENTS_S3_BUCKET"), s3key, duration,
          versionId, config);

    } else {

      config.contentType(item.getContentType());

      DocumentService documentService = awsservice.getExtension(DocumentService.class);
      Optional<DocumentFormat> format =
          documentService.findDocumentFormat(siteId, documentId, contentType);

      if (format.isPresent()) {

        if (awsservice.debug()) {
          logger.log("Found format " + contentType + " for siteId: " + siteId + " documentId: "
              + documentId);
        }

        String s3key = createS3Key(siteId, documentId, contentType);
        url = s3Service.presignGetUrl(awsservice.environment("DOCUMENTS_S3_BUCKET"), s3key,
            duration, versionId, config);

      } else if (awsservice.debug()) {

        logger.log("Cannot find format " + contentType + " for siteId: " + siteId + " documentId: "
            + documentId);
      }
    }

    return url;
  }
}
