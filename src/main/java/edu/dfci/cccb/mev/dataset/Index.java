/*
 * The MIT License (MIT)
 * Copyright (c) 2017 Dana-Farber Cancer Institute
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package edu.dfci.cccb.mev.dataset;

import static java.lang.String.valueOf;

import javax.inject.Inject;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.ws.rs.InternalServerErrorException;

import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.transport.TransportClient;

/**
 * Index
 * 
 * @author levk
 */
public class Index {

  /**
   * ELasticSearch
   */
  private @Inject TransportClient es;

  /**
   * @param dimension
   */
  @PostLoad
  void inject (Dimension dimension) {
    dimension.annotations.es = es;
  }

  /**
   * @param dimension
   */
  @PostPersist
  void index (Dimension dimension) {
    if (!dimension.annotations.cache.isEmpty ()) {
      BulkRequestBuilder b = es.prepareBulk ();
      String i = valueOf (dimension.id);
      dimension.annotations.cache.forEach ( (k, p) -> b.add (es.prepareIndex (i, "annotation", k).setSource (p)));
      BulkResponse r = b.get ();
      if (r.hasFailures ()) throw new InternalServerErrorException (r.buildFailureMessage ());
    }
  }

  /**
   * @param dimension
   */
  @PostRemove
  void remove (Dimension dimension) {
    es.admin ().indices ().delete (new DeleteIndexRequest (valueOf (dimension.id)));
  }
}