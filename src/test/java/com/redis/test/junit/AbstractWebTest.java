package com.redis.test.junit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.ServletWebRequest;

/**
 * @author <a href="mailto:Daniel@webull.com">杨浩</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
public abstract class AbstractWebTest extends AbstractJUnit4SpringContextTests {
	protected final Logger logger = LogManager.getLogger(getClass());
	protected @Autowired WebApplicationContext wac;
	protected @Autowired MockServletContext servletContext;
	protected @Autowired MockHttpSession session;
	protected @Autowired MockHttpServletRequest request;
	protected @Autowired MockHttpServletResponse response;
	protected @Autowired ServletWebRequest webRequest;
    protected MockMvc mvc;
    
    @Before
    public void _setup() {
    	mvc = MockMvcBuilders.webAppContextSetup(wac).build();
    }
    
}
