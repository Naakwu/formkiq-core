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
package com.formkiq.stacks.common.formats;

/**
 * Supported Conversion Formats.
 *
 */
public enum MimeType {

  /** text/html. */
  MIME_HTML("text/html"),
  /** application/vnd.openxmlformats-officedocument.wordprocessingml.document. */
  MIME_DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
  /** application/pdf. */
  MIME_PDF("application/pdf"),
  /** image/png. */
  MIME_PNG("image/png"),
  /** image/jpeg. */
  MIME_JPEG("image/jpeg"),
  /** application/json. */
  MIME_JSON("application/json"),
  /** Unknown Mime. */
  MIME_UNKNOWN("UNKNOWN");

  /** Content Type. */
  private String contentType;

  /**
   * constructor.
   * 
   * @param type {@link String}
   */
  MimeType(final String type) {
    this.contentType = type;
  }

  /**
   * Get Content Type.
   * 
   * @return {@link String}
   */
  public String getContentType() {
    return this.contentType;
  }

  /**
   * Is Content Type plain text.
   * 
   * @param contentType {@link String}
   * @return boolean
   */
  public static boolean isPlainText(final String contentType) {
    return contentType != null
        && (contentType.startsWith("text/") || "application/json".equals(contentType)
            || "application/x-www-form-urlencoded".equals(contentType));
  }
}
