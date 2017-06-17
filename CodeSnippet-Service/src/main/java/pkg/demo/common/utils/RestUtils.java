package pkg.demo.common.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pkg.demo.common.pojo.ResultEntity;

public class RestUtils {

	public static <T> ResponseEntity<ResultEntity<T>> build(T t, HttpStatus status) {
		ResultEntity<T> result = new ResultEntity<T>(t, status.value());
		return ResponseEntity.status(status).body(result);
	}

	public static <T> ResponseEntity<ResultEntity<T>> ok(T t) {
		return ResponseEntity.ok(new ResultEntity<T>(t, HttpStatus.OK.value()));
	}

	public static ResponseEntity<ResultEntity<String>> ok() {
		return ResponseEntity.ok(new ResultEntity<String>("", HttpStatus.OK.value()));
	}
}
