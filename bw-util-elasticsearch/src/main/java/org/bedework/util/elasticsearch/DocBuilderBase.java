/* ********************************************************************
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
*/
package org.bedework.util.elasticsearch;

import org.bedework.util.indexing.IndexException;
import org.bedework.util.logging.BwLogger;
import org.bedework.util.logging.Logged;
import org.bedework.util.misc.Util;

import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.Collection;

/** Build documents for ElasticSearch
 *
 * @author Mike Douglass douglm - rpi.edu
 *
 */
public class DocBuilderBase implements Logged {
  public static final String docTypeUpdateTracker = "updateTracker";

  public static final String updateTrackerId = "updateTracker";

  private final XContentBuilder builder;

  //private IndexProperties props;

  /**
   *
   */
  protected DocBuilderBase() throws IndexException {
    super();

    builder = newBuilder();
  }

  /* ===================================================================
   *                   package private methods
   * =================================================================== */

  protected XContentBuilder newBuilder() throws IndexException {
    try {
      XContentBuilder builder = XContentFactory.jsonBuilder();

      if (debug()) {
        builder = builder.prettyPrint();
      }

      return builder;
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }
  
  protected EsDocInfo makeDocInfo(final String type,
                                 final long version,
                                 final String id) {
    return new EsDocInfo(builder, type, version, id);
  }

  public static class UpdateInfo {
    private String dtstamp;
    private Long count = 0L;

    public UpdateInfo() {
    }

    public UpdateInfo(final String dtstamp,
                      final Long count) {
      this.dtstamp = dtstamp;
      this.count = count;
    }

    /**
     * @return dtstamp last time this object type saved
     */
    public String getDtstamp() {
      return dtstamp;
    }

    /**
     * @return count of updates
     */
    public Long getCount() {
      return count;
    }

    /**
     * @return a change token for the index.
     */
    public String getChangeToken() {
      return dtstamp + ";" + count;
    }
  }

  public static UpdateInfo makeUpdateInfo(final String timestamp,
                                          Long l) {
    if (l == null) {
      l = 0L;
    }

    return new UpdateInfo(timestamp,
                          l);
  }

  protected void startObject() throws IndexException {
    try {
      builder.startObject();
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  protected void startObject(final String id) throws IndexException {
    try {
      builder.startObject(id);
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  protected void endObject() throws IndexException {
    try {
      builder.endObject();
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  protected void startArray(final String id) throws IndexException {
    try {
      builder.startArray(id);
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  protected void endArray() throws IndexException {
    try {
      builder.endArray();
    } catch (final Throwable t) {
      throw new IndexException(t);
    }
  }

  /* Return the docinfo for the indexer */
  public EsDocInfo makeDoc(final UpdateInfo ent) throws IndexException {
    startObject();

    makeField("count", ent.getCount());

    endObject();

    return makeDocInfo(docTypeUpdateTracker, 0,
                       updateTrackerId);
  }

  protected void makeField(final String id,
                           final Number val) throws IndexException {
    if (val == null) {
      return;
    }

    try {
      builder.field(id, val);
    } catch (final IOException e) {
      throw new IndexException(e);
    }
  }

  protected void makeField(final String id,
                           final Object val) throws IndexException {
    if (val == null) {
      return;
    }

    try {
      builder.field(id, String.valueOf(val));
    } catch (final IOException e) {
      throw new IndexException(e);
    }
  }

  protected void indexStrings(final String name,
                              final Collection<String> vals) throws IndexException {
    if (Util.isEmpty(vals)) {
      return;
    }

    startArray(name);
    for (final String val: vals) {
      value(val);
    }

    endArray();
  }

  protected void value(final Object val) throws IndexException {
    if (val == null) {
      return;
    }

    try {
      builder.value(String.valueOf(val));
    } catch (final IOException e) {
      throw new IndexException(e);
    }
  }

  @SuppressWarnings("unused")
  protected void makeField(final String id,
                           final Collection<String> vals) throws IndexException {
    try {
      if (Util.isEmpty(vals)) {
        return;
      }

      builder.startArray(id);

      for (final String s: vals) {
        builder.value(s);
      }

      builder.endArray();
    } catch (final IOException e) {
      throw new IndexException(e);
    }
  }

  /* ====================================================================
   *                   Logged methods
   * ==================================================================== */

  private BwLogger logger = new BwLogger();

  @Override
  public BwLogger getLogger() {
    if ((logger.getLoggedClass() == null) && (logger.getLoggedName() == null)) {
      logger.setLoggedClass(getClass());
    }

    return logger;
  }
}