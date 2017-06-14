package pkg.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import pkg.demo.common.utils.RestUtils;
import pkg.demo.modal.TCountries;
import pkg.demo.service.IDemoService;

//@CrossOrigin
@RestController
@RequestMapping("/api/demoCtrl")
public class DemoController {

	@Autowired
	private IDemoService demoService;

	@RequestMapping(value = "/getCountry/{id}", method = RequestMethod.GET)
	public ResponseEntity<?> getCountry(@PathVariable("id") int id) {
		TCountries bean = demoService.getCountry(id);
		return RestUtils.ok(bean);
	}

}