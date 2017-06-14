package pkg.demo.test;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.netflix.eureka.EurekaDiscoveryClient;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest
@WebAppConfiguration
public class MainTest {
	@Autowired
	DiscoveryClient discoveryClient;

	@Test
	public void discoveryClientIsEureka() {
		assertTrue("discoveryClient is wrong type: " + this.discoveryClient,
				this.discoveryClient instanceof EurekaDiscoveryClient);
	}
}
