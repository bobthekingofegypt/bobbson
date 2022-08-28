/*
 * Copyright 2008-present MongoDB, Inc.
 * Copyright 2022-present Bob.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bobstuff.bobbson;

public enum BsonState {
  /** The initial state. */
  INITIAL,

  /** The reader is positioned at the type of an element or value. */
  TYPE,

  /** The reader is positioned at the name of an element. */
  NAME,

  /** The reader is positioned at a value. */
  VALUE,

  /** The reader is positioned at a scope document. */
  SCOPE_DOCUMENT,

  /** The reader is positioned at the end of a document. */
  END_OF_DOCUMENT,

  /** The reader is positioned at the end of an array. */
  END_OF_ARRAY,

  /** The reader has finished reading a document. */
  DONE,

  /** The reader is closed. */
  CLOSED
}
