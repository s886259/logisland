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
package com.hurence.logisland.util;

import java.util.List;
import java.util.function.Function;

public class ListUtils {
    static public <E> String mkString(List<E> list, Function<E,String> stringify, String delimiter) {
        int i = 0;
        StringBuilder s = new StringBuilder();
        for (E e : list) {
            if (i != 0) { s.append(delimiter); }
            s.append(stringify.apply(e));
            i++;
        }
        return s.toString();
    }
}