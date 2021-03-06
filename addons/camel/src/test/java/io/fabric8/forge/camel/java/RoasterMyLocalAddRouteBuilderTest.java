/**
 *  Copyright 2005-2015 Red Hat, Inc.
 *
 *  Red Hat licenses this file to you under the Apache License, version
 *  2.0 (the "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied.  See the License for the specific language governing
 *  permissions and limitations under the License.
 */
package io.fabric8.forge.camel.java;

import java.io.File;
import java.util.List;

import io.fabric8.forge.camel.commands.project.helper.CamelJavaParserHelper;
import io.fabric8.forge.camel.commands.project.helper.ParserResult;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.junit.Assert;
import org.junit.Test;

public class RoasterMyLocalAddRouteBuilderTest {

    @Test
    public void parse() throws Exception {
        JavaClassSource clazz = (JavaClassSource) Roaster.parse(new File("src/test/java/io/fabric8/forge/camel/java/MyLocalAddRouteBuilderTest.java"));
        MethodSource<JavaClassSource> method = CamelJavaParserHelper.findConfigureMethod(clazz);
        Assert.assertNull(method);

        List<MethodSource<JavaClassSource>> methods = CamelJavaParserHelper.findInlinedConfigureMethods(clazz);
        Assert.assertEquals(1, methods.size());
        method = methods.get(0);

        List<ParserResult> list = CamelJavaParserHelper.parseCamelConsumerUris(method, true, true);
        for (ParserResult result : list) {
            System.out.println("Consumer: " + result.getElement());
        }

        list = CamelJavaParserHelper.parseCamelProducerUris(method, true, true);
        for (ParserResult result : list) {
            System.out.println("Producer: " + result.getElement());
        }
    }

}
