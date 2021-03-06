/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.serialization.bagit;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class BagItSerializerIT extends AbstractResourceIT {

    @Test
    public void tryOneObject() throws ClientProtocolException, IOException {
        client.execute(postObjMethod("BagIt1"));
        client.execute(postDSMethod("BagIt1", "testDS", "stuff"));
        final HttpGet getObjMethod =
                new HttpGet(serverAddress +
                        "objects/BagIt1/fcr:export?format=bagit");
        HttpResponse response = client.execute(getObjMethod);
        assertEquals(200, response.getStatusLine().getStatusCode());
        final String content = EntityUtils.toString(response.getEntity());
        logger.debug("Found exported object: " + content);
        client.execute(new HttpDelete(serverAddress + "objects/BagIt1"));
        logger.debug("Deleted test object.");
        final HttpPost importMethod =
                new HttpPost(serverAddress + "objects/fcr:import?format=bagit");
        importMethod.setEntity(new StringEntity(content));
        assertEquals("Couldn't import!", 201, getStatus(importMethod));
        final HttpGet httpGet = new HttpGet(serverAddress + "objects/BagIt1");
        httpGet.setHeader("Accepts", "application/n3");
        response = client.execute(httpGet);
        assertEquals("Couldn't find reimported object!", 200, response
                .getStatusLine().getStatusCode());
        response =
                client.execute(new HttpGet(serverAddress +
                        "objects/BagIt1/testDS"));
        assertEquals("Couldn't find reimported datastream!", 200, response
                .getStatusLine().getStatusCode());
        logger.debug("Successfully reimported!");
    }
}
