/*
 * Copyright 2019 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.couchbase.client.core.error;

import com.couchbase.client.core.annotation.Stability;
import com.couchbase.client.core.deps.io.netty.handler.codec.http.HttpResponseStatus;

@Stability.Internal
public class HttpStatusCodeException extends RuntimeException {
  private final int code;

  public HttpStatusCodeException(int code) {
    super("Unexpected HTTP status code " + code);
    this.code = code;
  }

  public HttpStatusCodeException(HttpResponseStatus status) {
    this(status.code());
  }

  public int code() {
    return code;
  }
}