package pkg.demo.test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import pkg.demo.ServiceInstanceMain;
import pkg.demo.common.utils.JasEncryptUtils;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ContextConfiguration(classes = { ServiceInstanceMain.class })
public class JasEncryptUtilsSample {
	private static final Logger logger = LoggerFactory.getLogger(JasEncryptUtilsSample.class);
	@Autowired
	private JasEncryptUtils jasEncryptUtils;

	@Test
	public void pass() throws Exception {
		String passwd = "ENC(l1aDwNtcbUy9JXSAh0tbf6+ceWwbehii)";
		logger.info("passwd:" + passwd);
		String passwd_ = jasEncryptUtils.decrypt(passwd);
		logger.info("passwd_:" + passwd_);
	}

}
