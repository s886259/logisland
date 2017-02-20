/**
 * Copyright (C) 2017 Hurence 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hurence.logisland.processor.bro;

import com.hurence.logisland.record.FieldDictionary;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import com.hurence.logisland.util.runner.MockRecord;
import com.hurence.logisland.util.runner.TestRunner;
import com.hurence.logisland.util.runner.TestRunners;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple Bro events processor.
 */
public class BroProcessorTest {
    
    private static Logger logger = LoggerFactory.getLogger(BroProcessorTest.class);
    
    // Bro conn input event
    private static final String BRO_CONN_EVENT =
        "{" +
            "\"conn\": {" +
                "\"ts\": 1487603366.277002," +
                "\"uid\": \"Coo3g71UUMM2AyxWB\"," +
                "\"id.orig_h\": \"172.17.0.3\"," +
                "\"id.orig_p\": 9200," +
                "\"id.resp_h\": \"172.17.0.2\"," +
                "\"id.resp_p\": 42770," +
                "\"proto\": \"tcp\"," +
                "\"conn_state\": \"OTH\"," +
                "\"local_orig\": true," +
                "\"local_resp\": true," +
                "\"missed_bytes\": 0," +
                "\"history\": \"Cc\"," +
                "\"orig_pkts\": 0," +
                "\"orig_ip_bytes\": 0," +
                "\"resp_pkts\": 0," +
                "\"resp_ip_bytes\": 0," +
                "\"tunnel_parents\": []" +
            "}" +
        "}";

    // Bro dns input event
    private static final String BRO_DNS_EVENT =
        "{" +
            "\"dns\": {" +
                "\"ts\": 1487603382.840372," +
                "\"uid\": \"Csevsb0Kzff0gvXDe\"," +
                "\"id.orig_h\": \"172.17.0.3\"," +
                "\"id.orig_p\": 49215," +
                "\"id.resp_h\": \"8.8.8.8\"," +
                "\"id.resp_p\": 53," +
                "\"proto\": \"udp\"," +
                "\"trans_id\": 26310," +
                "\"query\": \"www.yahoo.fr\"," +
                "\"rcode\": 0," +
                "\"rcode_name\": \"NOERROR\"," +
                "\"AA\": false," +
                "\"TC\": false," +
                "\"RD\": false," +
                "\"RA\": false," +
                "\"Z\": 0," +
                "\"answers\": [\"rc.yahoo.com\",\"src.g03.yahoodns.net\",\"77.238.184.150\"]," +
                "\"TTLs\": [17.0,17.0,17.0]," +
                "\"rejected\": false" +
            "}" +
        "}";
    
    @Test
    public void testBroDnsEvent() {
        final TestRunner testRunner = TestRunners.newTestRunner(new BroProcessor());
        testRunner.assertValid();
        Record record = new StandardRecord("bro_event");
        record.setStringField(FieldDictionary.RECORD_VALUE, BRO_DNS_EVENT);
        testRunner.enqueue(record);
        testRunner.clearQueues();
        testRunner.run();
        testRunner.assertAllInputRecordsProcessed();
        testRunner.assertOutputRecordsCount(1);

        MockRecord out = testRunner.getOutputRecords().get(0);
        
        out.assertFieldExists(FieldDictionary.RECORD_TYPE);
        out.assertFieldEquals(FieldDictionary.RECORD_TYPE, "dns");
        
        out.assertFieldExists("ts");
        out.assertFieldEquals("ts", (float)1487603382.840372);
        
        out.assertFieldExists("uid");
        out.assertFieldEquals("uid", "Csevsb0Kzff0gvXDe");

        out.assertFieldExists("id_orig_h");
        out.assertFieldEquals("id_orig_h", "172.17.0.3");
        
        out.assertFieldExists("id_orig_p");
        out.assertFieldEquals("id_orig_p", (int)49215);
        
        out.assertFieldExists("id_resp_h");
        out.assertFieldEquals("id_resp_h", "8.8.8.8");
        
        out.assertFieldExists("id_resp_p");
        out.assertFieldEquals("id_resp_p", (int)53);
        
        out.assertFieldExists("proto");
        out.assertFieldEquals("proto", "udp");
        
        out.assertFieldExists("trans_id");
        out.assertFieldEquals("trans_id", (int)26310);
        
        out.assertFieldExists("query");
        out.assertFieldEquals("query", "www.yahoo.fr");
        
        out.assertFieldExists("rcode");
        out.assertFieldEquals("rcode", (int)0);
        
        out.assertFieldExists("rcode_name");
        out.assertFieldEquals("rcode_name", "NOERROR");
        
        out.assertFieldExists("AA");
        out.assertFieldEquals("AA", false);
        
        out.assertFieldExists("TC");
        out.assertFieldEquals("TC", false);
        
        out.assertFieldExists("RD");
        out.assertFieldEquals("RD", false);
        
        out.assertFieldExists("RA");
        out.assertFieldEquals("RA", false);
        
        out.assertFieldExists("Z");
        out.assertFieldEquals("Z", (int)0);
        
        out.assertFieldExists("answers");
        List<String> answers = (List<String>)out.getField("answers").getRawValue();        
        assertEquals(Arrays.asList("rc.yahoo.com", "src.g03.yahoodns.net", "77.238.184.150"), answers);
        
        out.assertFieldExists("TTLs");
        List<String> ttls = (List<String>)out.getField("TTLs").getRawValue();        
        assertEquals(Arrays.asList(17.0 , 17.0 , 17.0), ttls);
        
        out.assertFieldExists("rejected");
        out.assertFieldEquals("rejected", false);
    }
    
    @Test
    public void testBroConnEvent() {
        final TestRunner testRunner = TestRunners.newTestRunner(new BroProcessor());
        testRunner.assertValid();
        Record record = new StandardRecord("bro_event");
        record.setStringField(FieldDictionary.RECORD_VALUE, BRO_CONN_EVENT);
        testRunner.enqueue(record);
        testRunner.clearQueues();
        testRunner.run();
        testRunner.assertAllInputRecordsProcessed();
        testRunner.assertOutputRecordsCount(1);

        MockRecord out = testRunner.getOutputRecords().get(0);
        
        out.assertFieldExists(FieldDictionary.RECORD_TYPE);
        out.assertFieldEquals(FieldDictionary.RECORD_TYPE, "conn");
        
        out.assertFieldExists("ts");
        out.assertFieldEquals("ts", (float)1487603366.277002);
        
        out.assertFieldExists("uid");
        out.assertFieldEquals("uid", "Coo3g71UUMM2AyxWB");

        out.assertFieldExists("id_orig_h");
        out.assertFieldEquals("id_orig_h", "172.17.0.3");
        
        out.assertFieldExists("id_orig_p");
        out.assertFieldEquals("id_orig_p", (int)9200);
        
        out.assertFieldExists("id_resp_h");
        out.assertFieldEquals("id_resp_h", "172.17.0.2");
        
        out.assertFieldExists("id_resp_p");
        out.assertFieldEquals("id_resp_p", (int)42770);
        
        out.assertFieldExists("proto");
        out.assertFieldEquals("proto", "tcp");
        
        out.assertFieldExists("conn_state");
        out.assertFieldEquals("conn_state", "OTH");
        
        out.assertFieldExists("local_orig");
        out.assertFieldEquals("local_orig", true);
        
        out.assertFieldExists("local_resp");
        out.assertFieldEquals("local_resp", true);
        
        out.assertFieldExists("missed_bytes");
        out.assertFieldEquals("missed_bytes", (int)0);
        
        out.assertFieldExists("history");
        out.assertFieldEquals("history", "Cc");
        
        out.assertFieldExists("orig_pkts");
        out.assertFieldEquals("orig_pkts", (int)0);
        
        out.assertFieldExists("orig_ip_bytes");
        out.assertFieldEquals("orig_ip_bytes", (int)0);

        out.assertFieldExists("resp_ip_bytes");
        out.assertFieldEquals("resp_ip_bytes", (int)0);
        
        out.assertFieldExists("tunnel_parents");
        List<String> tunnelParents = (List<String>)out.getField("tunnel_parents").getRawValue();
        assertEquals(0, tunnelParents.size());       
    }
}
