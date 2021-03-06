/*
 * Copyright 2017, Yahoo Holdings Inc.
 * Copyrights licensed under the GPL License.
 * See the accompanying LICENSE file for terms.
 */

package com.yahoo.sherlock.model;

import com.yahoo.sherlock.TestUtilities;
import com.yahoo.sherlock.exception.SherlockException;
import com.yahoo.sherlock.settings.Constants;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

public class DruidClusterTest {

    @BeforeClass
    public void beforeClass() {
        Set<String> set = new HashSet<>();
        set.add("bkr123.p1.abc.com:4443");
        set.add("broker1.cluster2.com:4080");
        set.add("localhost:1234");
        TestUtilities.injectStaticFinal(null, Constants.class, "VALID_DRUID_BROKERS", Collections.unmodifiableSet(set));
    }

    @AfterClass
    public void afterClass() {
        TestUtilities.injectStaticFinal(null, Constants.class, "VALID_DRUID_BROKERS", null);
    }

    @Test
    public void testParameterConstructor() {
        DruidCluster c = new DruidCluster(
                1,
                "clusterName",
                "clusterDescription",
                "brokerHost",
                123,
                "brokerEndpoint",
                1234,
                true,
                ""
        );
        assertEquals(c.getClusterId(), (Integer) 1);
        assertEquals(c.getClusterName(), "clusterName");
        assertEquals(c.getClusterDescription(), "clusterDescription");
        assertEquals(c.getBrokerEndpoint(), "brokerEndpoint");
        assertEquals(c.getBrokerHost(), "brokerHost");
        assertEquals(c.getBrokerPort(), (Integer) 123);
        assertEquals(c.getHoursOfLag(), (Integer) 1234);
        assertEquals(c.getIsSSLAuth(), (Boolean) true);
        assertEquals(c.getPrincipalName(), "");
    }

    @Test
    public void testGetStatusReturnsErrorOnFailedConnection() {
        DruidCluster c = new DruidCluster();
        c.setBrokerHost("localdfffhost");
        c.setBrokerPort(1);
        c.setBrokerEndpoint("asefg");
        assertEquals("ERROR", c.getStatus());
    }

    @Test
    public void testGetBaseUrl() {
        DruidCluster c = new DruidCluster();
        c.setBrokerHost("localhost");
        c.setBrokerPort(1234);
        assertEquals(c.getBaseUrl(), "http://localhost:1234/");
    }

    @Test
    public void testGetBrokerUrl() {
        DruidCluster c = new DruidCluster();
        c.setBrokerHost("localhost");
        c.setBrokerPort(1234);
        c.setBrokerEndpoint("druid/v2");
        assertEquals(c.getBrokerUrl(), "http://localhost:1234/druid/v2/");
    }

    @Test
    public void testValidateInvalidName() {
        DruidCluster c = new DruidCluster();
        try {
            c.validate();
        } catch (SherlockException e) {
            assertEquals(e.getMessage(), "Cluster name cannot be empty");
            return;
        }
        fail("Expected exception");
    }

    @Test
    public void testValidateInvalidHost() {
        DruidCluster c = new DruidCluster();
        c.setClusterName("hello");
        try {
            c.validate();
        } catch (SherlockException e) {
            assertEquals(e.getMessage(), "Broker host cannot be empty");
            return;
        }
        fail("Expected exception");
    }

    @Test
    public void testValidateInvalidPort() {
        DruidCluster c = new DruidCluster();
        c.setClusterName("hello");
        c.setBrokerHost("host");
        try {
            c.validate();
        } catch (SherlockException e) {
            assertEquals(e.getMessage(), "Broker port cannot be empty");
            return;
        }
        fail("Expected exception");
    }

    @Test
    public void testValidateInvalidEndpoint() {
        DruidCluster c = new DruidCluster();
        c.setClusterName("hello");
        c.setBrokerHost("host");
        c.setBrokerPort(1234);
        try {
            c.validate();
        } catch (SherlockException e) {
            assertEquals(e.getMessage(), "Broker endpoint cannot be empty");
            return;
        }
        fail("Expected exception");
    }

    @Test
    public void testValidateNonNumericalPort() {
        DruidCluster c = new DruidCluster();
        c.setClusterName("hello");
        c.setBrokerHost("host");
        c.setBrokerPort(-12345);
        c.setBrokerEndpoint("druid/v2");
        try {
            c.validate();
        } catch (SherlockException e) {
            assertEquals(e.getMessage(), "Broker port must be a non-negative number");
            return;
        }
        fail("Expected exception");
    }

    @Test
    public void testValidateBrokerHostInvalidCharacters() {
        DruidCluster c = new DruidCluster();
        c.setClusterName("helo");
        c.setBrokerHost("asdf/asdf");
        c.setBrokerPort(1234);
        c.setBrokerEndpoint("druid/v2");
        try {
            c.validate();
        } catch (SherlockException e) {
            assertEquals(e.getMessage(), "Broker host should not contain any '/' or ':' characters");
            return;
        }
        fail("Expected exception");
    }

    @Test
    public void testValidateDescription() {
        DruidCluster c = new DruidCluster();
        c.setClusterName("hello");
        c.setBrokerHost("localhost");
        c.setBrokerPort(1234);
        c.setBrokerEndpoint("/druid/v2/");
        try {
            c.validate();
            assertEquals(c.getBrokerEndpoint(), "druid/v2");
            assertEquals(c.getClusterDescription(), "");
        } catch (SherlockException e) {
            fail(e.toString());
        }
    }

    @Test
    public void isAllowedHost() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        DruidCluster c = new DruidCluster();
        Method m = c.getClass().getDeclaredMethod("isAllowedHost", String.class, Integer.class);
        m.setAccessible(true);
        assertTrue((Boolean) m.invoke(c, "bkr123.p1.abc.com", 4443));
        assertFalse((Boolean) m.invoke(c, "bkr123.p2.abc.com", 4443));
    }

}
