/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.solr.client.solrj.response;

import junit.framework.Assert;

import org.apache.lucene.util.LuceneTestCase;
import org.apache.lucene.util.TestRuleLimitSysouts.Limit;
import org.apache.solr.client.solrj.impl.XMLResponseParser;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.DateUtil;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrResourceLoader;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * A few tests for parsing Solr response in QueryResponse
 * 
 * @since solr 1.3
 */
@Limit(bytes=20000)
public class QueryResponseTest extends LuceneTestCase {
  @Test
  public void testDateFacets() throws Exception   {
    XMLResponseParser parser = new XMLResponseParser();
    InputStream is = new SolrResourceLoader(null, null).openResource("solrj/sampleDateFacetResponse.xml");
    assertNotNull(is);
    Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
    NamedList<Object> response = parser.processResponse(in);
    in.close();
    
    QueryResponse qr = new QueryResponse(response, null);
    Assert.assertNotNull(qr);
    
    Assert.assertNotNull(qr.getFacetDates());
    
    for (FacetField f : qr.getFacetDates()) {
      Assert.assertNotNull(f);

      // TODO - test values?
      // System.out.println(f.toString());
      // System.out.println("GAP: " + f.getGap());
      // System.out.println("END: " + f.getEnd());
    }
  }

  @Test
  public void testRangeFacets() throws Exception {
    XMLResponseParser parser = new XMLResponseParser();
    InputStream is = new SolrResourceLoader(null, null).openResource("solrj/sampleDateFacetResponse.xml");
    assertNotNull(is);
    Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
    NamedList<Object> response = parser.processResponse(in);
    in.close();

    QueryResponse qr = new QueryResponse(response, null);
    Assert.assertNotNull(qr);

    int counter = 0;
    RangeFacet.Numeric price = null;
    RangeFacet.Date manufacturedateDt = null;
    for (RangeFacet r : qr.getFacetRanges()){
      assertNotNull(r);
      if ("price".equals(r.getName())) {
        price = (RangeFacet.Numeric) r;
      } else if ("manufacturedate_dt".equals(r.getName())) {
        manufacturedateDt = (RangeFacet.Date) r;
      }

      counter++;
    }
    assertEquals(2, counter);
    assertNotNull(price);
    assertNotNull(manufacturedateDt);

    assertEquals(0.0F, price.getStart());
    assertEquals(5.0F, price.getEnd());
    assertEquals(1.0F, price.getGap());
    assertEquals("0.0", price.getCounts().get(0).getValue());
    assertEquals(3, price.getCounts().get(0).getCount());
    assertEquals("1.0", price.getCounts().get(1).getValue());
    assertEquals(0, price.getCounts().get(1).getCount());
    assertEquals("2.0", price.getCounts().get(2).getValue());
    assertEquals(0, price.getCounts().get(2).getCount());
    assertEquals("3.0", price.getCounts().get(3).getValue());
    assertEquals(0, price.getCounts().get(3).getCount());
    assertEquals("4.0", price.getCounts().get(4).getValue());
    assertEquals(0, price.getCounts().get(4).getCount());

    assertEquals(DateUtil.parseDate("2005-02-13T15:26:37Z"), manufacturedateDt.getStart());
    assertEquals(DateUtil.parseDate("2008-02-13T15:26:37Z"), manufacturedateDt.getEnd());
    assertEquals("+1YEAR", manufacturedateDt.getGap());
    assertEquals("2005-02-13T15:26:37Z", manufacturedateDt.getCounts().get(0).getValue());
    assertEquals(4, manufacturedateDt.getCounts().get(0).getCount());
    assertEquals("2006-02-13T15:26:37Z", manufacturedateDt.getCounts().get(1).getValue());
    assertEquals(7, manufacturedateDt.getCounts().get(1).getCount());
    assertEquals("2007-02-13T15:26:37Z", manufacturedateDt.getCounts().get(2).getValue());
    assertEquals(0, manufacturedateDt.getCounts().get(2).getCount());
    assertEquals(90, manufacturedateDt.getBefore());
    assertEquals(1, manufacturedateDt.getAfter());
    assertEquals(11, manufacturedateDt.getBetween());
  }

  @Test
  public void testGroupResponse() throws Exception {
    XMLResponseParser parser = new XMLResponseParser();
    InputStream is = new SolrResourceLoader(null, null).openResource("solrj/sampleGroupResponse.xml");
    assertNotNull(is);
    Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
    NamedList<Object> response = parser.processResponse(in);
    in.close();

    QueryResponse qr = new QueryResponse(response, null);
    assertNotNull(qr);
    GroupResponse groupResponse = qr.getGroupResponse();
    assertNotNull(groupResponse);
    List<GroupCommand> commands = groupResponse.getValues();
    assertNotNull(commands);
    assertEquals(3, commands.size());

    GroupCommand fieldCommand = commands.get(0);
    assertEquals("acco_id", fieldCommand.getName());
    assertEquals(30000000, fieldCommand.getMatches());
    assertEquals(5687, fieldCommand.getNGroups().intValue());
    List<Group> fieldCommandGroups = fieldCommand.getValues();
    assertEquals(10, fieldCommandGroups.size());
    assertEquals("116_ar", fieldCommandGroups.get(0).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(0).getResult().size());
    assertEquals(2236, fieldCommandGroups.get(0).getResult().getNumFound());
    assertEquals("116_hi", fieldCommandGroups.get(1).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(1).getResult().size());
    assertEquals(2234, fieldCommandGroups.get(1).getResult().getNumFound());
    assertEquals("953_ar", fieldCommandGroups.get(2).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(2).getResult().size());
    assertEquals(1020, fieldCommandGroups.get(2).getResult().getNumFound());
    assertEquals("953_hi", fieldCommandGroups.get(3).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(3).getResult().size());
    assertEquals(1030, fieldCommandGroups.get(3).getResult().getNumFound());
    assertEquals("954_ar", fieldCommandGroups.get(4).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(4).getResult().size());
    assertEquals(2236, fieldCommandGroups.get(4).getResult().getNumFound());
    assertEquals("954_hi", fieldCommandGroups.get(5).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(5).getResult().size());
    assertEquals(2234, fieldCommandGroups.get(5).getResult().getNumFound());
    assertEquals("546_ar", fieldCommandGroups.get(6).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(6).getResult().size());
    assertEquals(4984, fieldCommandGroups.get(6).getResult().getNumFound());
    assertEquals("546_hi", fieldCommandGroups.get(7).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(7).getResult().size());
    assertEquals(4984, fieldCommandGroups.get(7).getResult().getNumFound());
    assertEquals("708_ar", fieldCommandGroups.get(8).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(8).getResult().size());
    assertEquals(4627, fieldCommandGroups.get(8).getResult().getNumFound());
    assertEquals("708_hi", fieldCommandGroups.get(9).getGroupValue());
    assertEquals(2, fieldCommandGroups.get(9).getResult().size());
    assertEquals(4627, fieldCommandGroups.get(9).getResult().getNumFound());

    GroupCommand funcCommand = commands.get(1);
    assertEquals("sum(price, price)", funcCommand.getName());
    assertEquals(30000000, funcCommand.getMatches());
    assertNull(funcCommand.getNGroups());
    List<Group> funcCommandGroups = funcCommand.getValues();
    assertEquals(10, funcCommandGroups.size());
    assertEquals("95000.0", funcCommandGroups.get(0).getGroupValue());
    assertEquals(2, funcCommandGroups.get(0).getResult().size());
    assertEquals(43666, funcCommandGroups.get(0).getResult().getNumFound());
    assertEquals("91400.0", funcCommandGroups.get(1).getGroupValue());
    assertEquals(2, funcCommandGroups.get(1).getResult().size());
    assertEquals(27120, funcCommandGroups.get(1).getResult().getNumFound());
    assertEquals("104800.0", funcCommandGroups.get(2).getGroupValue());
    assertEquals(2, funcCommandGroups.get(2).getResult().size());
    assertEquals(34579, funcCommandGroups.get(2).getResult().getNumFound());
    assertEquals("99400.0", funcCommandGroups.get(3).getGroupValue());
    assertEquals(2, funcCommandGroups.get(3).getResult().size());
    assertEquals(40519, funcCommandGroups.get(3).getResult().getNumFound());
    assertEquals("109600.0", funcCommandGroups.get(4).getGroupValue());
    assertEquals(2, funcCommandGroups.get(4).getResult().size());
    assertEquals(36203, funcCommandGroups.get(4).getResult().getNumFound());
    assertEquals("102400.0", funcCommandGroups.get(5).getGroupValue());
    assertEquals(2, funcCommandGroups.get(5).getResult().size());
    assertEquals(37852, funcCommandGroups.get(5).getResult().getNumFound());
    assertEquals("116800.0", funcCommandGroups.get(6).getGroupValue());
    assertEquals(2, funcCommandGroups.get(6).getResult().size());
    assertEquals(40393, funcCommandGroups.get(6).getResult().getNumFound());
    assertEquals("107800.0", funcCommandGroups.get(7).getGroupValue());
    assertEquals(2, funcCommandGroups.get(7).getResult().size());
    assertEquals(41639, funcCommandGroups.get(7).getResult().getNumFound());
    assertEquals("136200.0", funcCommandGroups.get(8).getGroupValue());
    assertEquals(2, funcCommandGroups.get(8).getResult().size());
    assertEquals(25929, funcCommandGroups.get(8).getResult().getNumFound());
    assertEquals("131400.0", funcCommandGroups.get(9).getGroupValue());
    assertEquals(2, funcCommandGroups.get(9).getResult().size());
    assertEquals(29179, funcCommandGroups.get(9).getResult().getNumFound());

    GroupCommand queryCommand = commands.get(2);
    assertEquals("country:fr", queryCommand.getName());
    assertNull(queryCommand.getNGroups());
    assertEquals(30000000, queryCommand.getMatches());
    List<Group> queryCommandGroups = queryCommand.getValues();
    assertEquals(1, queryCommandGroups.size());
    assertEquals("country:fr", queryCommandGroups.get(0).getGroupValue());
    assertEquals(2, queryCommandGroups.get(0).getResult().size());
    assertEquals(57074, queryCommandGroups.get(0).getResult().getNumFound());
  }

  @Test
  public void testSimpleGroupResponse() throws Exception {
    XMLResponseParser parser = new XMLResponseParser();
    InputStream is = new SolrResourceLoader(null, null).openResource("solrj/sampleSimpleGroupResponse.xml");
    assertNotNull(is);
    Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
    NamedList<Object> response = parser.processResponse(in);
    in.close();

    QueryResponse qr = new QueryResponse(response, null);
    assertNotNull(qr);
    GroupResponse groupResponse = qr.getGroupResponse();
    assertNotNull(groupResponse);
    List<GroupCommand> commands = groupResponse.getValues();
    assertNotNull(commands);
    assertEquals(1, commands.size());

    GroupCommand fieldCommand = commands.get(0);
    assertEquals("acco_id", fieldCommand.getName());
    assertEquals(30000000, fieldCommand.getMatches());
    assertEquals(5687, fieldCommand.getNGroups().intValue());
    List<Group> fieldCommandGroups = fieldCommand.getValues();
    assertEquals(1, fieldCommandGroups.size());
    
    assertEquals("acco_id", fieldCommandGroups.get(0).getGroupValue());
    SolrDocumentList documents = fieldCommandGroups.get(0).getResult();
    assertNotNull(documents);
    
    assertEquals(10, documents.size());
    assertEquals("116_AR", documents.get(0).getFieldValue("acco_id"));
    assertEquals("116_HI", documents.get(1).getFieldValue("acco_id"));
    assertEquals("953_AR", documents.get(2).getFieldValue("acco_id"));
    assertEquals("953_HI", documents.get(3).getFieldValue("acco_id"));
    assertEquals("954_AR", documents.get(4).getFieldValue("acco_id"));
    assertEquals("954_HI", documents.get(5).getFieldValue("acco_id"));
    assertEquals("546_AR", documents.get(6).getFieldValue("acco_id"));
    assertEquals("546_HI", documents.get(7).getFieldValue("acco_id"));
    assertEquals("708_AR", documents.get(8).getFieldValue("acco_id"));
    assertEquals("708_HI", documents.get(9).getFieldValue("acco_id"));
  }
  
  @Deprecated
  public void testIntervalFacetsResponse() throws Exception {
    XMLResponseParser parser = new XMLResponseParser();
    try(SolrResourceLoader loader = new SolrResourceLoader(null, null)) {
      InputStream is = loader.openResource("solrj/sampleIntervalFacetsResponse.xml");
      assertNotNull(is);
      Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
      NamedList<Object> response = parser.processResponse(in);
      in.close();
      
      QueryResponse qr = new QueryResponse(response, null);
      assertNotNull(qr);
      assertNotNull(qr.getIntervalFacets());
      assertEquals(2, qr.getIntervalFacets().size());
      
      IntervalFacet facet = qr.getIntervalFacets().get(0);
      assertEquals("price", facet.getField());
      assertEquals(3, facet.getIntervals().size());
      
      assertEquals("[0,10]", facet.getIntervals().get(0).getKey());
      assertEquals("(10,100]", facet.getIntervals().get(1).getKey());
      assertEquals("(100,*]", facet.getIntervals().get(2).getKey());
      
      assertEquals(3, facet.getIntervals().get(0).getCount());
      assertEquals(4, facet.getIntervals().get(1).getCount());
      assertEquals(9, facet.getIntervals().get(2).getCount());
      
      
      facet = qr.getIntervalFacets().get(1);
      assertEquals("popularity", facet.getField());
      assertEquals(3, facet.getIntervals().size());
      
      assertEquals("bad", facet.getIntervals().get(0).getKey());
      assertEquals("average", facet.getIntervals().get(1).getKey());
      assertEquals("good", facet.getIntervals().get(2).getKey());
      
      assertEquals(3, facet.getIntervals().get(0).getCount());
      assertEquals(10, facet.getIntervals().get(1).getCount());
      assertEquals(2, facet.getIntervals().get(2).getCount());
      
    }
    
  }

  @Test
  public void testIntervalRangeFacetsResponse() throws Exception {
    XMLResponseParser parser = new XMLResponseParser();
    try(SolrResourceLoader loader = new SolrResourceLoader(null, null)) {
      InputStream is = loader.openResource("solrj/sampleIntervalRangeFacetsResponse.xml");
      assertNotNull(is);
      Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
      NamedList<Object> response = parser.processResponse(in);
      in.close();

      QueryResponse qr = new QueryResponse(response, null);
      assertNotNull(qr);
      assertNotNull(qr.getFacetRanges());
      assertEquals(2, qr.getFacetRanges().size());

      RangeFacet facet = qr.getFacetRanges().get(0);
      assertEquals("price", facet.getName());
      assertEquals(3, facet.getIntervals().size());

      RangeFacet.Interval interval = (RangeFacet.Interval) facet;
      assertEquals("[0,10]", interval.getIntervals().get(0).getValue());
      assertEquals("(10,100]", interval.getIntervals().get(1).getValue());
      assertEquals("(100,*]", interval.getIntervals().get(2).getValue());

      assertEquals(3, interval.getIntervals().get(0).getCount());
      assertEquals(4, interval.getIntervals().get(1).getCount());
      assertEquals(9, interval.getIntervals().get(2).getCount());

      facet = qr.getFacetRanges().get(1);
      interval = (RangeFacet.Interval) facet;
      assertEquals("popularity", facet.getName());
      assertEquals(3, facet.getIntervals().size());

      assertEquals("bad", interval.getIntervals().get(0).getValue());
      assertEquals("average", interval.getIntervals().get(1).getValue());
      assertEquals("good", interval.getIntervals().get(2).getValue());

      assertEquals(3, interval.getIntervals().get(0).getCount());
      assertEquals(10, interval.getIntervals().get(1).getCount());
      assertEquals(2, interval.getIntervals().get(2).getCount());

    }
  }

  @Test
  public void testMultipleRangeFacetsResponse() throws Exception {
    XMLResponseParser parser = new XMLResponseParser();
    try(SolrResourceLoader loader = new SolrResourceLoader(null, null)) {
      InputStream is = loader.openResource("solrj/sampleMultipleRangeFacetsResponse.xml");
      assertNotNull(is);
      Reader in = new InputStreamReader(is, StandardCharsets.UTF_8);
      NamedList<Object> response = parser.processResponse(in);
      in.close();

      QueryResponse qr = new QueryResponse(response, null);
      assertNotNull(qr);
      assertNotNull(qr.getFacetRanges());
      assertEquals(1, qr.getFacetRanges().size());

      RangeFacet facet = qr.getFacetRanges().get(0);
      assertEquals("price", facet.getName());
      assertEquals(3, facet.getIntervals().size());
      assertEquals(5, facet.getCounts().size());

      RangeFacet.Numeric rangeFacet = (RangeFacet.Numeric) facet;
      assertEquals("[0,10]", rangeFacet.getIntervals().get(0).getValue());
      assertEquals("(10,100]", rangeFacet.getIntervals().get(1).getValue());
      assertEquals("(100,*]", rangeFacet.getIntervals().get(2).getValue());
      assertEquals(3, rangeFacet.getIntervals().get(0).getCount());
      assertEquals(4, rangeFacet.getIntervals().get(1).getCount());
      assertEquals(9, rangeFacet.getIntervals().get(2).getCount());

      assertEquals("0.0", rangeFacet.getCounts().get(0).getValue());
      assertEquals("50.0", rangeFacet.getCounts().get(1).getValue());
      assertEquals("100.0", rangeFacet.getCounts().get(2).getValue());
      assertEquals("150.0", rangeFacet.getCounts().get(3).getValue());
      assertEquals("200.0", rangeFacet.getCounts().get(4).getValue());
      assertEquals(3, rangeFacet.getCounts().get(0).getCount());
      assertEquals(1, rangeFacet.getCounts().get(1).getCount());
      assertEquals(0, rangeFacet.getCounts().get(2).getCount());
      assertEquals(0, rangeFacet.getCounts().get(3).getCount());
      assertEquals(0, rangeFacet.getCounts().get(4).getCount());
    }
  }
}
