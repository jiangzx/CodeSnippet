package pkg.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pkg.demo.dao.IDemoDao;
import pkg.demo.dao.TCountriesMapper;
import pkg.demo.modal.TCountries;

@Service
@Transactional(rollbackFor = Exception.class)
public class DemoService implements IDemoService {

	@Autowired
	private IDemoDao demoDao;

	@Autowired
	private TCountriesMapper countryMapper;

	private static final Logger logger = LoggerFactory.getLogger(DemoService.class);

	@Override
	public String sechduleJob() {
		String now = demoDao.now().getTime();
		logger.info("Hello, Current time: {}", now);
		return now;
	}

	@Override
	public TCountries getCountry(int id) {
		return countryMapper.selectByPrimaryKey(id);
	}
}
