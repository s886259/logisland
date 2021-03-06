/**
 * Copyright (C) 2016 Hurence (support@hurence.com)
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
package com.hurence.logisland.service.mongodb;


import com.hurence.logisland.record.FieldType;
import com.hurence.logisland.record.Record;
import com.hurence.logisland.record.StandardRecord;
import org.bson.Document;

/**
 * this class converts a logisland Record to a BSON document back and fort
 */
public class RecordConverter {

    public static final String MONGO_DOC_TYPE = "mongo_document";

    public static Record convert(Document document) {
        Record record = new StandardRecord(MONGO_DOC_TYPE)
                .setId(document.getObjectId("_id").toString());


        document.entrySet().forEach(entry -> {
            if (entry.getValue() instanceof String) {
                record.setField(entry.getKey(), FieldType.STRING, entry.getValue());
            } else if (entry.getValue() instanceof Integer) {
                record.setField(entry.getKey(), FieldType.INT, entry.getValue());
            } else if (entry.getValue() instanceof Long) {
                record.setField(entry.getKey(), FieldType.LONG, entry.getValue());
            } else if (entry.getValue() instanceof Double) {
                record.setField(entry.getKey(), FieldType.DOUBLE, entry.getValue());
            } else if (entry.getValue() instanceof Float) {
                record.setField(entry.getKey(), FieldType.FLOAT, entry.getValue());
            }
        });


        return record;
    }


    public static Document convert(Record record) {
        Document document = new Document("_id", record.getId());

        record.getFieldsEntrySet().forEach(entry -> {
            document.put(entry.getKey(), entry.getValue().getRawValue());

        });

        return document;
    }
}
